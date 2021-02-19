package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.github.gmazzo.gradle.plugins.BuildConfigTask
import org.gradle.api.tasks.TaskProvider

internal interface BuildConfigClassSpecInternal : BuildConfigClassSpec {

    val fields: Map<String, BuildConfigField>

    override var generateTask: TaskProvider<BuildConfigTask>

}
