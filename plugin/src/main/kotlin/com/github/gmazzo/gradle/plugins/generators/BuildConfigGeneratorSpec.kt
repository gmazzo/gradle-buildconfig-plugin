package com.github.gmazzo.gradle.plugins.generators

import com.github.gmazzo.gradle.plugins.BuildConfigField
import java.io.File

data class BuildConfigGeneratorSpec(
    val className: String,
    val packageName: String,
    val documentation: String?,
    val fields: Collection<BuildConfigField>,
    val outputDir: File,
)
