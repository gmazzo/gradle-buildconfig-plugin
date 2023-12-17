package com.github.gmazzo.buildconfig

import org.gradle.api.Named
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

interface BuildConfigField : Named {

    @Input
    override fun getName(): String

    @get:Input
    val type: Property<BuildConfigType<*>>

    @get:Input
    val value: Property<BuildConfigValue>

    @get:Input
    val position: Property<Int>

}
