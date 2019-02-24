package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.internal.DefaultBuildConfigExtension
import com.github.gmazzo.gradle.plugins.internal.DefaultBuildConfigSourceSet
import com.github.gmazzo.gradle.plugins.tasks.BuildConfigTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.HasConvention
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.internal.reflect.Instantiator
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
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
        ) as DefaultBuildConfigExtension

        var kotlinDetected = false

        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            logger.debug("Configuring buildConfig '${BuildConfigLanguage.KOTLIN}' language for $project")

            kotlinDetected = true
            extension.language(BuildConfigLanguage.KOTLIN)
        }

        project.plugins.withType(JavaPlugin::class.java) {
            project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.all { ss ->
                logger.debug("Creating buildConfig sourceSet '${ss.name}' for $project")

                val prefix = ss.name.takeUnless { it == "main" }?.capitalize() ?: ""
                val sourceSet = extension.create(ss.name) as DefaultBuildConfigSourceSet

                project.tasks.create("generate${prefix}BuildConfig", BuildConfigTask::class.java).apply {
                    fields = sourceSet.fields
                    outputDir = project.file("${project.buildDir}/generated/buildConfig/${ss.name}")

                    ss.java.srcDir(outputDir)
                    project.tasks.getAt(ss.compileJavaTaskName).dependsOn(this)

                    if (kotlinDetected) {
                        (ss as HasConvention).convention.getPlugin(KotlinSourceSet::class.java).apply {
                            kotlin.srcDir(outputDir)
                        }
                        project.tasks.getAt("compileKotlin").dependsOn(this)
                    }

                    project.afterEvaluate {
                        className = extension.className ?: "${prefix}BuildConfig"
                        packageName = extension.packageName ?: project.group.toString()
                        language = extension.language ?: BuildConfigLanguage.JAVA
                    }
                }
            }
        }
    }

}
