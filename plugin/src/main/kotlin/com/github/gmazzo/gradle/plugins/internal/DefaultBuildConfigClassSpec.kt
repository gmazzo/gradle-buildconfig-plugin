package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.github.gmazzo.gradle.plugins.BuildConfigGenerator
import com.github.gmazzo.gradle.plugins.tasks.BuildConfigTask

internal open class DefaultBuildConfigClassSpec(
    private val name: String
) : BuildConfigClassSpecInternal {

    override var className: String? = null

    override var packageName: String? = null

    override var language: BuildConfigGenerator? = null

    override val fields = linkedMapOf<String, BuildConfigField>()

    override lateinit var generateTask: BuildConfigTask

    override fun className(className: String) {
        this.className = className
    }

    override fun packageName(packageName: String) {
        this.packageName = packageName
    }

    override fun language(language: BuildConfigGenerator) {
        this.language = language
    }

    override fun getName() = name

    override fun buildConfigField(field: BuildConfigField) =
        field.also { fields[it.name] = it }

}
