package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet

internal open class DefaultBuildConfigSourceSet(
    private val name: String
) : BuildConfigSourceSet {

    internal val fields = linkedMapOf<String, BuildConfigField>()

    override fun getName() = name

    override fun buildConfigField(field: BuildConfigField) =
        field.also { fields[it.name] = it }


}
