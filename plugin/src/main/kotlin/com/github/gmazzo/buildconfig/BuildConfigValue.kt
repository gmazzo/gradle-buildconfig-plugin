package com.github.gmazzo.buildconfig

import java.io.Serializable
import org.gradle.api.provider.Provider

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
