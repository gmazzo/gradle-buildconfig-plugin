package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.internal.DefaultBuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.internal.DefaultBuildConfigExtension
import com.github.gmazzo.gradle.plugins.internal.DefaultBuildConfigSourceSet
import com.github.gmazzo.gradle.plugins.tasks.BuildConfigTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

class BuildConfigPlugin : Plugin<Project> {

    private val logger = Logging.getLogger(javaClass)

    override fun apply(project: Project) {
        val sourceSets = project.container(BuildConfigSourceSet::class.java) { name ->
            DefaultBuildConfigSourceSet(
                DefaultBuildConfigClassSpec(name),
                project.container(DefaultBuildConfigClassSpec::class.java)
            )
        }

        val defaultSS = sourceSets.create("main") as DefaultBuildConfigSourceSet

        val extension = project.extensions.create(
            BuildConfigExtension::class.java,
            "buildConfig",
            DefaultBuildConfigExtension::class.java,
            defaultSS
        )

        sourceSets.all {
            configureSourceSet(project, it as DefaultBuildConfigSourceSet, defaultSS.classSpec)
        }

        with(project) {
            var kotlinDetected = false
            var taskGraphLocked = false

            gradle.taskGraph.whenReady { taskGraphLocked = true }

            plugins.withId("org.jetbrains.kotlin.jvm") {
                logger.debug("Configuring buildConfig '${BuildConfigLanguage.KOTLIN}' language for $project")

                kotlinDetected = true
                extension.language(BuildConfigLanguage.KOTLIN)
            }

            plugins.withType(JavaPlugin::class.java) {
                convention.getPlugin(JavaPluginConvention::class.java).sourceSets.all { ss ->
                    with(sourceSets.maybeCreate(ss.name) as DefaultBuildConfigSourceSet) {
                        DslObject(ss).convention.plugins[ss.name] = this

                        classSpec.task.bindTo(ss, kotlinDetected)

                        extraSpecs.all {
                            if (taskGraphLocked) {
                                throw IllegalStateException("Can't call 'forClass' after taskGraph was built!")
                            }

                            it.task.bindTo(ss, kotlinDetected)
                        }
                    }
                }
            }
        }
    }

    private fun configureSourceSet(
        project: Project,
        sourceSet: DefaultBuildConfigSourceSet,
        defaultSpec: DefaultBuildConfigClassSpec
    ) {
        logger.debug("Creating buildConfig sourceSet '${sourceSet.name}' for $project")

        val buildDir = sourceSet.name
        val prefix = buildDir.takeUnless { it.equals("main", ignoreCase = true) }?.capitalize() ?: ""

        createGenerateTask(
            project, prefix, buildDir, sourceSet.classSpec, defaultSpec,
            descriptionSuffix = "'${sourceSet.name}' source"
        )

        sourceSet.extraSpecs.all {
            val childPrefix = prefix + it.name.capitalize()

            createGenerateTask(
                project, childPrefix, buildDir, it, defaultSpec,
                descriptionSuffix = "'${it.name}' spec on '${sourceSet.name}' source"
            )
        }
    }

    private fun createGenerateTask(
        project: Project,
        prefix: String,
        buildDir: String,
        spec: DefaultBuildConfigClassSpec,
        defaultSpec: DefaultBuildConfigClassSpec,
        descriptionSuffix: String
    ) =
        project.tasks.create("generate${prefix}BuildConfig", BuildConfigTask::class.java).apply {
            group = "BuildConfig"
            description = "Generates the build constants class for $descriptionSuffix"

            fields = spec.fields.lazyValues
            outputDir = project.file("${project.buildDir}/generated/source/buildConfig/$buildDir")

            project.afterEvaluate {
                className = spec.className ?: defaultSpec.className ?: "${prefix}BuildConfig"
                packageName = spec.packageName ?: defaultSpec.packageName ?: project.group.toString()
                language = spec.language ?: defaultSpec.language ?: BuildConfigLanguage.JAVA
            }

            spec.task = this
        }

    private fun BuildConfigTask.bindTo(javaSourceSet: SourceSet, kotlinDetected: Boolean) {
        addGeneratedAnnotation = true

        javaSourceSet.java.srcDir(outputDir)
        project.tasks.getAt(javaSourceSet.compileJavaTaskName).dependsOn(this)

        if (kotlinDetected) {
            DslObject(javaSourceSet).convention.getPlugin(KotlinSourceSet::class.java).apply {
                kotlin.srcDir(outputDir)
            }
            project.tasks.getAt("compileKotlin").dependsOn(this)
        }
    }

}
