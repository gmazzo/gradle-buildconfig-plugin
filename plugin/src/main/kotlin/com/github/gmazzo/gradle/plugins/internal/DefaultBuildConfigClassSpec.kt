package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.github.gmazzo.gradle.plugins.BuildConfigTask
import com.github.gmazzo.gradle.plugins.generators.BuildConfigGenerator
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider

internal open class DefaultBuildConfigClassSpec(
    objects: ObjectFactory,
    private val name: String
) : BuildConfigClassSpecInternal {

    override fun getName() = name

    override val className: Property<String> = objects.property(String::class.java)

    override val packageName: Property<String> = objects.property(String::class.java)

    override val generator: Property<BuildConfigGenerator> = objects.property(BuildConfigGenerator::class.java)

    override val fields = linkedMapOf<String, BuildConfigField>()

    override lateinit var generateTask: TaskProvider<BuildConfigTask>

    override fun buildConfigField(field: BuildConfigField) =
        field.also { fields[it.name] = it }

}
