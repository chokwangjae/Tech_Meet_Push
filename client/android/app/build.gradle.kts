plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // push-client 에서 사용.
    alias(push.plugins.ksp)
    alias(push.plugins.google.services)

}

android {
    namespace = "matrix.push.sample"
    compileSdk = 36

    defaultConfig {
        applicationId = "matrix.push.sample"
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
}

dependencies {
    implementation(project(":modules:matrix-commons"))
    implementation(project(":modules:matrix-push-client"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.navigation)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.icons.extended)
    implementation("androidx.compose.foundation:foundation")
    implementation("io.coil-kt:coil-compose:2.7.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)



    // ==== Push Client Dependencies Start ====
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(push.bundles.retrofit)
//    implementation(push.okhttp.sse)     // private push 사용 시 필수.
    implementation(push.coil)
    implementation(push.datastore.preferences)
    implementation(push.bundles.database)
    ksp(push.androidx.room.compiler)

    // Firebase FCM 의존성 (필수) - 개별로 넣을 경우.
//    implementation("com.google.firebase:firebase-messaging:24.1.2")

    // Firebase FCM 의존성 (필수) - Bom 사용 시
    implementation(platform(push.firebase.bom))
    implementation(push.firebase.messaging.ktx)

    implementation(push.shortcut.badger)
    // ==== Push Client Dependencies End ====

}