package com.github.gmazzo.gradle.plugins.internal.bindings

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import com.github.gmazzo.gradle.plugins.BuildConfigLanguage
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

internal abstract class KotlinBindingHandler : PluginBindingHandler {

    override fun invoke(project: Project, extension: BuildConfigExtension, sourceSetProvider: SourceSetProvider) {
        extension.language(BuildConfigLanguage.KOTLIN)

        project.discoverSourceSets { ss ->
            sourceSetProvider(ss.name) { project.bindSpec(it, ss) }
        }
    }

    abstract fun Project.discoverSourceSets(onSourceSet: (KotlinSourceSet) -> Unit)

    private fun Project.bindSpec(spec: BuildConfigClassSpec, sourceSet: KotlinSourceSet) {
        with(spec.generateTask) {
            sourceSet.kotlin.srcDir(outputDir)

            // TODO find the right task name here!
            tasks.getAt("compileKotlin").dependsOn(this)
        }
    }

}
