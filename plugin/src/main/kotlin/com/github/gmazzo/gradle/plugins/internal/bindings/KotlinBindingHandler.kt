package com.github.gmazzo.gradle.plugins.internal.bindings

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import com.github.gmazzo.gradle.plugins.BuildConfigLanguage
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

internal val Project.kotlinExtension
    get() = extensions.getByType(KotlinProjectExtension::class.java)

internal abstract class KotlinBindingHandler(
    private val hasJavaDependency: Boolean
) : PluginBindingHandler {

    abstract val KotlinSourceSet.compileTaskName: String

    internal val String.taskPrefix
        get() = takeUnless { it.equals("main", ignoreCase = true) }?.capitalize() ?: ""

    override fun invoke(project: Project, extension: BuildConfigExtension, sourceSetProvider: SourceSetProvider) {
        extension.language(BuildConfigLanguage.KOTLIN)

        project.kotlinExtension.sourceSets.all { ss ->
            sourceSetProvider(ss.name) { project.bindSpec(it, ss) }
        }
    }

    private fun Project.bindSpec(spec: BuildConfigClassSpec, sourceSet: KotlinSourceSet) {
        with(spec.generateTask) {
            addGeneratedAnnotation = hasJavaDependency

            sourceSet.kotlin.srcDir(outputDir)

            tasks.getByName(sourceSet.compileTaskName).dependsOn(this)
        }
    }

}
