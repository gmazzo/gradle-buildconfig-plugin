package com.github.gmazzo.buildconfig

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property

interface BuildConfigExtension : BuildConfigSourceSet {

    val generateAtSync: Property<Boolean>

    val sourceSets: NamedDomainObjectContainer<out BuildConfigSourceSet>

    fun sourceSets(configure: Action<NamedDomainObjectContainer<out BuildConfigSourceSet>>) =
        configure.execute(sourceSets)

}
