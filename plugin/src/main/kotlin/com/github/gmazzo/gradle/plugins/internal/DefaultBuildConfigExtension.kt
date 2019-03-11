package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet

internal open class DefaultBuildConfigExtension(
    defaultSourceSet: BuildConfigSourceSet
) : BuildConfigExtension,
    BuildConfigSourceSet by defaultSourceSet