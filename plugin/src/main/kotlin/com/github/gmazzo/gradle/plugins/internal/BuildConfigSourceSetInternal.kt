package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet
import org.gradle.api.NamedDomainObjectContainer

internal interface BuildConfigSourceSetInternal :
    BuildConfigSourceSet,
    BuildConfigClassSpecInternal {

    val classSpec: BuildConfigClassSpecInternal

    val extraSpecs: NamedDomainObjectContainer<out BuildConfigClassSpecInternal>

}
