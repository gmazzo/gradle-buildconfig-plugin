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
    val collectionType: Property<CollectionType>

    @get:Input
    val value: Property<String>

    @get:Input
    val optional: Property<Boolean>

    @get:Input
    val position: Property<Int>

    fun asList() = apply {
        collectionType.set(CollectionType.LIST)
    }

    fun asSet() = apply {
        collectionType.set(CollectionType.SET)
    }

    fun asCollection() = apply {
        collectionType.set(CollectionType.COLLECTION)
    }

    enum class CollectionType {
        NONE, COLLECTION, LIST, SET
    }
}
