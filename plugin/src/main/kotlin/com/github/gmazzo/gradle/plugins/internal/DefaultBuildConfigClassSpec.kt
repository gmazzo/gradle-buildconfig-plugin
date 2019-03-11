package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.github.gmazzo.gradle.plugins.BuildConfigLanguage

internal open class DefaultBuildConfigClassSpec(
    private val name: String
) : BuildConfigClassSpec {

    var className: String? = null

    var packageName: String? = null

    var language: BuildConfigLanguage? = null

    internal val fields = linkedMapOf<String, BuildConfigField>()

    override fun className(className: String) {
        this.className = className
    }

    override fun packageName(packageName: String) {
        this.packageName = packageName
    }

    override fun language(language: BuildConfigLanguage) {
        this.language = language
    }

    override fun getName() = name

    override fun buildConfigField(field: BuildConfigField) =
        field.also { fields[it.name] = it }

}
