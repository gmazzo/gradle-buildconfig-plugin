package com.github.gmazzo.gradle.plugins

import org.gradle.api.Named
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

interface BuildConfigField : Named {

    @Input
    override fun getName(): String

    @get:Input
    val type: Property<String>

    @get:Input
    val value: Property<String>

    @get:Input
    val optional: Property<Boolean>

}
