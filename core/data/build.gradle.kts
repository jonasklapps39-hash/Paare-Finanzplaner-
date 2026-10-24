import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// Verbindungsdaten: local.properties > Umgebungsvariablen > eingecheckte supabase.properties
// (öffentliche Projekt-URL und anon-Key; geschützt wird über Row Level Security).
fun loadProperties(fileName: String) = Properties().apply {
    val file = rootProject.file(fileName)
    if (file.exists()) file.inputStream().use { load(it) }
}

val localProperties = loadProperties("local.properties")
val projectProperties = loadProperties("supabase.properties")

fun secret(name: String): String =
    (localProperties.getProperty(name) ?: System.getenv(name) ?: projectProperties.getProperty(name) ?: "")
        .replace("\"", "")

android {
    namespace = "de.paarfinanz.core.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        buildConfigField("String", "SUPABASE_URL", "\"${secret("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${secret("SUPABASE_ANON_KEY")}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${secret("GOOGLE_WEB_CLIENT_ID")}\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    api(project(":core:domain"))

    api(platform(libs.supabase.bom))
    api(libs.supabase.auth)
    api(libs.supabase.postgrest)
    api(libs.supabase.realtime)
    api(libs.supabase.compose.auth)
    implementation(libs.ktor.client.okhttp)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
}
