package com.github.gmazzo.gradle.plugins.internal.bindings

import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import com.github.gmazzo.gradle.plugins.BuildConfigPlugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

internal class KotlinMultiplatformHandler(
    private val kotlinHandler: KotlinHandler
) : PluginBindingHandler<KotlinSourceSet> by kotlinHandler {

    constructor(
        project: Project,
        extension: BuildConfigExtension
    ) : this(KotlinHandler(project, extension))

    override fun nameOf(sourceSet: KotlinSourceSet) = when (val name = kotlinHandler.nameOf(sourceSet)) {
        KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME -> BuildConfigPlugin.DEFAULT_SOURCE_SET_NAME
        else -> name
    }

}
