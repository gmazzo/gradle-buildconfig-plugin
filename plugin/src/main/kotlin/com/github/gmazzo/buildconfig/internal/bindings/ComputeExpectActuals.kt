package com.github.gmazzo.buildconfig.internal.bindings

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import com.github.gmazzo.buildconfig.BuildConfigField
import com.github.gmazzo.buildconfig.internal.BuildConfigExtensionInternal
import com.github.gmazzo.buildconfig.internal.BuildConfigSourceSetInternal
import org.jetbrains.annotations.VisibleForTesting

/**
 * Helper method to extract the complexity out of [KotlinBinder.Multiplatform] for handling expect/actual fields.
 */
internal fun BuildConfigExtensionInternal.computeExpectsActuals() {
    val specsOfTargets = linkedMapOf<BuildConfigClassSpec, Set<BuildConfigClassSpec>>()

    for (targetSpec in sourceSets.filter { it.isKMPTarget }) {
        val targetDependsOn = targetSpec.allDependsOn.filter { !it.isSuperseded }

        val spec = findEffectiveSpec(targetSpec)

        val dependsOn = spec.allDependsOn
            .filter { !it.isSuperseded }
            .filter { it.hasExpects() }
            .toSet()

        if (dependsOn.isNotEmpty()) {
            lookForExpectFields(spec, dependsOn)
            specsOfTargets[spec] = dependsOn
        }

        // find all the extra (traversing the depends on graph) with unique names
        val extras = (sequenceOf(targetSpec) + targetDependsOn)
            .flatMap { spec ->
                val specDependsOn = spec.allDependsOn
                    .filter { !it.isSuperseded }

                spec.extraSpecs.asSequence().map { extra ->
                    extra to specDependsOn
                        .mapNotNull { it.extraSpecs.findByName(extra.name) }
                        .filter { it.hasExpects() }
                        .toSet()
                }
            }
            .distinctBy { (it, _) -> it.name }

        for ((extra, extraDependsOn) in extras) {
            if (extraDependsOn.isNotEmpty()) {
                lookForExpectFields(extra, extraDependsOn)
                specsOfTargets[extra] = extraDependsOn
            }
        }
    }

    // finally, we make sure that all expects with defaults are present in a target (or any of its depends on)
    fillMissingActuals(specsOfTargets)
}

private fun lookForExpectFields(spec: BuildConfigClassSpec, dependsOnSpecs: Set<BuildConfigClassSpec>) {
    val expectSpecs = linkedSetOf<BuildConfigClassSpec>()
    for (field in spec.buildConfigFields) {
        for (dependsOnSpec in dependsOnSpecs) {
            val dependsOnField = dependsOnSpec.buildConfigFields.findByName(field.name) ?: continue

            check(dependsOnField.isExpect) {
                "Field '${dependsOnField.name}' in '$dependsOnSpec' must be `expect`, since it's defined as `actual` in '$spec'"
            }

            field.tags.add(BuildConfigField.IsActual)
            expectSpecs.add(dependsOnSpec)

            // also makes sure that the actual class matches the expect declaration
            spec.defaultsFrom(dependsOnSpec)
        }
    }

    // then, in case we have mixed expect and regular constants in the same spec, we promote them all to this spec
    for (expectSpec in expectSpecs) {
        for (expectField in expectSpec.buildConfigFields) {
            if (expectField.isExpectNoDefault) continue
            if (spec.buildConfigFields.names.contains(expectField.name)) continue

            spec.buildConfigField(expectField).tags.add(BuildConfigField.IsActual)
        }
    }
}

private fun fillMissingActuals(specsOfTargets: Map<BuildConfigClassSpec, Set<BuildConfigClassSpec>>) {
    for ((spec, dependsOnSpecs) in specsOfTargets) {
        if (dependsOnSpecs.any { it.hasActuals() }) continue

        for (expectSpec in dependsOnSpecs) {
            for (expectField in expectSpec.buildConfigFields) {
                if (spec.buildConfigFields.names.contains(expectField.name)) continue
                if (expectField.isExpectNoDefault) continue

                spec.buildConfigField(expectField)
                    .tags.add(BuildConfigField.IsActual)

                // also makes sure that the actual class matches the expect declaration
                spec.defaultsFrom(expectSpec)
            }
        }
    }
}

/**
 * Returns staring from this [spec] and going down on its [BuildConfigSourceSetInternal.dependsOn] graph,
 * the "effective spec" is defined as the first one that:
 * - Its [BuildConfigSourceSetInternal.dependents] (if any) are empty
 * - Has any declared field or depends on directly on a root one
 *
 * If no such spec is found, returns [spec].
 * The returned can be the same as [spec] but never its root one (`main` or `test`)
 */
@VisibleForTesting
internal fun findEffectiveSpec(
    spec: BuildConfigSourceSetInternal,
    selector: (BuildConfigSourceSetInternal) -> BuildConfigClassSpec = { it },
): BuildConfigSourceSetInternal {
    if (selector(spec).hasFields) return spec

    var candidate = spec
    for (dep in spec.allDependsOn.filter { !it.isSuperseded && !it.isRoot }) {
        if (dep.allDependents.any { d -> selector(d).hasFields }) return candidate
        candidate = dep
    }
    return candidate
}

private val BuildConfigSourceSetInternal.isRoot: Boolean
    get() = dependsOn.isEmpty()

private val BuildConfigClassSpec.hasFields: Boolean
    get() = buildConfigFields.isNotEmpty()

private fun BuildConfigClassSpec.hasExpects() =
    buildConfigFields.any { it.isExpect }

private fun BuildConfigClassSpec.hasActuals() =
    buildConfigFields.any { it.isActual }

private val BuildConfigField.isExpect: Boolean
    get() = BuildConfigField.IsExpect in tags.get()

private val BuildConfigField.isExpectNoDefault: Boolean
    get() = isExpect && value.orNull == null

private val BuildConfigField.isActual: Boolean
    get() = BuildConfigField.IsActual in tags.get()
