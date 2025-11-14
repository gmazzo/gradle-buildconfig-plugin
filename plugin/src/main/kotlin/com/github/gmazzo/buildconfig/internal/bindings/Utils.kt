package com.github.gmazzo.buildconfig.internal.bindings

import com.github.gmazzo.buildconfig.BuildConfigClassSpec

internal fun BuildConfigClassSpec.defaultsFrom(other: BuildConfigClassSpec) {
    generator.convention(other.generator)
    className.convention(other.className)
    packageName.convention(other.packageName)
    documentation.convention(other.documentation)
}
