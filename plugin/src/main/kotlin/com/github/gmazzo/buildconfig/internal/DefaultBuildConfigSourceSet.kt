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
    override val extraSpecs: NamedDomainObjectContainer<out BuildConfigClassSpec>
) :
    BuildConfigSourceSetInternal,
    BuildConfigClassSpec by classSpec,
    Iterable<TaskProvider<BuildConfigTask>>,
    GroovyNullValueWorkaround() {

    @Inject
    @Suppress("unused")
    constructor(
        name: String,
        objects: ObjectFactory,
    ) : this(
        classSpec = objects.newInstance<DefaultBuildConfigClassSpec>(name),
        extraSpecs = objects.domainObjectContainer(DefaultBuildConfigClassSpec::class.java) { extraName ->
            objects.newInstance<DefaultBuildConfigClassSpec>(extraName)
        }
    )

    override val dependsOn = linkedSetOf<BuildConfigSourceSetInternal>()

    override lateinit var generateTask: TaskProvider<BuildConfigTask>

    override fun dependsOn(other: BuildConfigSourceSetInternal) {
        if (other !== this) {
            dependsOn += other
        }
    }

    override fun forClass(packageName: String?, className: String): BuildConfigClassSpec =
        extraSpecs.maybeCreate(className).also {
            it.className.value(className).disallowChanges()
            it.packageName.value(packageName)
        }

    override fun iterator() = iterator { yield(generateTask) }

    override fun toString() = "buildConfig source set <$name>"

}
