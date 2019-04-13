package com.github.gmazzo.gradle.plugins.internal.bindings

import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import org.gradle.api.Project

internal typealias PluginBindingHandler =
            (project: Project, extension: BuildConfigExtension, sourceSetProvider: SourceSetProvider) -> Unit
