package com.github.gmazzo.gradle.plugins.internal.bindings

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet

internal class JavaHandler(
    private val project: Project,
    private val extension: BuildConfigExtension
) : PluginBindingHandler<SourceSet> {

    override val sourceSets: DomainObjectCollection<SourceSet>
        get() = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets

    override fun nameOf(sourceSet: SourceSet): String = sourceSet.name

    override fun onBind() {
        extension.useJavaOutput()
    }

    override fun onSourceSetAdded(sourceSet: SourceSet, spec: BuildConfigClassSpec) {
        sourceSet.java.srcDir(spec.generateTask)
    }

}
