package com.github.gmazzo.buildconfig.internal

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import com.github.gmazzo.buildconfig.BuildConfigTask
import javax.inject.Inject
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.newInstance

internal abstract class DefaultBuildConfigSourceSet(
    private val defaultSpec: BuildConfigClassSpec,
    override val extraSpecs: NamedDomainObjectContainer<BuildConfigClassSpec>,
    private val providerFactory: ProviderFactory,
) :
    BuildConfigSourceSetInternal,
    BuildConfigClassSpec by defaultSpec,
    Iterable<TaskProvider<BuildConfigTask>>,
    GroovyNullValueWorkaround() {

    override var isSuperseded = false

    override var isKMPTarget = false

    @Inject
    @Suppress("UNCHECKED_CAST")
    constructor(
        name: String,
        objects: ObjectFactory,
        providerFactory: ProviderFactory,
    ) : this(
        defaultSpec = objects.newInstance<DefaultBuildConfigClassSpec>(name, "buildConfig source set <$name>"),
        extraSpecs = objects.domainObjectContainer(DefaultBuildConfigClassSpec::class.java) { extraName ->
            objects.newInstance<DefaultBuildConfigClassSpec>(
                extraName,
                "buildConfig source set <$name>, class <$extraName>"
            )
        } as NamedDomainObjectContainer<BuildConfigClassSpec>,
        providerFactory = providerFactory,
    )

    init {
        check(name.matches("[\\w-]+".toRegex())) {
            "Invalid name '$name': only alphanumeric characters are allowed"
        }
    }

    override val dependsOn = linkedSetOf<BuildConfigSourceSetInternal>()

    override val dependents = linkedSetOf<BuildConfigSourceSetInternal>()

    override lateinit var generateTask: TaskProvider<BuildConfigTask>

    override fun dependsOn(other: BuildConfigSourceSetInternal) {
        check(other != this) { "A source set cannot depend on itself: '$name'" }
        check(this !in other.allDependsOn) { "Circular dependency detected: '$name' -> '${other.name}'" }

        dependsOn += other
        (other as DefaultBuildConfigSourceSet).dependents += this
    }

    override fun supersededBy(other: BuildConfigSourceSetInternal) {
        check(other != this) { "A source set cannot supersede itself: '$name'" }

        if (!isSuperseded) {
            isSuperseded = true
            generateTask.configure {
                it.doFirst { error("'${this@DefaultBuildConfigSourceSet.name}' was superseded by '${other.name}' source set") }
            }
        }

        // copies all fields and settings to the new source set
        other.addMissing(this)
        extraSpecs.all { spec ->
            other.extraSpecs.register(spec.name) { extraSpec ->
                extraSpec.generator.convention(spec.generator)
                extraSpec.className.convention(spec.className)
                extraSpec.packageName.convention(spec.packageName)
                extraSpec.documentation.convention(spec.documentation)
                extraSpec.addMissing(spec)
            }
        }
    }

    private fun BuildConfigClassSpec.addMissing(from: BuildConfigClassSpec) {
        buildConfigFields.addAllLater(this@DefaultBuildConfigSourceSet.providerFactory.provider {
            from.buildConfigFields.filter { it.name !in buildConfigFields.names }
        })
    }

    override fun markAsKMPTarget() {
        isKMPTarget = true
    }

    override fun forClass(packageName: String?, className: String): BuildConfigClassSpec =
        extraSpecs.maybeCreate(className).also {
            it.className.value(className)
            it.packageName.value(packageName)
        }

    override fun iterator() = iterator {
        if (!isSuperseded) yield(generateTask)
    }

    override fun toString() = defaultSpec.toString()

}
