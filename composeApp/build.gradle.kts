import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type


plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
    id("com.codingfeline.buildkonfig") version "0.17.1"
}

buildkonfig {
    packageName = "com.plcoding.cmp_memecreator"

    defaultConfigs {
        val localProperties = Properties()

        val localPropertiesFile = rootProject.file("local.properties")

        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use {
                localProperties.load(it)
            }
        } else {
            println("WARNING: local.properties file not found at ${localPropertiesFile.absolutePath}. Ensure your API_KEY is set via environment variable or in CI.")
        }

        val apiKey = localProperties.getProperty("API_KEY")
        val apiUrl = localProperties.getProperty("API_URL")

        require(!apiKey.isNullOrEmpty()) {
            "Register your api key from developer and place it in local.properties as `API_KEY`"
        }

        buildConfigField(Type.STRING, "API_KEY", apiKey)
        buildConfigField(Type.STRING, "API_URL", apiUrl)
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            implementation(libs.bundles.koin.android)

            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.material3.adaptive)
            implementation(libs.material3.adaptive.layout)
            implementation(libs.compose.ui.backhandler)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.coil.compose)
            implementation(libs.jetbrains.compose.navigation)

            implementation(libs.bundles.compose.ui)
            implementation(libs.bundles.koin.common)
            implementation(libs.bundles.androidx.lifecycle)

            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.postgrest.kt)
            implementation(libs.realtime.kt)
            implementation(libs.ktor.client.core)

            implementation(libs.kotlinx.datetime)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.plcoding.cmp_memecreator"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.plcoding.cmp_memecreator"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 3
        versionName = "2"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

