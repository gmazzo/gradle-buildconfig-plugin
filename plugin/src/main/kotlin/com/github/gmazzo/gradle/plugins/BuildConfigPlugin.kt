package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.internal.DefaultBuildConfigExtension
import com.github.gmazzo.gradle.plugins.internal.DefaultBuildConfigSourceSet
import com.github.gmazzo.gradle.plugins.tasks.BuildConfigTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.internal.reflect.Instantiator
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
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

        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            logger.debug("Configuring buildConfig '${BuildConfigLanguage.KOTLIN}' language for $project")

            extension.language(BuildConfigLanguage.KOTLIN)
        }

        project.plugins.withType(JavaPlugin::class.java) {
            project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.all { ss ->
                logger.debug("Creating buildConfig sourceSet '${ss.name}' for $project")

                createBuildConfigTask(
                    project,
                    extension,
                    ss
                ).apply {
                    ss.java.srcDir(outputDir)
                    project.tasks.getAt(ss.compileJavaTaskName).dependsOn(this)
                }
            }
        }
    }

    private fun createBuildConfigTask(
        project: Project,
        extension: DefaultBuildConfigExtension,
        sourceSet: SourceSet
    ): BuildConfigTask {
        val prefix = sourceSet.name.takeUnless { it == "main" }?.capitalize() ?: ""
        val bcss = extension.create(sourceSet.name) as DefaultBuildConfigSourceSet

        return project.tasks.create("generate${prefix}BuildConfig", BuildConfigTask::class.java).apply {
            className = extension.className ?: "${prefix}BuildConfig"
            packageName = extension.packageName ?: project.group.toString()
            language = extension.language ?: BuildConfigLanguage.JAVA
            fields = bcss.fields
            outputDir = project.file("${project.buildDir}/generated/buildConfig/${sourceSet.name}")

            when (language) {
                BuildConfigLanguage.JAVA -> {
                    sourceSet.java.srcDir(outputDir)
                    project.tasks.getAt(sourceSet.compileJavaTaskName).dependsOn(this)
                }
                BuildConfigLanguage.KOTLIN -> {
                    val kss = project.extensions
                        .getByType(KotlinProjectExtension::class.java)
                        .sourceSets
                        .getAt(sourceSet.name)

                    kss.kotlin.srcDir(outputDir)
                    project.tasks.getAt("compileKotlin").dependsOn(this)
                }
            }
        }
    }

}
