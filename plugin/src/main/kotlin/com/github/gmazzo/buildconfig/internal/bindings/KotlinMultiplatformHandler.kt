package com.github.gmazzo.buildconfig.internal.bindings

import org.gradle.api.tasks.SourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

internal class KotlinMultiplatformHandler(
    private val kotlinHandler: KotlinHandler
) : PluginBindingHandler<KotlinSourceSet> by kotlinHandler {

    override fun nameOf(sourceSet: KotlinSourceSet) = when (val name = kotlinHandler.nameOf(sourceSet)) {
        KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME -> SourceSet.MAIN_SOURCE_SET_NAME
        KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME -> SourceSet.TEST_SOURCE_SET_NAME
        else -> name
    }

}
