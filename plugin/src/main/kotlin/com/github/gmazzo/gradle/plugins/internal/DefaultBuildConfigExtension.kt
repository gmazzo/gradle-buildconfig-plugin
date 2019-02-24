package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.github.gmazzo.gradle.plugins.BuildConfigLanguage
import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet
import org.gradle.api.internal.FactoryNamedDomainObjectContainer
import org.gradle.internal.reflect.Instantiator

internal open class DefaultBuildConfigExtension(
    instantiator: Instantiator
) :
    FactoryNamedDomainObjectContainer<BuildConfigSourceSet>(
        BuildConfigSourceSet::class.java,
        instantiator
    ),
    BuildConfigExtension {

    private val default get() = maybeCreate("main")

    var className: String? = null

    var packageName: String? = null

    var language: BuildConfigLanguage? = null

    override fun className(className: String) {
        this.className = className
    }

    override fun packageName(packageName: String) {
        this.packageName = packageName
    }

    override fun language(language: BuildConfigLanguage) {
        this.language = language
    }

    override fun buildConfigField(field: BuildConfigField) =
        default.buildConfigField(field)

    override fun getName(): String =
        default.name

    override fun doCreate(name: String): BuildConfigSourceSet =
        instantiator.newInstance(DefaultBuildConfigSourceSet::class.java, name)

}
