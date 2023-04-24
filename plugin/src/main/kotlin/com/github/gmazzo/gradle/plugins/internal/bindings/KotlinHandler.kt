package com.github.gmazzo.gradle.plugins.internal.bindings

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import com.github.gmazzo.gradle.plugins.generators.BuildConfigKotlinGenerator
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet

internal class KotlinHandler(
    project: Project,
    private val extension: BuildConfigExtension
) : PluginBindingHandler<Named> { // TODO should be KotlinSourceSet but fails on tests (but not from external project)

    // project.extensions.getByName<KotlinSourceSetContainer>("kotlin").sourceSets
    override val sourceSets = with(project.extensions.getByName("kotlin")) {
        @Suppress("UNCHECKED_CAST")
        javaClass.getMethod("getSourceSets")
            .invoke(this) as NamedDomainObjectContainer<Named>
    }

    override fun nameOf(sourceSet: Named): String = sourceSet.name

    override fun onBind() {
        extension.generator.convention(BuildConfigKotlinGenerator())
    }

    // sourceSet.kotlin.srcDir(spec.generateTask)
    override fun onSourceSetAdded(sourceSet: Named, spec: BuildConfigClassSpec) {
        (sourceSet.javaClass.getMethod("getKotlin")
            .invoke(sourceSet) as SourceDirectorySet)
            .srcDir(spec.generateTask)
    }

}
