package org.gradle.kotlin.dsl

import com.android.build.api.dsl.AndroidSourceSet
import com.github.gmazzo.buildconfig.BuildConfigSourceSet
import org.gradle.api.Action
import org.gradle.api.plugins.ExtensionAware

public val AndroidSourceSet.buildConfig: BuildConfigSourceSet
    get() = (this as ExtensionAware).extensions.getByName<BuildConfigSourceSet>("buildConfig")

public fun AndroidSourceSet.buildConfig(action: Action<BuildConfigSourceSet>): Unit = action.execute(buildConfig)
