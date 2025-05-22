import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("androidx.navigation.safeargs")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.trackify"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.trackify"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    ///Timber
    implementation ("com.jakewharton.timber:timber:5.0.1")

    implementation ("androidx.lifecycle:lifecycle-service:2.8.7")


    //Glide
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    runtimeOnly("com.github.bumptech.glide:compiler:4.16.0")
    //Dagger Android
    implementation ("com.google.dagger:dagger-android:2.55")
    implementation ("com.google.dagger:dagger-android-support:2.55")
    annotationProcessor("com.google.dagger:dagger-android-processor:2.55")


    //Dagger-Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")

    //Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    //LifeCycle
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    //Android Chart
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    //Easy Permission
    implementation ("pub.devrel:easypermissions:3.0.0")

    //Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")

    //Navigation Components
    implementation("androidx.navigation:navigation-fragment:2.8.8")
    implementation("androidx.navigation:navigation-ui:2.8.8")
    ksp("androidx.navigation:navigation-common-ktx:2.8.8")

    //Maps SDK
    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("androidx.core:core-ktx:1.15.0")
    implementation ("androidx.core:core-location-ktx:1.2.0")
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit) // Assuming this is "junit:junit:4.13.2" or similar via version catalog
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("org.mockito:mockito-core:5.10.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3") // Specific test library
    testImplementation("com.google.truth:truth:1.1.5")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}