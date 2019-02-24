val kotlinVersion: String by settings

// TODO this should not be required once Gradle supports variables on 'plugins' closure
pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("org.jetbrains.kotlin.")) {
                useVersion(kotlinVersion)
            }
        }
    }
}
