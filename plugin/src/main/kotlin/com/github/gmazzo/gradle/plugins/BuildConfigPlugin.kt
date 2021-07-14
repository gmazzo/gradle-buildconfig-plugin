package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.generators.BuildConfigJavaGenerator
import com.github.gmazzo.gradle.plugins.internal.BuildConfigClassSpecInternal
import com.github.gmazzo.gradle.plugins.internal.BuildConfigSourceSetInternal
import com.github.gmazzo.gradle.plugins.internal.DefaultBuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.internal.DefaultBuildConfigExtension
import com.github.gmazzo.gradle.plugins.internal.DefaultBuildConfigSourceSet
import com.github.gmazzo.gradle.plugins.internal.bindings.PluginBindingHandler
import com.github.gmazzo.gradle.plugins.internal.bindings.PluginBindings
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.register

class BuildConfigPlugin : Plugin<Project> {

    private val logger = Logging.getLogger(javaClass)

    override fun apply(project: Project) = with(project) {
        val sourceSets = container(BuildConfigSourceSetInternal::class.java) { name ->
            DefaultBuildConfigSourceSet(
                classSpec = objects.newInstance<DefaultBuildConfigClassSpec>(name),
                extraSpecs = project.container(BuildConfigClassSpecInternal::class.java) { extraName ->
                    objects.newInstance<DefaultBuildConfigClassSpec>(extraName)
                }
            )
        }

        val defaultSS = sourceSets.create(DEFAULT_SOURCE_SET_NAME)

        val extension = extensions.create(
            BuildConfigExtension::class.java,
            "buildConfig",
            DefaultBuildConfigExtension::class.java,
            sourceSets,
            defaultSS
        )

        sourceSets.configureEach {
            configureSourceSet(project, it, defaultSS.classSpec)
        }

        PluginBindings.values().forEach { binding ->
            pluginManager.withPlugin(binding.pluginId) {
                binding
                    .handler(project, extension)
                    .configure(sourceSets)
            }
        }
    }

    private fun <SourceSet> PluginBindingHandler<SourceSet>.configure(
        specs: NamedDomainObjectContainer<BuildConfigSourceSetInternal>
    ) {
        onBind()

        sourceSets.configureEach { ss ->
            val spec = specs.maybeCreate(nameOf(ss))

            onSourceSetAdded(ss, spec)
            spec.extraSpecs.configureEach { onSourceSetAdded(ss, it) }

            (ss as? ExtensionAware)?.extensions?.add("buildConfig", spec)
        }
    }

    private fun configureSourceSet(
        project: Project,
        sourceSet: BuildConfigSourceSetInternal,
        defaultSpec: BuildConfigClassSpecInternal
    ) {
        logger.debug("Creating buildConfig sourceSet '${sourceSet.name}' for $project")

        val prefix = when (val name = sourceSet.name.capitalize()) {
            "Main" -> ""
            else -> name
        }

        createGenerateTask(
            project, prefix, sourceSet, sourceSet.classSpec, defaultSpec,
            descriptionSuffix = "'${sourceSet.name}' source"
        )

        sourceSet.extraSpecs.configureEach { subSpec ->
            val childPrefix = prefix + subSpec.name.capitalize()

            createGenerateTask(
                project, childPrefix, sourceSet, subSpec, defaultSpec,
                descriptionSuffix = "'${subSpec.name}' spec on '${sourceSet.name}' source"
            )
        }
    }

    private fun createGenerateTask(
        project: Project,
        prefix: String,
        sourceSet: BuildConfigSourceSet,
        spec: BuildConfigClassSpecInternal,
        defaultSpec: BuildConfigClassSpecInternal,
        descriptionSuffix: String
    ) = project.tasks.register<BuildConfigTask>("generate${prefix}BuildConfig") {
        group = "BuildConfig"
        description = "Generates the build constants class for $descriptionSuffix"

        fields.set(spec.fields.values)
        outputDir.set(project.file("${project.buildDir}/generated/source/buildConfig/${sourceSet.name}/${spec.name.decapitalize()}"))
        className.set(
            spec.className
                .or(project, defaultSpec.className)
                .or(project, "${prefix}BuildConfig")
        )
        packageName.set(
            spec.packageName
                .or(project, defaultSpec.packageName)
                .or(project, project.defaultPackage.map { it.replace("[^a-zA-Z._$]".toRegex(), "_") })
        )
        generator.set(
            spec.generator
                .or(project, defaultSpec.generator)
                .or(project, BuildConfigJavaGenerator())
        )

    }.also { spec.generateTask = it }

    private val Project.defaultPackage
        get() = provider {
            group
                .toString()
                .takeUnless { it.isEmpty() }
                ?.let { "$it.${project.name}" }
                ?: project.name
        }

    companion object {

        const val DEFAULT_SOURCE_SET_NAME = "main"

        @Suppress("DeprecatedCallableAddReplaceWith")
        @Deprecated(message = "this should be Gradle's official `orElse`, but it's not available at Gradle 5")
        private fun <T> Provider<T>.or(project: Project, other: Provider<T>) =
            project.provider { orNull ?: other.orNull }

        @Suppress("DeprecatedCallableAddReplaceWith")
        @Deprecated(message = "this should be Gradle's official `orElse`, but it's not available at Gradle 5")
        private fun <T> Provider<T>.or(project: Project, other: T?) =
            project.provider { orNull ?: other }

    }

}
