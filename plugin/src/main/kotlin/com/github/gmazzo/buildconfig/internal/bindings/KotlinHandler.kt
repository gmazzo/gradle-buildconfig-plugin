package com.github.gmazzo.buildconfig.internal.bindings

import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.github.gmazzo.buildconfig.BuildConfigSourceSet
import com.github.gmazzo.buildconfig.generators.BuildConfigKotlinGenerator
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

    // sourceSet.kotlin.srcDir(spec)
    override fun onSourceSetAdded(sourceSet: Named, spec: BuildConfigSourceSet) {
        (sourceSet.javaClass.getMethod("getKotlin")
            .invoke(sourceSet) as SourceDirectorySet)
            .srcDir(spec)
    }

}
