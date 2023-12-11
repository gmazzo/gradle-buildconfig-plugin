package com.github.gmazzo.gradle.plugins

import org.gradle.api.Named
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

interface BuildConfigField : Named {

    @Input
    override fun getName(): String

    @get:Input
    val type: Property<String>

    @get:Optional
    @get:Input
    val typeArguments: ListProperty<String>

    @get:Input
    val value: Property<String>

    @get:Deprecated("Indicate nullability directly on the type with a trailing '?'")
    @get:Input
    @get:Optional
    val optional: Property<Boolean>

    @get:Input
    val position: Property<Int>
}
