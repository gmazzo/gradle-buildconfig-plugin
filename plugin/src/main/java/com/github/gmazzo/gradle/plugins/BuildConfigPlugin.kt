package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.internal.DefaultBuildConfigExtension
import com.github.gmazzo.gradle.plugins.internal.DefaultBuildConfigSourceSet
import com.github.gmazzo.gradle.plugins.tasks.BuildConfigTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.CompositeDomainObjectSet
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.internal.reflect.Instantiator
import javax.inject.Inject

class BuildConfigPlugin @Inject constructor(
    private val instantiator: Instantiator
) : Plugin<Project> {

    private val logger = Logging.getLogger(javaClass)

    override fun apply(project: Project) {
        val sharedSourceSet = DefaultBuildConfigSourceSet("")

        val extension = project.extensions.create(
            BuildConfigExtension::class.java,
            "buildConfig",
            DefaultBuildConfigExtension::class.java,
            instantiator,
            sharedSourceSet
        ) as DefaultBuildConfigExtension

        project.plugins.withType(JavaPlugin::class.java) {
            project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.all { ss ->
                logger.debug("Creating buildConfig sourceSet '${ss.name}' for $project")

                extension.create(ss.name)
            }
        }

        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            logger.debug("Configuring buildConfig '${BuildConfigLanguage.KOTLIN}' language for $project")

            extension.language(BuildConfigLanguage.KOTLIN)
        }

        extension.all {
            val task = createBuildConfigTask(project, extension, it as DefaultBuildConfigSourceSet)


        }
    }

    private fun createBuildConfigTask(
        project: Project,
        extension: DefaultBuildConfigExtension,
        sourceSet: DefaultBuildConfigSourceSet
    ): BuildConfigTask {
        val prefix = sourceSet.name.takeUnless { it == "main" } ?: ""

        return project.tasks.create("generate${prefix}BuildConfig", BuildConfigTask::class.java) {
            it.packageName = project.group.toString()
            it.fields = CompositeDomainObjectSet.create(
                BuildConfigField::class.java,
                extension.sharedSourceSet.fields,
                sourceSet.fields
            )
            it.language = extension.language
            it.outputDir = project.file("${project.buildDir}/generated/buildConfig/${sourceSet.name}")
        }
    }

}
