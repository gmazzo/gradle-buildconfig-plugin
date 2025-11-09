package com.github.gmazzo.buildconfig

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property

public interface BuildConfigExtension : BuildConfigSourceSet {

    public val generateAtSync: Property<Boolean>

    public val sourceSets: NamedDomainObjectContainer<out BuildConfigSourceSet>

    public fun sourceSets(configure: Action<NamedDomainObjectContainer<out BuildConfigSourceSet>>): Unit =
        configure.execute(sourceSets)

}
