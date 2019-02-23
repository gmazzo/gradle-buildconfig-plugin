package com.github.gmazzo.gradle.plugins

import org.gradle.api.NamedDomainObjectContainer

interface BuildConfigExtension : NamedDomainObjectContainer<BuildConfigSourceSet>, BuildConfigSourceSet {

    fun language(language: BuildConfigLanguage)

    fun language(language: String) =
        language(BuildConfigLanguage.valueOf(language.toUpperCase()))

}
