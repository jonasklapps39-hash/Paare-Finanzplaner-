import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

val appVersionName = "0.2.0"

// Verbindungsdaten: local.properties > Umgebungsvariablen > eingecheckte supabase.properties
// (öffentliche Projekt-URL und anon-Key; geschützt wird über Row Level Security).
fun loadProperties(fileName: String) = Properties().apply {
    val file = rootProject.file(fileName)
    if (file.exists()) file.inputStream().use { load(it) }
}

val localProperties = loadProperties("local.properties")
val projectProperties = loadProperties("supabase.properties")

fun config(name: String): String =
    (localProperties.getProperty(name) ?: System.getenv(name) ?: projectProperties.getProperty(name) ?: "")
        .replace("\"", "")

// Erzeugt AppConfig.kt für alle Plattformen (Ersatz für Androids BuildConfig).
val appConfigDir = layout.buildDirectory.dir("generated/appConfig/commonMain/kotlin")
val generateAppConfig by tasks.registering {
    val values = mapOf(
        "SUPABASE_URL" to config("SUPABASE_URL"),
        "SUPABASE_ANON_KEY" to config("SUPABASE_ANON_KEY"),
        "GOOGLE_WEB_CLIENT_ID" to config("GOOGLE_WEB_CLIENT_ID"),
        "VERSION_NAME" to appVersionName,
    )
    inputs.properties(values)
    val outputDir = appConfigDir
    outputs.dir(outputDir)
    doLast {
        val file = outputDir.get().file("de/paarfinanz/core/data/AppConfig.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            buildString {
                appendLine("package de.paarfinanz.core.data")
                appendLine()
                appendLine("// Automatisch erzeugt von :shared:generateAppConfig – nicht von Hand ändern.")
                appendLine("internal object AppConfig {")
                values.forEach { (key, value) -> appendLine("    const val $key: String = \"$value\"") }
                appendLine("}")
            },
        )
    }
}

tasks.withType<KotlinCompilationTask<*>>().configureEach { dependsOn(generateAppConfig) }

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(appConfigDir)
            dependencies {
                api(project(":core:domain"))

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(compose.components.resources)

                implementation(libs.lifecycle.viewmodel.compose)
                implementation(libs.lifecycle.runtime.compose)
                implementation(libs.navigation.compose)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)

                implementation(project.dependencies.platform(libs.supabase.bom))
                implementation(libs.supabase.auth)
                implementation(libs.supabase.postgrest)
                implementation(libs.supabase.realtime)

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.qrose)
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.supabase.compose.auth)
            implementation(libs.koin.android)
            implementation(libs.play.services.code.scanner)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "de.paarfinanz.shared"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

compose.resources {
    packageOfResClass = "de.paarfinanz.shared.resources"
}
