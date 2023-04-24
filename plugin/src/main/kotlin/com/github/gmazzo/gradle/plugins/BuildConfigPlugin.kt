package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.generators.BuildConfigJavaGenerator
import com.github.gmazzo.gradle.plugins.internal.*
import com.github.gmazzo.gradle.plugins.internal.bindings.PluginBindingHandler
import com.github.gmazzo.gradle.plugins.internal.bindings.PluginBindings
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.domainObjectContainer
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.register

class BuildConfigPlugin : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        val sourceSets =  objects.domainObjectContainer(BuildConfigSourceSetInternal::class) { name ->
            DefaultBuildConfigSourceSet(
                classSpec = objects.newInstance<DefaultBuildConfigClassSpec>(name),
                extraSpecs = project.container(BuildConfigClassSpecInternal::class.java) { extraName ->
                    objects.newInstance<DefaultBuildConfigClassSpec>(extraName)
                }
            )
        }

        val defaultSS = sourceSets.create(SourceSet.MAIN_SOURCE_SET_NAME)

        val extension = extensions.create(
            BuildConfigExtension::class.java,
            "buildConfig",
            DefaultBuildConfigExtension::class.java,
            sourceSets,
            defaultSS,
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
                .orElse(defaultSpec.className)
                .orElse("${prefix}BuildConfig")
        )
        packageName.set(
            spec.packageName
                .orElse(defaultSpec.packageName)
                .orElse(project.defaultPackage.map { it.replace("[^a-zA-Z._$]".toRegex(), "_") })
        )
        generator.set(
            spec.generator
                .orElse(defaultSpec.generator)
                .orElse(BuildConfigJavaGenerator())
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

}
