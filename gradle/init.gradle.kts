/**
 * Run with following commands:
 * - Check:
 *  - .\gradlew -I ./gradle/init.gradle.kts spotlessCheck
 *  - .\gradlew -p ./build-logic -I ../gradle/init.gradle.kts spotlessCheck
 *
 * - Apply:
 *  - .\gradlew -I ./gradle/init.gradle.kts spotlessApply
 *  - .\gradlew -p ./build-logic -I ../gradle/init.gradle.kts spotlessApply
 *
 *  or utilize run configurations:
 *  - Check:
 *   - NsmaVPN [spotlessCheck]
 *   - NsmaVPN/BuildLogic [spotlessCheck]
 *
 *  - Apply:
 *   - NsmaVPN [spotlessApply]
 *   - NsmaVPN/BuildLogic [spotlessApply]
 */
initscript {
    val spotlessVersion = "6.25.0"

    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("com.diffplug.spotless:spotless-plugin-gradle:$spotlessVersion")
    }
}

rootProject {
    subprojects {
        apply<com.diffplug.gradle.spotless.SpotlessPlugin>()
        extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
            kotlin {
                target("**/*.kt")
                targetExclude("**/build/**/*.kt")
                licenseHeader(KotlinFileLicenseHeader)
            }
            kotlinGradle {
                target("**/*.kts")
                targetExclude("**/build/**/*.kts")
                // Look for the first line that doesn't have a block comment (assumed to be the license)
                licenseHeader(KotlinFileLicenseHeader, "(^(?![\\/ ]\\*).*$)")
            }
            format("xml") {
                target("**/*.xml")
                targetExclude("**/build/**/*.xml")
                // Look for the first XML tag that isn't a comment (<!--) or the xml declaration (<?xml)
                licenseHeader(XmlFileLicenseHeader, "(<[^!?])")
            }
        }
    }
}

private val OWNER = "Erfan Sn"

private val KotlinFileLicenseHeader = """
    /*
     * Copyright ${"$"}YEAR $OWNER
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *     https://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
""".trimIndent()

private val XmlFileLicenseHeader = """
    <?xml version="1.0" encoding="utf-8"?>
    <!--
      ~ Copyright ${"$"}YEAR $OWNER
      ~
      ~ Licensed under the Apache License, Version 2.0 (the "License");
      ~ you may not use this file except in compliance with the License.
      ~ You may obtain a copy of the License at
      ~
      ~      http://www.apache.org/licenses/LICENSE-2.0
      ~
      ~ Unless required by applicable law or agreed to in writing, software
      ~ distributed under the License is distributed on an "AS IS" BASIS,
      ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      ~ See the License for the specific language governing permissions and
      ~ limitations under the License.
      -->
""".trimIndent()
