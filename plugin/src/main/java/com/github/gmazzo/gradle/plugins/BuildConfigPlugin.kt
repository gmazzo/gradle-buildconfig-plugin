package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.internal.DefaultBuildConfigExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
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
        val extension = project.extensions.create(
            BuildConfigExtension::class.java,
            "buildConfig",
            DefaultBuildConfigExtension::class.java,
            instantiator
        )

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
    }

}
