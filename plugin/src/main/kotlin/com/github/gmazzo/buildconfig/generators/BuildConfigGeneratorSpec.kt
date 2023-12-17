package com.github.gmazzo.buildconfig.generators

import com.github.gmazzo.buildconfig.BuildConfigField
import java.io.File

data class BuildConfigGeneratorSpec(
    val className: String,
    val packageName: String,
    val documentation: String?,
    val fields: Collection<BuildConfigField>,
    val outputDir: File,
)
