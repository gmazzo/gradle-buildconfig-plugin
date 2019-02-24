package com.github.gmazzo.gradle.plugins

import org.gradle.api.NamedDomainObjectContainer

interface BuildConfigExtension : NamedDomainObjectContainer<BuildConfigSourceSet>, BuildConfigSourceSet {

    fun className(className: String)

    fun packageName(packageName: String)

    fun language(language: String) =
        language(BuildConfigLanguage.valueOf(language.toUpperCase()))

    fun language(language: BuildConfigLanguage)

}
