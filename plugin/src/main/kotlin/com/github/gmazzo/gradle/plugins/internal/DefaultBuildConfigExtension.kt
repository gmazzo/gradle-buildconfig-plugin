package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import com.github.gmazzo.gradle.plugins.BuildConfigLanguage
import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet

internal open class DefaultBuildConfigExtension(
    defaultSourceSet: BuildConfigSourceSet
) : BuildConfigExtension,
    BuildConfigSourceSet by defaultSourceSet {

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

}
