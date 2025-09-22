plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val moduleName = "matrix-commons"

android {
    namespace = "matrix.commons"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        version = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
    buildTypes {
        getByName("debug") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(client.kotlinx.coroutines.core)
    implementation(client.kotlinx.coroutines.android)
    implementation(client.parser.gson)

    implementation(client.datastore.preferences)
}


tasks.register("jar") {
    group = "matrix-modules"
    dependsOn("build")

    doLast {
        copy {
            from("build/intermediates/aar_main_jar/release/syncReleaseLibJars/")
            into("${rootDir}/outputs/")
            include("classes.jar")
            rename("classes.jar", "${moduleName}-${version}.jar")
        }
    }
}