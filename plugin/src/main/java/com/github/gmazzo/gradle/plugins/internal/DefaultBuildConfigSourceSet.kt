package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet
import org.gradle.api.Named

internal class DefaultBuildConfigSourceSet(
    private val name: String
) : BuildConfigSourceSet, Named {

    override fun getName() = name

}
