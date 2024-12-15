plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.university.marathononline"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.university.marathononline"
        minSdk = 29
        targetSdk = 34
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)

    // ViewModel & LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.androidx.viewpager2)

    implementation(libs.circleimageview)
    implementation(libs.lottie)

    implementation(libs.mpandroidchart)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Retrofit & GSON
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    implementation(libs.logging.interceptor)

    // Android Navigation Architecture
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.fragment)

    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")

    implementation("androidx.datastore:datastore-preferences:1.0.0-alpha01")

    implementation("com.github.1902shubh:SendMail:1.0.0")

    // MOMO
    implementation ("com.github.momo-wallet:mobile-sdk:1.0.7")

    // Glide

    implementation ("com.github.bumptech.glide:glide:4.15.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.0")

    implementation ("com.facebook.shimmer:shimmer:0.5.0")
}