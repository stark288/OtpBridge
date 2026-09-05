plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android { namespace = "com.example.otpbridge"; compileSdk = 36
    defaultConfig { applicationId = "com.example.otpbridge"; minSdk = 26; targetSdk = 36; versionCode = 1; versionName = "1.0" }
}

kotlin { jvmToolchain(17) }

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.3")
    implementation("androidx.work:work-runtime-ktx:2.10.4")
    implementation("com.google.android.gms:play-services-auth:21.4.0")
    implementation("com.google.android.gms:play-services-auth-api-phone:18.2.0")
}
