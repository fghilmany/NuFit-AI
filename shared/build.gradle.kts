import io.sentry.kotlin.multiplatform.gradle.AppleDependencyProvider
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
    // tech-stack.md's locked crash-reporting choice -- auto-adds the Sentry KMP dependency to
    // commonMain and wires the per-target native setup (Android + iOS).
    alias(libs.plugins.sentryKotlinMultiplatform)
}

// iOS isn't buildable yet (separate blockers: GifDecoder/TokenStorage.ios.kt, see
// .projectmem/plan.md's open issues) -- the plugin's default AUTO provider tries a SwiftPM
// install of Sentry Cocoa on every build regardless of target, which then resolves the WHOLE
// commonMain dependency graph (incl. coil-gif, which has no iOS/klib variant) against iOS
// cinterop and fails. NONE skips Cocoa install entirely; commonMain's Sentry KMP SDK (used by
// the Android target) is unaffected. Flip back to AUTO once iOS actually builds.
sentryKmp {
    autoInstall.apple.provider.set(AppleDependencyProvider.NONE)
    linker.enabled.set(false)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            // app.cash.sqldelight:native-driver uses cinterop bindings to the system SQLite
            // library -- Xcode doesn't auto-link that transitively, so it's declared here.
            linkerOpts.add("-lsqlite3")
        }
    }

    android {
       namespace = "com.fghilmany.nufitai.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()

       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.ktor.client.okhttp)
            // issue #83 follow-up -- real Google Sign-In via Credential Manager (ADR-006).
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services.auth)
            implementation(libs.googleid)
            // P-15 "Ganti Bahasa" -- LocaleController.android.kt uses AppCompatDelegate/LocaleListCompat.
            // Also transitively supplies androidx.core (NotificationCompat/NotificationManagerCompat/
            // ContextCompat, used by MealReminderScheduler.android.kt) -- the version catalog's own
            // `androidx-core-ktx` entry (1.19.0) requires compileSdk 37/AGP 9.1.0, too new for this
            // project's compileSdk 36/AGP 9.0.1, so it's deliberately NOT declared explicitly here.
            implementation(libs.androidx.appcompat)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines.extensions)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kermit)
            implementation(libs.multiplatform.settings)
            implementation(libs.navigation.compose)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinxJson)
            implementation(libs.ktor.client.logging)
            // Exercise Library data-source pivot (ADR-007) -- bundled photo/GIF rendering.
            implementation(libs.coil.compose)
            implementation(libs.coil.gif)
            // P-15 Google profile photo -- URL-based image load (coil-compose/coil-gif alone
            // never registered a network fetcher).
            implementation(libs.coil.network.ktor3)
            // Declared explicitly even though the Sentry KMP Gradle plugin auto-adds it too --
            // every other dependency in this project goes through the version catalog explicitly
            // (ADR-pattern), Gradle just dedupes the duplicate.
            implementation(libs.sentry.kotlin.multiplatform)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
            implementation(libs.turbine)
            implementation(libs.kotlinx.coroutines.test)
        }
        getByName("androidHostTest").dependencies {
            // JVM-only: in-memory SQLite driver for DiVerificationTest, and MockK per testing.md
            // ("MockK is JVM-only: usable in androidUnitTest/JVM source sets").
            implementation(libs.sqldelight.sqlite.driver)
            implementation(libs.mockk)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

sqldelight {
    databases {
        create("NuFitDatabase") {
            packageName.set("com.fghilmany.nufitai.db")
        }
    }
}
