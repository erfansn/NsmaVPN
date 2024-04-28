/*
 * Copyright 2024 Erfan Sn
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
package detekt

import ProjectPlugin
import com.android.build.gradle.internal.tasks.factory.dependsOn
import de.undercouch.gradle.tasks.download.Download
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import unsafeLibs

abstract class DetektConventionsPlugin : ProjectPlugin {

    override fun Project.with() {
        with(pluginManager) {
            apply("io.gitlab.arturbosch.detekt")
            apply("de.undercouch.download")
            apply("com.android.application")
        }

        val detektComposeRulesVersion = unsafeLibs.findVersion("detektComposeRulesVersion").get().toString()
        tasks.register<Download>("downloadDetektComposeRulesJarFile") {
            group = "Detekt Compose Rules"
            description = "Download the Detekt Compose rules jar file"

            src("https://github.com/mrmans0n/compose-rules/releases/download/v$detektComposeRulesVersion/detekt-compose-$detektComposeRulesVersion-all.jar")
            dest("$rootDir/detekt/detekt-compose-$detektComposeRulesVersion-all.jar")
            overwrite(false)
        }
        tasks.register<DetektComposeRulesConfigTask>("writeDetektComposeRulesIdeaConfig") {
            group = "Detekt Compose Rules"
            description = "Config Detekt Compose rules to Idea if needed"

            ruleVersion.set(detektComposeRulesVersion)
            dependsOn("downloadDetektComposeRulesJarFile")
        }
        tasks.named("detekt").dependsOn(tasks.named("writeDetektComposeRulesIdeaConfig"))
        tasks.named("preBuild").dependsOn(tasks.named("detekt"))

        extensions.getByType<DetektExtension>().apply {
            config.setFrom("$rootDir/detekt/config.yml")
        }

        dependencies {
            add("detektPlugins", unsafeLibs.findLibrary("detekt-compose-rules").get())
        }
    }
}