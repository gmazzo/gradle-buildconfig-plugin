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
import org.gradle.internal.reflect.Instantiator
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import javax.inject.Inject

class BuildConfigPlugin @Inject constructor(
    private val instantiator: Instantiator
) : Plugin<Project> {

    private val logger = Logging.getLogger(javaClass)

    override fun apply(project: Project) {
        val sourceSets = project.container(BuildConfigSourceSet::class.java) { name ->
            DefaultBuildConfigSourceSet(DefaultBuildConfigClassSpec(name), instantiator)
        }

        val defaultSS = sourceSets.create("main") as DefaultBuildConfigSourceSet

        val extension = project.extensions.create(
            BuildConfigExtension::class.java,
            "buildConfig",
            DefaultBuildConfigExtension::class.java,
            defaultSS
        )

        var kotlinDetected = false

        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            logger.debug("Configuring buildConfig '${BuildConfigLanguage.KOTLIN}' language for $project")

            kotlinDetected = true
            extension.language(BuildConfigLanguage.KOTLIN)
        }

        project.plugins.withType(JavaPlugin::class.java) {
            project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.all { ss ->
                createSourceSet(project, sourceSets, defaultSS.classSpec, ss, kotlinDetected)
            }
        }
    }

    private fun createSourceSet(
        project: Project,
        sourceSets: NamedDomainObjectContainer<BuildConfigSourceSet>,
        defaultSpec: DefaultBuildConfigClassSpec,
        javaSourceSet: SourceSet,
        kotlinDetected: Boolean
    ) {
        logger.debug("Creating buildConfig sourceSet '${javaSourceSet.name}' for $project")

        val prefix = javaSourceSet.name.takeUnless { it == "main" }?.capitalize() ?: ""
        val sourceSet = sourceSets.maybeCreate(javaSourceSet.name) as DefaultBuildConfigSourceSet
        DslObject(javaSourceSet).convention.plugins[javaSourceSet.name] = sourceSet

        createGenerateTask(project, prefix, sourceSet.classSpec, defaultSpec, javaSourceSet, kotlinDetected)

        sourceSet.all {
            val childPrefix = prefix + it.name.capitalize()

            createGenerateTask(project, childPrefix, it, defaultSpec, javaSourceSet, kotlinDetected)
        }
    }

    private fun createGenerateTask(
        project: Project,
        prefix: String,
        spec: DefaultBuildConfigClassSpec,
        defaultSpec: DefaultBuildConfigClassSpec,
        javaSourceSet: SourceSet,
        kotlinDetected: Boolean
    ) = project.tasks.create("generate${prefix}BuildConfig", BuildConfigTask::class.java).apply {
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
