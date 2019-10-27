package com.github.gmazzo.gradle.plugins.generators

import com.github.gmazzo.gradle.plugins.BuildConfigTaskSpec
import org.gradle.api.Action
import java.io.Serializable

interface BuildConfigGenerator : Action<BuildConfigTaskSpec>, Serializable
