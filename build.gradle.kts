import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

// Top-level build file where you can add configuration options common to all sub-projects/modules.
allprojects {
    apply(from = "${rootProject.projectDir}/repositories.gradle.kts")
    apply(plugin = "com.github.ben-manes.versions")
    tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
        val regex = listOf(
            "alpha", "beta", "rc", "cr", "m", "preview","a", "b", "ea", "jre"
        ).map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-+]*") }
        resolutionStrategy {
            componentSelection {
                all {
                    val rejected = regex.any {
                        it.matches(candidate.version)
                    } && regex.all {
                        !it.matches(
                            currentVersion
                        )
                    }
                    if (rejected) {
                        reject("Release candidate")
                    }
                }
            }
        }
        // optional parameters
        checkForGradleUpdate = false
        outputFormatter = "json"
        outputDir = "build/dependencyUpdates"
        reportfileName = "report"
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

subprojects {
    // skip uploading the mapping to Crashlytics
    tasks.configureEach {
        if (name.contains("uploadCrashlyticsMappingFile")) enabled = false
    }
}
