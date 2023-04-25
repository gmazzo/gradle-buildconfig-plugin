package com.github.gmazzo.gradle.plugins

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional

interface BuildConfigClassSpec : Named {

    @Input
    override fun getName(): String

    @get:Input
    val className: Property<String>

    @get:Input
    @get:Optional
    val packageName: Property<String>

    @get:Nested
    val buildConfigFields: NamedDomainObjectContainer<BuildConfigField>

    fun className(className: String) = apply {
        this.className.set(className)
    }

    fun packageName(packageName: String) = apply {
        this.packageName.set(packageName)
    }

    fun withoutPackage() = apply {
        packageName("")
    }

    fun buildConfigField(
        type: String,
        name: String,
        value: String,
    ): NamedDomainObjectProvider<BuildConfigField>

    fun buildConfigField(
        type: String,
        name: String,
        value: Provider<String>
    ): NamedDomainObjectProvider<BuildConfigField>

}
