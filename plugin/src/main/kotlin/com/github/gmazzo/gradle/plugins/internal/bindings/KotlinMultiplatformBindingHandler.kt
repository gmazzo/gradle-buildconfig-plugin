package com.github.gmazzo.gradle.plugins.internal.bindings

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import com.github.gmazzo.gradle.plugins.BuildConfigLanguage
import com.github.gmazzo.gradle.plugins.BuildConfigPlugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetsContainer

internal object KotlinMultiplatformBindingHandler : PluginBindingHandler {

    override fun invoke(project: Project, extension: BuildConfigExtension, sourceSetProvider: SourceSetProvider) {
        extension.language(BuildConfigLanguage.KOTLIN)

        (project.kotlinExtension as KotlinTargetsContainer).targets.all { target ->
            target.compilations.all { compilation ->
                compilation.allKotlinSourceSets.forEach { ss ->
                    val name = when (ss.name) {
                        KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME -> BuildConfigPlugin.DEFAULT_SOURCE_SET_NAME
                        else -> ss.name
                    }

                    sourceSetProvider(name) { project.bindSpec(compilation, it, ss) }
                }
            }
        }
    }

    private fun Project.bindSpec(
        compilation: KotlinCompilation<*>,
        spec: BuildConfigClassSpec,
        sourceSet: KotlinSourceSet
    ) {
        with(spec.generateTask) {
            sourceSet.kotlin.srcDir(outputDir)

            tasks.getByName(compilation.compileKotlinTaskName).dependsOn(this)
        }
    }

}
