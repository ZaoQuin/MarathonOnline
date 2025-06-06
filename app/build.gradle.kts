plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.university.marathononline"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.university.marathononline"
        minSdk = 29
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        dataBinding = true;
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.material.v130alpha03)

    implementation(libs.sdp.android)
    implementation(libs.ssp.android)

    implementation(libs.play.services.location)
    implementation(libs.androidx.fragment.ktx)

    // ViewModel & LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.androidx.viewpager2)

    implementation(libs.circleimageview)
    implementation(libs.lottie)

    implementation(libs.mpandroidchart)

    // Retrofit & GSON
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    implementation(libs.logging.interceptor)

    // Android Navigation Architecture
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.fragment)

    implementation("androidx.datastore:datastore-preferences:1.0.0-alpha01")

    implementation("com.github.1902shubh:SendMail:1.0.0")

    // MOMO
    implementation ("com.github.momo-wallet:mobile-sdk:1.0.7")

    // Glide

    implementation ("com.github.bumptech.glide:glide:4.15.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.0")

    implementation ("com.facebook.shimmer:shimmer:0.5.0")

    // GG Map
    implementation("com.google.android.gms:play-services-maps:19.0.0")

    // Wear OS connectivity
    implementation("com.google.android.gms:play-services-wearable:18.1.0")
    implementation("androidx.wear:wear-remote-interactions:1.0.0")

    // Health Connect (optional, for additional health data)
    implementation("androidx.health.connect:connect-client:1.1.0-alpha07")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.7.3")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))

    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")

    // Firebase Message
    implementation("com.google.firebase:firebase-messaging:23.4.0")

    // Health Connect
    implementation("androidx.health.connect:connect-client:1.1.0-alpha08")
}

buildscript {
    dependencies {
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
    }
}

secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"
    ignoreList.add("keyToIgnore")
    ignoreList.add("sdk.*")
}
