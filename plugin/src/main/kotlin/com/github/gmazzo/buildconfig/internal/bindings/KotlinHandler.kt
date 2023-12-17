package com.github.gmazzo.buildconfig.internal.bindings

import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.github.gmazzo.buildconfig.BuildConfigSourceSet
import com.github.gmazzo.buildconfig.generators.BuildConfigKotlinGenerator
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByName
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetContainer

internal class KotlinHandler(
    project: Project,
    private val extension: BuildConfigExtension
) : PluginBindingHandler<KotlinSourceSet> {

    override val sourceSets =
        project.extensions.getByName<KotlinSourceSetContainer>("kotlin").sourceSets

    override fun nameOf(sourceSet: KotlinSourceSet): String = sourceSet.name

    override fun onBind() {
        extension.generator.convention(BuildConfigKotlinGenerator())
    }

    override fun onSourceSetAdded(sourceSet: KotlinSourceSet, spec: BuildConfigSourceSet) {
        sourceSet.kotlin.srcDir(spec)
    }

}
