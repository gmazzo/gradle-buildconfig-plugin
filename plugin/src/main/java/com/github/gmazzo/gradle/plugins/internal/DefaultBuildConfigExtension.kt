package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import com.github.gmazzo.gradle.plugins.BuildConfigLanguage
import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet
import org.gradle.api.internal.FactoryNamedDomainObjectContainer
import org.gradle.internal.reflect.Instantiator

internal open class DefaultBuildConfigExtension(
    instantiator: Instantiator,
    internal val sharedSourceSet: DefaultBuildConfigSourceSet
) :
    FactoryNamedDomainObjectContainer<BuildConfigSourceSet>(
        BuildConfigSourceSet::class.java,
        instantiator
    ),
    BuildConfigExtension,
    BuildConfigSourceSet by sharedSourceSet {

    var language = BuildConfigLanguage.JAVA

    override fun language(language: BuildConfigLanguage) {
        this.language = language
    }

    override fun doCreate(name: String): BuildConfigSourceSet =
        instantiator.newInstance(DefaultBuildConfigSourceSet::class.java, name)

}
