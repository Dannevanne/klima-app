plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "no.uio.ifi.in2000.team45"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "no.uio.ifi.in2000.team45"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    //ktor
    implementation("io.ktor:ktor-client-core:3.4.0")
    implementation("io.ktor:ktor-client-cio:3.4.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.4.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.0")

    //coil
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    //mapbox
    implementation("com.mapbox.maps:android-ndk27:11.20.1")
    implementation("com.mapbox.extension:maps-compose-ndk27:11.20.1")

    //datastore
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    //locationPermission
    implementation("com.google.accompanist:accompanist-permissions:0.35.0-alpha")
    // mockk for testing
    testImplementation("io.mockk:mockk:1.14.9")
    // coroutines testing
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")

    implementation("com.mapbox.search:autofill-ndk27:2.21.0")
    implementation("com.mapbox.search:discover-ndk27:2.21.0")
    implementation("com.mapbox.search:place-autocomplete-ndk27:2.21.0")
    implementation("com.mapbox.search:offline-ndk27:2.21.0")
    implementation("com.mapbox.search:mapbox-search-android-ndk27:2.21.0")
    implementation("com.mapbox.search:mapbox-search-android-ui-ndk27:2.21.0")

    // Appcompat
    implementation("androidx.appcompat:appcompat:1.7.1")
}