package com.github.gmazzo.gradle.plugins.internal.bindings

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import com.github.gmazzo.gradle.plugins.generators.BuildConfigKotlinGenerator
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

internal class KotlinHandler(
    private val project: Project,
    private val extension: BuildConfigExtension
) : PluginBindingHandler<KotlinSourceSet> {

    override val sourceSets: DomainObjectCollection<KotlinSourceSet>
        get() = project.the<KotlinProjectExtension>().sourceSets

    override fun nameOf(sourceSet: KotlinSourceSet): String = sourceSet.name

    override fun onBind() {
        extension.generator.convention(BuildConfigKotlinGenerator())
    }

    override fun onSourceSetAdded(sourceSet: KotlinSourceSet, spec: BuildConfigClassSpec) {
        sourceSet.kotlin.srcDir(spec.generateTask)
    }

}
