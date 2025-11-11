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

    public data class Expect(override val value: Serializable? = NoDefault) : BuildConfigValue() {

        override fun toString(): String = "expect" + (if (value !is NoDefault) " (defaultsTo=$value)" else "")

    }

    internal data object NoDefault : Serializable {
        @Suppress("unused")
        private fun readResolve(): Any = NoDefault
    }

}
