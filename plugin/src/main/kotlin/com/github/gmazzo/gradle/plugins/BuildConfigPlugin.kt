package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.internal.DefaultBuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.internal.DefaultBuildConfigExtension
import com.github.gmazzo.gradle.plugins.internal.DefaultBuildConfigSourceSet
import com.github.gmazzo.gradle.plugins.tasks.BuildConfigTask
import org.gradle.api.NamedDomainObjectContainer
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

        val args = ApplyArgs(project, sourceSets, defaultSS.classSpec)

        with(project) {
            gradle.taskGraph.whenReady { args.taskGraphLocked = true }

            plugins.withId("org.jetbrains.kotlin.jvm") {
                logger.debug("Configuring buildConfig '${BuildConfigLanguage.KOTLIN}' language for $project")

                args.kotlinDetected = true
                extension.language(BuildConfigLanguage.KOTLIN)
            }

            plugins.withType(JavaPlugin::class.java) {
                convention.getPlugin(JavaPluginConvention::class.java).sourceSets.all { ss ->
                    createSourceSet(args, ss)
                }
            }
        }
    }

    private fun createSourceSet(args: ApplyArgs, javaSourceSet: SourceSet) = with(args) {
        logger.debug("Creating buildConfig sourceSet '${javaSourceSet.name}' for $project")

        val prefix = javaSourceSet.name.takeUnless { it == "main" }?.capitalize() ?: ""
        val sourceSet = sourceSets.maybeCreate(javaSourceSet.name) as DefaultBuildConfigSourceSet
        DslObject(javaSourceSet).convention.plugins[javaSourceSet.name] = sourceSet

        createGenerateTask(this, prefix, sourceSet.classSpec, javaSourceSet, "'${sourceSet.name}' source")

        sourceSet.extraSpecs.all {
            if (args.taskGraphLocked) {
                throw IllegalStateException("Can't call 'forClass' after taskGraph was built!")
            }

            val childPrefix = prefix + it.name.capitalize()

            createGenerateTask(this, childPrefix, it, javaSourceSet, "'${it.name}' spec on '${sourceSet.name}' source")
        }
    }

    private fun createGenerateTask(
        args: ApplyArgs,
        prefix: String,
        spec: DefaultBuildConfigClassSpec,
        javaSourceSet: SourceSet,
        descriptionSuffix: String
    ) = with(args) {
        project.tasks.create("generate${prefix}BuildConfig", BuildConfigTask::class.java).apply {
            group = "BuildConfig"
            description = "Generates the build constants class for $descriptionSuffix"

            fields = spec.fields.lazyValues
            outputDir = project.file("${project.buildDir}/generated/source/buildConfig/${javaSourceSet.name}")

            javaSourceSet.java.srcDir(outputDir)
            project.tasks.getAt(javaSourceSet.compileJavaTaskName).dependsOn(this)

            if (kotlinDetected) {
                DslObject(javaSourceSet).convention.getPlugin(KotlinSourceSet::class.java).apply {
                    kotlin.srcDir(outputDir)
                }
                project.tasks.getAt("compileKotlin").dependsOn(this)
            }

            project.afterEvaluate {
                className = spec.className ?: defaultSpec.className ?: "${prefix}BuildConfig"
                packageName = spec.packageName ?: defaultSpec.packageName ?: project.group.toString()
                language = spec.language ?: defaultSpec.language ?: BuildConfigLanguage.JAVA
            }
        }
    }

    private data class ApplyArgs(
        val project: Project,
        val sourceSets: NamedDomainObjectContainer<BuildConfigSourceSet>,
        val defaultSpec: DefaultBuildConfigClassSpec,
        var kotlinDetected: Boolean = false,
        var taskGraphLocked: Boolean = false
    )

}
