package com.github.gmazzo.gradle.plugins.internal.bindings

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import org.gradle.api.Project
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet

internal object JavaBindingHandler : PluginBindingHandler {

    override fun invoke(project: Project, extension: BuildConfigExtension, sourceSetProvider: SourceSetProvider) {
        extension.useJavaOutput()

        project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.all { ss ->
            DslObject(ss).convention.plugins["buildConfig"] = sourceSetProvider(ss.name) { project.bindSpec(it, ss) }
        }
    }

    private fun Project.bindSpec(spec: BuildConfigClassSpec, sourceSet: SourceSet) {
        with(spec.generateTask) {
            sourceSet.java.srcDir(outputDir)
            tasks.getAt(sourceSet.compileJavaTaskName).dependsOn(this)
        }
    }

}
