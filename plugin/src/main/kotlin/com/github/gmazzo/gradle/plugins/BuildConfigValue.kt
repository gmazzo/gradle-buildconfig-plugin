package com.github.gmazzo.gradle.plugins

import org.gradle.api.Named
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import java.io.Serializable
import java.lang.reflect.Type as JavaType

sealed class BuildConfigValue : Serializable {

    abstract val value: Serializable?

    data class Literal(override val value: Serializable?) : BuildConfigValue() {

        init {
            check(value !is BuildConfigValue) { "$value is already a Value" }
            check(value !is Provider<*>) { "$value is a Gradle provider" }
        }

        override fun toString() = value.toString()

    }

    data class Expression(override val value: String) : BuildConfigValue() {

        override fun toString() = "expression($value)"

    }

}
