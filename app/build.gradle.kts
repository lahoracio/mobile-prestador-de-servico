plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.exemple.facilita"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.exemple.facilita"
        minSdk = 26
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/license.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/notice.txt"
            excludes += "/META-INF/ASL2.0"
            excludes += "/META-INF/*.kotlin_module"
            excludes += "/META-INF/io.netty.versions.properties"
        }
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
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.play.services.location)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.firebase.appdistribution.gradle)
    implementation(libs.androidx.ui)
    implementation(libs.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)


    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    //COIL biblioteca para fazer chamada das img
    implementation("io.coil-kt:coil-compose:2.7.0")


    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation("com.google.android.gms:play-services-location:21.1.0")
    implementation("com.google.accompanist:accompanist-permissions:0.30.1")
    implementation("com.google.android.libraries.places:places:3.5.0")

    implementation("com.google.maps.android:maps-compose:4.3.3")

    // Socket.IO para chat em tempo real
    implementation("io.socket:socket.io-client:2.1.0") {
        exclude(group = "org.json", module = "json")
    }

    // Socket.IO para WebSocket
    implementation("io.socket:socket.io-client:2.1.0")

    // Google Maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.maps.android:android-maps-utils:3.8.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // Google Directions API
    implementation("com.google.maps:google-maps-services:2.2.0")
    implementation("org.slf4j:slf4j-simple:1.7.36")

    implementation("com.airbnb.android:lottie-compose:6.3.0")

    //Web RTC
    implementation("io.github.webrtc-sdk:android:125.6422.04")

    // Permissões
    implementation("com.google.accompanist:accompanist-permissions:0.31.5-beta")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

}

