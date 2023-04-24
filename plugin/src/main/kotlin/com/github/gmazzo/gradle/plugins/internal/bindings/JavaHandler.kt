package com.github.gmazzo.gradle.plugins.internal.bindings

import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet
import com.github.gmazzo.gradle.plugins.generators.BuildConfigJavaGenerator
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.the

internal class JavaHandler(
    project: Project,
    private val extension: BuildConfigExtension
) : PluginBindingHandler<SourceSet> {

    override val sourceSets = project.the<SourceSetContainer>()

    override fun nameOf(sourceSet: SourceSet): String = sourceSet.name

    override fun onBind() {
        extension.generator.convention(BuildConfigJavaGenerator())
    }

    override fun onSourceSetAdded(sourceSet: SourceSet, spec: BuildConfigSourceSet) {
        sourceSet.java.srcDir(spec)
    }

}
