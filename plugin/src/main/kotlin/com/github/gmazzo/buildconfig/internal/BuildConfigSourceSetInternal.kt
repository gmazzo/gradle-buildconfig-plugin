package com.github.gmazzo.buildconfig.internal

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import com.github.gmazzo.buildconfig.BuildConfigSourceSet
import com.github.gmazzo.buildconfig.BuildConfigTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.TaskProvider

internal interface BuildConfigSourceSetInternal : BuildConfigSourceSet {

    val extraSpecs: NamedDomainObjectContainer<BuildConfigClassSpec>

    val dependsOn: Set<BuildConfigSourceSetInternal>

    val dependents: Set<BuildConfigSourceSetInternal>

    val allDependsOn: Sequence<BuildConfigSourceSetInternal>
        get() = dependsOn.asSequence() + dependsOn.asSequence().flatMap { it.allDependsOn }

    val allDependents: Sequence<BuildConfigSourceSetInternal>
        get() = dependents.asSequence() + dependents.asSequence().flatMap { it.allDependents }

    val isSuperseded: Boolean

    val isKMPTarget: Boolean

    override var generateTask: TaskProvider<BuildConfigTask>

    fun dependsOn(other: BuildConfigSourceSetInternal)

    fun supersededBy(other: BuildConfigSourceSetInternal)

    fun markAsKMPTarget()

}
