package com.github.gmazzo.buildconfig.internal

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import com.github.gmazzo.buildconfig.BuildConfigTask
import javax.inject.Inject
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.newInstance

internal abstract class DefaultBuildConfigSourceSet(
    classSpec: BuildConfigClassSpec,
    override val extraSpecs: NamedDomainObjectContainer<BuildConfigClassSpec>
) :
    BuildConfigSourceSetInternal,
    BuildConfigClassSpec by classSpec,
    Iterable<TaskProvider<BuildConfigTask>>,
    GroovyNullValueWorkaround() {

    private var superseded = false

    @Inject
    @Suppress("UNCHECKED_CAST")
    constructor(
        name: String,
        objects: ObjectFactory,
    ) : this(
        classSpec = objects.newInstance<DefaultBuildConfigClassSpec>(name),
        extraSpecs = objects.domainObjectContainer(DefaultBuildConfigClassSpec::class.java) { extraName ->
            objects.newInstance<DefaultBuildConfigClassSpec>(extraName)
        } as NamedDomainObjectContainer<BuildConfigClassSpec>
    )

    init {
        check(name.matches("[\\w-]+".toRegex())) {
            "Invalid name '$name': only alphanumeric characters are allowed"
        }
    }

    override val dependsOn = linkedSetOf<BuildConfigSourceSetInternal>()

    override lateinit var generateTask: TaskProvider<BuildConfigTask>

    override fun dependsOn(other: BuildConfigSourceSetInternal) {
        check(other != this) { "A source set cannot depend on itself: '$name'" }
        check(this !in other.allDependsOn) { "Circular dependency detected: '$name' -> '${other.name}'" }

        dependsOn += other
    }

    override fun supersededBy(other: BuildConfigSourceSetInternal) {
        check(other != this) { "A source set cannot supersede itself: '$name'" }

        if (!superseded) {
            superseded = true
            generateTask.configure {
                it.doFirst { error("'${this@DefaultBuildConfigSourceSet.name}' was superseded by '${other.name}' source set") }
            }
        }
        buildConfigFields.all(other.buildConfigFields::add)
        extraSpecs.all(other.extraSpecs::add)
    }

    override fun forClass(packageName: String?, className: String): BuildConfigClassSpec =
        extraSpecs.maybeCreate(className).also {
            it.className.value(className)
            it.packageName.value(packageName)
        }

    override fun iterator() = iterator {
        if (!superseded) yield(generateTask)
    }

    override fun toString() = "buildConfig source set <$name>"

}
