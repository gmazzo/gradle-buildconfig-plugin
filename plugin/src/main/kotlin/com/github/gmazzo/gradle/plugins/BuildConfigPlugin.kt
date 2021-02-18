package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.generators.BuildConfigJavaGenerator
import com.github.gmazzo.gradle.plugins.internal.*
import com.github.gmazzo.gradle.plugins.internal.bindings.PluginBindingHandler
import com.github.gmazzo.gradle.plugins.internal.bindings.PluginBindings
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.HasConvention
import org.gradle.api.logging.Logging

class BuildConfigPlugin : Plugin<Project> {

    private val logger = Logging.getLogger(javaClass)

    override fun apply(project: Project) = with(project) {
        val sourceSets = container(BuildConfigSourceSetInternal::class.java) { name ->
            DefaultBuildConfigSourceSet(
                classSpec = DefaultBuildConfigClassSpec(name),
                extraSpecs = project.container(BuildConfigClassSpecInternal::class.java) { extraName ->
                    DefaultBuildConfigClassSpec(extraName)
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

        sourceSets.all {
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

        sourceSets.all { ss ->
            val spec = specs.maybeCreate(nameOf(ss))

            onSourceSetAdded(ss, spec)
            spec.extraSpecs.all { onSourceSetAdded(ss, it) }

            if (ss is HasConvention) {
                ss.convention.plugins["buildConfig"] = spec
            }
        }
    }

    private fun configureSourceSet(
        project: Project,
        sourceSet: BuildConfigSourceSetInternal,
        defaultSpec: BuildConfigClassSpecInternal
    ) {
        logger.debug("Creating buildConfig sourceSet '${sourceSet.name}' for $project")

        val prefix = sourceSet.name.takeUnless { it.equals("main", ignoreCase = true) }?.capitalize().orEmpty()

        createGenerateTask(
            project, prefix, sourceSet, sourceSet.classSpec, defaultSpec,
            descriptionSuffix = "'${sourceSet.name}' source"
        )

        sourceSet.extraSpecs.all { subSpec ->
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
    ) =
        project.tasks.create("generate${prefix}BuildConfig", BuildConfigTask::class.java).apply {
            group = "BuildConfig"
            description = "Generates the build constants class for $descriptionSuffix"

            fields = spec.fields.values
            outputDir =
                project.file("${project.buildDir}/generated/source/buildConfig/${sourceSet.name}/${spec.name.decapitalize()}")

            project.afterEvaluate {
                className = spec.className ?: defaultSpec.className ?: "${prefix}BuildConfig"
                packageName = spec.packageName ?: defaultSpec.packageName ?: project.defaultPackage
                    .replace("[^a-zA-Z._$]".toRegex(), "_")
                generator = spec.generator ?: defaultSpec.generator ?: BuildConfigJavaGenerator
            }

            spec.generateTask = this

            doFirst {
                outputDir.deleteRecursively()
            }
        }

    private val Project.defaultPackage
        get() = group
            .toString()
            .takeUnless { it.isEmpty() }
            ?.let { "$it.${project.name}" }
            ?: project.name

    companion object {

        const val DEFAULT_SOURCE_SET_NAME = "main"

    }

}
