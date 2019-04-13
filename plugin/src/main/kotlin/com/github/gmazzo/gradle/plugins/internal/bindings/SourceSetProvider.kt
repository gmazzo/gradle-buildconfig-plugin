package com.github.gmazzo.gradle.plugins.internal.bindings

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet

internal typealias SourceSetProvider = (name: String, onSpec: (BuildConfigClassSpec) -> Unit) -> BuildConfigSourceSet
