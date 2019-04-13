package com.github.gmazzo.gradle.plugins

import org.gradle.api.Action
import java.io.Serializable

interface BuildConfigGenerator : Action<BuildConfigTaskSpec>, Serializable
