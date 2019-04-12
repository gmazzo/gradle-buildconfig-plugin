package com.github.gmazzo.gradle.plugins

import java.io.Serializable

interface BuildConfigGenerator : (BuildConfigTaskSpec) -> Unit, Serializable
