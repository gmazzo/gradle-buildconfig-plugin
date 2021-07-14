package com.github.gmazzo.gradle.plugins

import org.gradle.api.provider.Provider
import java.io.Serializable

data class BuildConfigField(
    val type: String,
    val name: String,
    val value: Provider<String>
) : Serializable
