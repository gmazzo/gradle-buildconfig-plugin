package com.github.gmazzo.gradle.plugins

import java.io.Serializable

data class BuildConfigField(
    val type: String,
    val name: String,
    val value: String
) : Serializable
