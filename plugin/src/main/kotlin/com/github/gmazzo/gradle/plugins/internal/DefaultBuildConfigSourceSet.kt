package com.github.gmazzo.gradle.plugins.internal

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.BuildConfigTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

internal abstract class DefaultBuildConfigSourceSet(
    override val classSpec: BuildConfigClassSpec,
    override val extraSpecs: NamedDomainObjectContainer<out BuildConfigClassSpec>
) :
    BuildConfigSourceSetInternal,
    BuildConfigClassSpec by classSpec,
    Iterable<TaskProvider<BuildConfigTask>> {

    @Inject
    constructor(
        name: String,
        objects: ObjectFactory,
    ) : this(
        classSpec = objects.newInstance<BuildConfigClassSpec>(name),
        extraSpecs = objects.domainObjectContainer(BuildConfigClassSpec::class.java) { extraName ->
            objects.newInstance<BuildConfigClassSpec>(extraName)
        }
    )

    override lateinit var generateTask: TaskProvider<BuildConfigTask>

    override fun forClass(packageName: String?, className: String): BuildConfigClassSpec =
        extraSpecs.maybeCreate(className).also {
            it.className.value(className)
            it.packageName.value(packageName).convention(this.packageName)
        }

    override fun iterator() = iterator { yield(generateTask) }

}
