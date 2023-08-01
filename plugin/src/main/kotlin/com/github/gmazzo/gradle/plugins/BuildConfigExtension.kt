package com.github.gmazzo.gradle.plugins

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer

interface BuildConfigExtension : BuildConfigSourceSet {

    val sourceSets: NamedDomainObjectContainer<out BuildConfigSourceSet>

    fun sourceSets(configure: Action<NamedDomainObjectContainer<out BuildConfigSourceSet>>) = configure.execute(sourceSets)

}
