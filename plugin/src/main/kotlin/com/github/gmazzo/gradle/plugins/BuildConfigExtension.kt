package com.github.gmazzo.gradle.plugins

interface BuildConfigExtension : BuildConfigSourceSet {

    fun className(className: String)

    fun packageName(packageName: String)

    fun language(language: String) =
        language(BuildConfigLanguage.valueOf(language.toUpperCase()))

    fun language(language: BuildConfigLanguage)

}
