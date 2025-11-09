package com.github.gmazzo.buildconfig

import java.io.Serializable
import org.gradle.api.provider.Provider

public sealed class BuildConfigValue : Serializable {

    public abstract val value: Serializable?

    public data class Literal(override val value: Serializable?) : BuildConfigValue() {

        init {
            check(value !is BuildConfigValue) { "$value is already a Value" }
            check(value !is Provider<*>) { "$value is a Gradle provider" }
        }

        override fun toString(): String = value.toString()

    }

    public data class Expression(override val value: String) : BuildConfigValue() {

        override fun toString(): String = "expression($value)"

    }

    data object Expect : BuildConfigValue() {

        @Suppress("unused")
        private fun readResolve(): Any = Expect

        override val value = null

    }

}
