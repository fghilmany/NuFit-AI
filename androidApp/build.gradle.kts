import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

// Project-local, already gitignored (same file `sdk.dir` lives in -- never create a second
// secrets file when this one already does the job for exactly this use case).
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sentryAndroid)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)
    // Per-app language switching (P-15) -- AppCompatDelegate.setApplicationLocales backport,
    // needed for minSdk 31 (native OS-level per-app language is API 33+ only).
    implementation(libs.androidx.appcompat)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)

    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

android {
    namespace = "com.fghilmany.nufitai"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.fghilmany.nufitai"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Uploads the R8 mapping file on release builds so Sentry can de-obfuscate stack traces --
// without this, isMinifyEnabled = true (androidApp/build.gradle.kts) makes every Sentry crash
// report unreadable. Dry-run (UUID generated, nothing uploaded) until a token is set.
//
// Reads the env var first (CI), then `local.properties` (dev's own machine, project-local,
// already gitignored -- same file sdk.dir lives in). Add this line to local.properties:
//   sentry.authToken=sntryu_xxxxx
// Create a token at https://faris-ghilmany-studio.sentry.io/settings/auth-tokens/ (scope
// project:releases), or `sentry token create faris-ghilmany-studio/<name>` from a real terminal.
sentry {
    org = "faris-ghilmany-studio"
    projectName = "nufit-ai"
    authToken = System.getenv("SENTRY_AUTH_TOKEN")
        ?: localProperties.getProperty("sentry.authToken")
}
