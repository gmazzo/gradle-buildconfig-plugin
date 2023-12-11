package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.BuildConfigField
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.provider.Provider
import javax.inject.Inject

internal abstract class DefaultBuildConfigClassSpec @Inject constructor(
    private val name: String
) : BuildConfigClassSpec {

    override fun getName() = name

    private fun buildConfigField(
        type: BuildConfigField.Type,
        name: String,
        action: (BuildConfigField) -> Unit,
    ): NamedDomainObjectProvider<BuildConfigField> = buildConfigFields.size.let { position ->
        buildConfigFields.register(name) {
            it.type.value(type).disallowChanges()
            it.position.convention(position)
            action(it)
        }
    }

    override fun buildConfigField(type: BuildConfigField.Type, name: String, value: BuildConfigField.Value) =
        buildConfigField(type, name) { it.value.value(value).disallowChanges() }

    override fun buildConfigField(type: BuildConfigField.Type, name: String, value: Provider<BuildConfigField.Value>) =
        buildConfigField(type, name) { it.value.value(value).disallowChanges() }

}
