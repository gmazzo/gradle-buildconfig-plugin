package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import javax.inject.Inject

internal abstract class DefaultBuildConfigClassSpec @Inject constructor() :
    BuildConfigClassSpec,
    GroovyNullValueWorkaround()
