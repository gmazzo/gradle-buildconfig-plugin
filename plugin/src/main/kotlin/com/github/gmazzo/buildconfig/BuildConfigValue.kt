package com.github.gmazzo.buildconfig

import java.io.Serializable
import org.gradle.api.provider.Provider

public sealed class BuildConfigValue : Serializable {

    public abstract val value: Serializable?

    init {
        check(value !is BuildConfigValue) { "$value is already a Value" }
        check(value !is Provider<*>) { "$value is a Gradle provider" }
    }

    public data class Literal(override val value: Serializable?) : BuildConfigValue() {

        override fun toString(): String = value.toString()

    }

    public data class Expression(override val value: String) : BuildConfigValue() {

        override fun toString(): String = "expression($value)"

    }

    public data class MultiplatformExpect<Type : Serializable>(
        @Transient val producer: MultiplatformProducer<Type>,
    ) : BuildConfigValue() {

        override val value: Nothing? = null

        override fun toString(): String = "expect <provider>"
    }

    public data class MultiplatformActual(override val value: Serializable?) : BuildConfigValue() {
        override fun toString(): String = "actual $value"
    }

    public fun interface MultiplatformProducer<Type : Serializable> {
        public fun resolveValue(forTarget: String): Type?
    }

}
