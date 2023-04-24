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
        type: String,
        name: String,
        action: (BuildConfigField) -> Unit,
    ) : NamedDomainObjectProvider<BuildConfigField> =
        buildConfigFields.register(name) {
            it.type.value(type.removeSuffix("?")).disallowChanges()
            it.optional.value(type.endsWith("?")).disallowChanges()
            action(it)
        }

    override fun buildConfigField(type: String, name: String, value: String) =
        buildConfigField(type, name) { it.value.value(value).disallowChanges() }

    override fun buildConfigField(type: String, name: String, value: Provider<String>) =
        buildConfigField(type, name) { it.value.value(value).disallowChanges() }

}
