package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigExtension

internal open class DefaultBuildConfigExtension(
    defaultSourceSet: BuildConfigSourceSetInternal
) : BuildConfigExtension,
    BuildConfigSourceSetInternal by defaultSourceSet