package com.github.gmazzo.gradle.plugins.internal.bindings

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

internal class KotlinHandler(
    private val project: Project,
    private val extension: BuildConfigExtension
) : PluginBindingHandler<KotlinSourceSet> {

    override val sourceSets: DomainObjectCollection<KotlinSourceSet>
        get() = project.extensions.getByType(KotlinProjectExtension::class.java).sourceSets

    override fun nameOf(sourceSet: KotlinSourceSet): String = sourceSet.name

    override fun onBind() {
        extension.useKotlinOutput()
    }

    override fun onSourceSetAdded(sourceSet: KotlinSourceSet, spec: BuildConfigClassSpec) {
        sourceSet.kotlin.srcDir(spec.generateTask)
    }

}
