import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Move build outputs outside OneDrive on Windows — OneDrive sync corrupts/locks
// files (KSP-generated dirs, packaged manifests) mid-build. Mirrors the same
// pattern used in CreditCardApp.
run {
    val override = System.getenv("ANDROID_BUILD_DIR")
        ?: if (System.getProperty("os.name").startsWith("Windows", ignoreCase = true)
            && file("C:/CCBuild").exists()
        ) "C:/CCBuild/r1garage-app" else null
    if (override != null) layout.buildDirectory.set(file(override))
}

// Read signing configuration from local.properties (dev) or env vars (CI).
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun localProp(name: String, default: String = "") =
    (localProps.getProperty(name) ?: System.getenv(name) ?: default)
        .trim()
        .trim('"', '\'')

android {
    namespace = "com.r1garage.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.r1garage.android"
        minSdk = 26
        targetSdk = 36
        versionCode = (System.getenv("RELEASE_VERSION_CODE") ?: "1").toInt()
        versionName = System.getenv("RELEASE_VERSION_NAME") ?: "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    // Stable upgrade keystore: only created when SIGNING_STORE_PASSWORD is
    // provided (via local.properties or CI env). When absent (fork PRs,
    // fresh clones), the release buildType falls back to AGP's debug key
    // so the project still builds.
    signingConfigs {
        val storePwd = localProp("SIGNING_STORE_PASSWORD")
        if (storePwd.isNotEmpty()) {
            create("upgrade") {
                storeFile = file(localProp("SIGNING_STORE_FILE", "upgrade.keystore"))
                storePassword = storePwd
                keyAlias = localProp("SIGNING_KEY_ALIAS", "upgrade")
                keyPassword = localProp("SIGNING_KEY_PASSWORD")
                storeType = "PKCS12"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.findByName("upgrade")
                ?: signingConfigs.getByName("debug")
        }
        debug {
            isMinifyEnabled = false
            signingConfigs.findByName("upgrade")?.let { signingConfig = it }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.navigation)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    implementation(libs.work.runtime.ktx)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(libs.datastore.preferences)
    implementation(libs.security.crypto)

    testImplementation(libs.junit)
}
