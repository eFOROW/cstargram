plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
}

android {
    namespace = "kr.ac.wsu.cstargram"
    compileSdk = 34

    defaultConfig {
        applicationId = "kr.ac.wsu.cstargram"
        minSdk = 31
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //noinspection UseTomlInstead
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-database")
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-storage")

    //noinspection UseTomlInstead
    implementation ("com.github.bumptech.glide:glide:4.14.2")
    //noinspection UseTomlInstead
    annotationProcessor ("com.github.bumptech.glide:compiler:4.14.2")
}