import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.parcelize)

    alias(client.plugins.dokka)
}

val moduleName = "matrix-push-client"

android {
    namespace = "matrix.push.client"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        val sdkVersion = generateVersion(
            isStable = false,
            majorVersionCode = "0"
        )
        version = sdkVersion
        
        // BuildConfig에 버전 정보 추가
        buildConfigField("String", "SDK_VERSION", "\"$sdkVersion\"")
    }

    buildFeatures {
        buildConfig = true
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
    implementation(project(":modules:matrix-commons"))

    implementation(client.kotlinx.coroutines.core)
    implementation(client.kotlinx.coroutines.android)

    implementation(client.retrofit)
    implementation(client.converter.gson)
    implementation(client.converter.scalars)
    implementation(client.okhttp.urlconnection)
    debugImplementation(client.okhttp.logging.interceptor)
    implementation(client.okhttp.sse)

    // fcm 을 위한 Push
    implementation(platform(client.firebase.bom))
    implementation(client.firebase.messaging.ktx)

    // image library
    implementation(client.coil)

    // database
    ksp(client.androidx.room.compiler)
    implementation(client.androidx.room.ktx)
    implementation(client.androidx.room.runtime)

    // preference
    implementation(client.datastore.preferences)

    // badge - 기기 제조사별 스펙이 고려된 library.
    implementation(client.shortcut.badger)

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

/**
 * 버전명에서 사용할 format 에 맞는 현재 날짜를 가져온다.
 * @param format    결과를 받을 format.
 * @return          포맷팅된 현재 날짜 문자열
 */
fun getCurrentDate(format: String): String {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern(format, Locale.getDefault())
    return currentDateTime.format(formatter)
}

/**
 * 버전을 생성한다.
 * @param isStable      true: 안정화 버전, false: 개발 버전.
 * @param majorVersionCode  major 버전 코드
 * @param middleVersionCode middle 버전 코드
 * @param minorVersionCode  minor 버전 코드.
 * @return              생성 된 버전.
 */
fun generateVersion(
    isStable: Boolean,
    majorVersionCode: String = "1",
    middleVersionCode: String = "0",
    minorVersionCode: String = "1"
): String {
    println(":generateVersion isStable: $isStable, versionCode: $minorVersionCode")
    val header = "${majorVersionCode}.${middleVersionCode}.${getCurrentDate("yyMMdd.HHmmss")}"

    return if (isStable) {
        "$header.$minorVersionCode"
    } else {
        "${header}.${minorVersionCode}_dev"
    }
}
