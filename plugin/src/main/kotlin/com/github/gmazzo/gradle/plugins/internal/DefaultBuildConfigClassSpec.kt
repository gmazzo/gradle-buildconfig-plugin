package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.github.gmazzo.gradle.plugins.BuildConfigTask
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.TaskProvider
import javax.inject.Inject

internal abstract class DefaultBuildConfigClassSpec @Inject constructor(
    private val providerFactory: ProviderFactory,
    private val name: String
) : BuildConfigClassSpecInternal {

    override fun getName() = name

    override val fields = linkedMapOf<String, BuildConfigField>()

    override lateinit var generateTask: TaskProvider<BuildConfigTask>

    override fun buildConfigField(field: BuildConfigField) =
        field.also { fields[it.name] = it }

    override fun buildConfigField(type: String, name: String, value: String) =
        buildConfigField(type, name, providerFactory.provider { value })

    override fun buildConfigField(type: String, name: String, value: Provider<String>) {
        buildConfigField(BuildConfigField(
            type = type.removeSuffix("?"),
            name = name,
            value = value,
            optional = type.endsWith("?")
        ))
    }

}
