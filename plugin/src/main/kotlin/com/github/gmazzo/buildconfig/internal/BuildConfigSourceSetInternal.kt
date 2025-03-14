package com.github.gmazzo.buildconfig.internal

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import com.github.gmazzo.buildconfig.BuildConfigSourceSet
import com.github.gmazzo.buildconfig.BuildConfigTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.TaskProvider

internal interface BuildConfigSourceSetInternal : BuildConfigSourceSet {

    val classSpec: BuildConfigClassSpec

    val extraSpecs: NamedDomainObjectContainer<out BuildConfigClassSpec>

    override var generateTask: TaskProvider<BuildConfigTask>

}
