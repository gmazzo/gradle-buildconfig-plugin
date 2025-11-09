package com.github.gmazzo.buildconfig.generators

import com.github.gmazzo.buildconfig.BuildConfigField
import java.io.File

public data class BuildConfigGeneratorSpec(
    public val className: String,
    public val packageName: String,
    public val documentation: String?,
    public val fields: Collection<BuildConfigField>,
    public val outputDir: File,
)
