package com.github.gmazzo.gradle.plugins.internal.bindings

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import com.github.gmazzo.gradle.plugins.generators.BuildConfigJavaGenerator
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.the

internal class JavaHandler(
    private val project: Project,
    private val extension: BuildConfigExtension
) : PluginBindingHandler<SourceSet> {

    override val sourceSets: DomainObjectCollection<SourceSet>
        get() = project.the<SourceSetContainer>()

    override fun nameOf(sourceSet: SourceSet): String = sourceSet.name

    override fun onBind() {
        extension.generator.convention(BuildConfigJavaGenerator())
    }

    override fun onSourceSetAdded(sourceSet: SourceSet, spec: BuildConfigClassSpec) {
        sourceSet.java.srcDir(spec.generateTask)
    }

}
