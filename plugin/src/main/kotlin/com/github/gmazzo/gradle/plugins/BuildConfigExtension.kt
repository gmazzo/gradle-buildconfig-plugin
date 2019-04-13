package com.github.gmazzo.gradle.plugins

import org.gradle.api.NamedDomainObjectContainer

interface BuildConfigExtension : BuildConfigSourceSet {

    val sourceSets: NamedDomainObjectContainer<out BuildConfigSourceSet>

}
