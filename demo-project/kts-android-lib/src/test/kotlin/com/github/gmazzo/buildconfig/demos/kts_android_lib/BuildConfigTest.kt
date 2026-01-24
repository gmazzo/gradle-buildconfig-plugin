package com.github.gmazzo.buildconfig.demos.kts_android_lib

import com.github.gmazzo.buildconfig.demos.android.lib.BuildConfig as AndroidBuildConfig
import com.github.gmazzo.buildconfig.demos.groovy.BuildConfigBaseTest

class BuildConfigTest : BuildConfigBaseTest() {

    override fun extraCases() = arrayOf(
        // properties cases
        arrayOf("com.github.gmazzo.buildconfig.demos.android.lib", AndroidBuildConfig.LIBRARY_PACKAGE_NAME),

        arrayOf("kts-android-lib", BuildConfig.APP_NAME),
        arrayOf("Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu", BuildConfig.APP_SECRET),
        arrayOf(true, BuildConfig.FEATURE_ENABLED),
        arrayOf(listOf(1, 2, 3, 4), BuildConfig.MAGIC_NUMBERS.toList()),

        // variant cases
        arrayOf(AndroidBuildConfig.BUILD_TYPE, BuildConfig.BUILD_TYPE),
    )

}
