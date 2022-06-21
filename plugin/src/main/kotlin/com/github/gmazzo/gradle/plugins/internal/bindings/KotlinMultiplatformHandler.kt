package com.github.gmazzo.gradle.plugins.internal.bindings

import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

internal class KotlinMultiplatformHandler(
    private val kotlinHandler: KotlinHandler
) : PluginBindingHandler<Named> by kotlinHandler {

    constructor(
        project: Project,
        extension: BuildConfigExtension
    ) : this(KotlinHandler(project, extension))

    override fun nameOf(sourceSet: Named) = when (val name = kotlinHandler.nameOf(sourceSet)) {
        KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME -> SourceSet.MAIN_SOURCE_SET_NAME
        KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME -> SourceSet.TEST_SOURCE_SET_NAME
        else -> name
    }

}
