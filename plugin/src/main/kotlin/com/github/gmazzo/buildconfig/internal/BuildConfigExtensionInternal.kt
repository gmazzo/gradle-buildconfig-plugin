package com.github.gmazzo.buildconfig.internal

import com.github.gmazzo.buildconfig.BuildConfigExtension
import org.gradle.api.NamedDomainObjectContainer

internal interface BuildConfigExtensionInternal : BuildConfigExtension {

    override val sourceSets: NamedDomainObjectContainer<out BuildConfigSourceSetInternal>

}
