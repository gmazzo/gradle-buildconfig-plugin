package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet
import org.gradle.api.internal.DefaultNamedDomainObjectSet
import org.gradle.internal.reflect.Instantiator

internal open class DefaultBuildConfigExtension(instantiator: Instantiator) :
    BuildConfigExtension,
    DefaultNamedDomainObjectSet<BuildConfigSourceSet>(DefaultBuildConfigSourceSet::class.java, instantiator)
