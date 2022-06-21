package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.github.gmazzo.gradle.plugins.BuildConfigTask
import com.github.gmazzo.gradle.plugins.generators.BuildConfigGenerator
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

internal open class DefaultBuildConfigClassSpec @Inject constructor(
    objects: ObjectFactory,
    providerFactory: ProviderFactory,
    private val name: String
) : BuildConfigClassSpecInternal, ProviderFactory by providerFactory {

    override fun getName() = name

    override val className: Property<String> = objects.property()

    override val packageName: Property<String> = objects.property()

    override val generator: Property<BuildConfigGenerator> = objects.property()

    override val fields = linkedMapOf<String, BuildConfigField>()

    override lateinit var generateTask: TaskProvider<BuildConfigTask>

    override fun buildConfigField(field: BuildConfigField) =
        field.also { fields[it.name] = it }

    override fun buildConfigField(type: String, name: String, value: String) =
        buildConfigField(type, name, provider { value })

    override fun buildConfigField(type: String, name: String, value: Provider<String>) {
        buildConfigField(BuildConfigField(
            type = type.removeSuffix("?"),
            name = name,
            value = value,
            optional = type.endsWith("?")
        ))
    }

}
