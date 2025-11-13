package com.github.gmazzo.buildconfig.internal

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import com.github.gmazzo.buildconfig.BuildConfigSourceSet
import com.github.gmazzo.buildconfig.BuildConfigTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.TaskProvider

internal interface BuildConfigSourceSetInternal : BuildConfigSourceSet {

    val extraSpecs: NamedDomainObjectContainer<out BuildConfigClassSpec>

    val dependsOn: Set<BuildConfigSourceSetInternal>

    override var generateTask: TaskProvider<BuildConfigTask>

    fun dependsOn(other: BuildConfigSourceSetInternal)

}
