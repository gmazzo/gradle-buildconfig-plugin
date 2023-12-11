package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.github.gmazzo.gradle.plugins.FieldType
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.provider.Provider
import javax.inject.Inject

internal abstract class DefaultBuildConfigClassSpec @Inject constructor(
    private val name: String
) : BuildConfigClassSpec {

    override fun getName() = name

    private fun buildConfigField(
        type: FieldType,
        name: String,
        action: (BuildConfigField) -> Unit,
    ): NamedDomainObjectProvider<BuildConfigField> = buildConfigFields.size.let { position ->
        buildConfigFields.register(name) {
            it.type.value(type.rawType).disallowChanges()
            it.typeArguments.value(type.typeArguments).disallowChanges()
            it.position.convention(position)
            action(it)
        }
    }

    override fun buildConfigField(type: FieldType, name: String, value: String) =
        buildConfigField(type, name) { it.value.value(value).disallowChanges() }

    override fun buildConfigField(type: FieldType, name: String, value: Provider<String>) =
        buildConfigField(type, name) { it.value.value(value).disallowChanges() }

}
