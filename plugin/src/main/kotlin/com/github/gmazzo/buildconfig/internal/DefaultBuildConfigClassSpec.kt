package com.github.gmazzo.buildconfig.internal

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import javax.inject.Inject

internal abstract class DefaultBuildConfigClassSpec @Inject constructor() :
    BuildConfigClassSpec,
    GroovyNullValueWorkaround()
