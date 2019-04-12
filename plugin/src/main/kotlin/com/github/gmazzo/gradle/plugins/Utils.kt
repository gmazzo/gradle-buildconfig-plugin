package com.github.gmazzo.gradle.plugins

import java.io.Serializable

internal val <T> Map<*, T>.lazyValues: Collection<T>
    get() = let { map ->
        object : Collection<T>, Serializable {

            override val size = map.size

            override fun contains(element: T) = map.containsValue(element)

            override fun containsAll(elements: Collection<T>) = map.values.containsAll(elements)

            override fun isEmpty() = map.isEmpty()

            override fun iterator() = map.values.iterator()

        }
    }
