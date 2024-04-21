plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.androidx.baselineprofile)
    alias(libs.plugins.kotlin.android)
}

// Best articles about this:
// - [https://stefma.medium.com/sourcecompatibility-targetcompatibility-and-jvm-toolchains-in-gradle-explained-d2c17c8cff7c]
// - [https://developer.android.com/build/jdks#source-compat]
kotlin {
    jvmToolchain(17)
}

android {
    namespace = "ir.erfansn.nsmavpn.macrobenchmark"
    compileSdk = sdk.versions.compile.get().toInt()

    defaultConfig {
        minSdk = 28
        targetSdk = sdk.versions.target.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"

    testOptions {
        managedDevices {
            localDevices {
                register("pixel8Api34") {
                    device = "Pixel 8"
                    apiLevel = 34
                    systemImageSource = "aosp"
                }
            }
        }
    }
}

baselineProfile {
    managedDevices += "pixel8Api34"
    useConnectedDevices = false
}

dependencies {
    implementation(libs.androidx.rules)
    implementation(libs.androidx.junit)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}

androidComponents {
    onVariants(selector().all()) { v ->
        val artifactsLoader = v.artifacts.getBuiltArtifactsLoader()
        v.instrumentationRunnerArguments.put(
            "targetAppId",
            v.testedApks.map { artifactsLoader.load(it)?.applicationId }
        )
    }
}
