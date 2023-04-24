package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet
import com.github.gmazzo.gradle.plugins.BuildConfigTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.TaskProvider

internal interface BuildConfigSourceSetInternal : BuildConfigSourceSet{

    val classSpec: BuildConfigClassSpec

    val extraSpecs: NamedDomainObjectContainer<out BuildConfigClassSpec>

    override var generateTask: TaskProvider<BuildConfigTask>

}
