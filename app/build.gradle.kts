import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

room{
    schemaDirectory("$projectDir/schemas")
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.isFile) {
        localPropertiesFile.inputStream().use { input -> load(input) }
    }
}

val openWeatherApiKey = providers.gradleProperty("OPENWEATHER_API_KEY")
    .orElse(providers.environmentVariable("OPENWEATHER_API_KEY"))
    .orElse(localProperties.getProperty("OPENWEATHER_API_KEY").orEmpty())
    .getOrElse("")

val releaseStoreFile = localProperties.getProperty("RELEASE_STORE_FILE").orEmpty()
val releaseStorePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD").orEmpty()
val releaseKeyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS").orEmpty()
val releaseKeyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD").orEmpty()
val hasReleaseSigning = releaseStoreFile.isNotEmpty() &&
    releaseStorePassword.isNotEmpty() &&
    releaseKeyAlias.isNotEmpty() &&
    releaseKeyPassword.isNotEmpty()

android {
    namespace = "com.lovely.gweather"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.lovely.gweather"
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "OPENWEATHER_API_KEY", "\"$openWeatherApiKey\"")
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStoreFile)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
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
        buildConfig = true
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
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // Jetpack DataStore (Preferences flavor)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Coroutines and Flow for DataStore
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0") // Use the latest version
    val nav_version = "2.9.6"
    implementation("androidx.navigation:navigation-compose:$nav_version")

    //GMS - Google Mobile Services for location
    implementation("com.google.android.gms:play-services-location:21.0.1")

    //Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.30.1")

    //Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    annotationProcessor(libs.room.compiler)

    //Local Unit Test
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("app.cash.turbine:turbine:1.1.0")



    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Moshi for parsing JSON into Kotlin objects
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")

    // OkHttp Logging Interceptor (optional but VERY useful for debugging)
    // This will let you see the raw API requests and responses in  Logcat
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    //Roboelectric for unit tests
    testImplementation("org.robolectric:robolectric:4.10.3")
    // Use Google's Truth library for readable assertions
    testImplementation("com.google.truth:truth:1.4.2") // Or a newer version


}