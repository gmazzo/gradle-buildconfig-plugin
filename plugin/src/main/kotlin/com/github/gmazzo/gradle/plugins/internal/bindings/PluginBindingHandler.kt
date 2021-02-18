package com.github.gmazzo.gradle.plugins.internal.bindings

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import org.gradle.api.DomainObjectCollection

internal interface PluginBindingHandler<SourceSet> {

    val sourceSets: DomainObjectCollection<SourceSet>

    fun nameOf(sourceSet: SourceSet): String

    fun onBind()

    fun onSourceSetAdded(sourceSet: SourceSet, spec: BuildConfigClassSpec)

}
