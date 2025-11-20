package org.gradle.kotlin.dsl

import com.github.gmazzo.buildconfig.BuildConfigSourceSet
import org.gradle.api.Action
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

public val KotlinSourceSet.buildConfig: BuildConfigSourceSet
    get() = (this as ExtensionAware).extensions.getByName<BuildConfigSourceSet>("buildConfig")

public fun KotlinSourceSet.buildConfig(action: Action<BuildConfigSourceSet>): Unit = action.execute(buildConfig)
