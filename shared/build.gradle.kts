import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.room)
}

kotlin {
    jvm()

    // Room KMP uses expect/actual objects (ShowtimeDatabaseCtor); this is the
    // officially recommended flag to silence the (Beta) expect/actual-class warning.
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidLibrary {
       namespace = "com.showtime.app.shared"
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
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)

            // Showtime: Android-only engine + Koin Android
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Showtime: lifecycle base ViewModel (for the MVI Store) + Navigation
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.navigation.compose)

            // Showtime: Ktor + Ktorfit
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinxJson)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktorfit.lib)

            // Showtime: Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeViewmodel)

            // Showtime: Coil
            implementation(libs.coil.compose)
            implementation(libs.coil.networkKtor)

            // Showtime: Room + SQLite bundled driver
            implementation(libs.room.runtime)
            implementation(libs.room.paging)
            implementation(libs.sqlite.bundled)

            // Showtime: Paging
            implementation(libs.paging.common)
            implementation(libs.paging.compose)

            // Showtime: DataStore
            implementation(libs.datastore.preferences)

            // Showtime: Kotlinx
            implementation(libs.kotlinx.serializationJson)
            implementation(libs.kotlinx.coroutinesCore)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            // Showtime: JVM/Desktop engine + Swing main dispatcher
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

// Several JVM tests open a real DataStore/Room DB over a fixed file path. DataStore forbids
// two active instances for the same file within one process, so give each test class a fresh
// JVM (the instances are never explicitly closed, which is fine for short integration tests).
tasks.withType<Test>().configureEach {
    forkEvery = 1
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)

    // Room compiler must run for BOTH targets via KSP.
    // NOTE: under the new `com.android.kotlin.multiplatform.library` model the
    // Android KSP processor configuration is `kspAndroid` (for the androidMain
    // source set); the JVM one is `kspJvm`.
    add("kspAndroid", libs.room.compiler)
    add("kspJvm", libs.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}
