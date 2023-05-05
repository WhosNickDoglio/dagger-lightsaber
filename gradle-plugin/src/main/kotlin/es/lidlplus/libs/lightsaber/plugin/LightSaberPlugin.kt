package es.lidlplus.libs.lightsaber.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

class LightSaberPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.apply()
    }
}

private fun Project.apply() {
    val extension = extensions.create("lightsaber", LightSaberPluginExtension::class.java)

    pluginManager.withPlugin("kotlin-kapt") {
        dependencies.add("kapt", "es.lidlplus.lightsaber:lightsaber:0.0.1")
        extensions.configure(KaptExtension::class.java) {
            it.arguments {
                arg(
                    "LightSaber.UnusedBindInstance",
                    extension.unusedBindInstance.convention(Severity.Error).map(Severity::toKapt).get(),
                )
                arg(
                    "LightSaber.UnusedBindsAndProvides",
                    extension.unusedBindsAndProvides.convention(Severity.Error).map(Severity::toKapt).get(),
                )
                arg(
                    "LightSaber.UnusedDependencies",
                    extension.unusedDependencies.convention(Severity.Error).map(Severity::toKapt).get(),
                )
                arg(
                    "LightSaber.UnusedModules",
                    extension.unusedModules.convention(Severity.Error).map(Severity::toKapt).get(),
                )
            }
        }
    }
}

interface LightSaberPluginExtension {
    val unusedBindInstance: Property<Severity>
    val unusedBindsAndProvides: Property<Severity>
    val unusedDependencies: Property<Severity>
    val unusedModules: Property<Severity>
}

enum class Severity {
    Error,
    Warning,
    Ignore,
}

private fun Severity.toKapt() = when (this) {
    Severity.Error -> "error"
    Severity.Warning -> "warning"
    Severity.Ignore -> "ignore"
}
