package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.internal.BuildConfigClassSpecInternal
import com.github.gmazzo.gradle.plugins.internal.BuildConfigSourceSetInternal
import com.github.gmazzo.gradle.plugins.internal.DefaultBuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.internal.DefaultBuildConfigExtension
import com.github.gmazzo.gradle.plugins.internal.DefaultBuildConfigSourceSet
import com.github.gmazzo.gradle.plugins.tasks.BuildConfigTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

class BuildConfigPlugin : Plugin<Project> {

    private val logger = Logging.getLogger(javaClass)

    override fun apply(project: Project) {
        val sourceSets = project.container(BuildConfigSourceSetInternal::class.java) { name ->
            DefaultBuildConfigSourceSet(
                DefaultBuildConfigClassSpec(name),
                project.container(BuildConfigClassSpecInternal::class.java, ::DefaultBuildConfigClassSpec)
            )
        }

        val defaultSS = sourceSets.create("main")

        val extension = project.extensions.create(
            BuildConfigExtension::class.java,
            "buildConfig",
            DefaultBuildConfigExtension::class.java,
            defaultSS
        )

        sourceSets.all {
            configureSourceSet(project, it, defaultSS.classSpec)
        }

        with(project) {
            var taskGraphLocked = false

            gradle.taskGraph.whenReady { taskGraphLocked = true }

            ifKotlin {
                logger.debug("Configuring buildConfig '${BuildConfigLanguage.KOTLIN}' language for $project")

                extension.language(BuildConfigLanguage.KOTLIN)
            }

            plugins.withType(JavaPlugin::class.java) {
                convention.getPlugin(JavaPluginConvention::class.java).sourceSets.all { ss ->
                    with(sourceSets.maybeCreate(ss.name)) {
                        DslObject(ss).convention.plugins[ss.name] = this

                        classSpec.generateTask.bindTo(project, ss)

                        extraSpecs.all {
                            if (taskGraphLocked) {
                                throw IllegalStateException("Can't call 'forClass' after taskGraph was built!")
                            }

                            it.generateTask.bindTo(project, ss)
                        }
                    }
                }
            }
        }
    }

    private fun configureSourceSet(
        project: Project,
        sourceSet: BuildConfigSourceSetInternal,
        defaultSpec: BuildConfigClassSpecInternal
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
        spec: BuildConfigClassSpecInternal,
        defaultSpec: BuildConfigClassSpecInternal,
        descriptionSuffix: String
    ) =
        project.tasks.create("generate${prefix}BuildConfig", BuildConfigTask::class.java).apply {
            group = "BuildConfig"
            description = "Generates the build constants class for $descriptionSuffix"

            fields = spec.fields.lazyValues
            outputDir = project.file("${project.buildDir}/generated/source/buildConfig/$buildDir")

            project.afterEvaluate {
                className = spec.className ?: defaultSpec.className ?: "${prefix}BuildConfig"
                packageName = spec.packageName ?: defaultSpec.packageName ?: "${project.group}.${project.name}"
                    .replace("[^a-zA-Z._$]".toRegex(), "_")
                language = spec.language ?: defaultSpec.language ?: BuildConfigLanguage.JAVA
            }

            spec.generateTask = this
        }

    private fun BuildConfigTask.bindTo(project: Project, javaSourceSet: SourceSet) {
        addGeneratedAnnotation = true

        javaSourceSet.java.srcDir(outputDir)
        project.tasks.getAt(javaSourceSet.compileJavaTaskName).dependsOn(this)

        project.ifKotlin {
            DslObject(javaSourceSet).convention.getPlugin(KotlinSourceSet::class.java).apply {
                kotlin.srcDir(outputDir)
            }
            project.tasks.getAt("compileKotlin").dependsOn(this)
        }
    }

    private fun Project.ifKotlin(action: (AppliedPlugin) -> Unit) =
        pluginManager.withPlugin("org.jetbrains.kotlin.jvm", action)

}
