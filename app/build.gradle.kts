import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
}

// Load secrets from secrets.properties file
val secretsFile = rootProject.file("secrets.properties")
val secretsProperties = Properties()
if (secretsFile.exists()) {
    secretsFile.inputStream().use { secretsProperties.load(it) }
} else {
    logger.warn("Warning: secrets.properties file not found! Using default values.")
}

// Function to safely get properties with defaults
fun getSecretProperty(key: String, defaultValue: String): String {
    val value = secretsProperties.getProperty(key, defaultValue)
    // Ensure the value is not empty, use default if it is
    return if (value.isBlank()) defaultValue else value
}

android {
    namespace = "com.example.aisecretary"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.aisecretary"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        
        // Add secrets as BuildConfig fields
        buildConfigField("String", "OLLAMA_BASE_URL", "\"${getSecretProperty("OLLAMA_BASE_URL", "http://localhost:11434")}\"")
        buildConfigField("String", "LLAMA_MODEL_NAME", "\"${getSecretProperty("LLAMA_MODEL_NAME", "llama3:8b")}\"")
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/license.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/notice.txt"
        }
    }
    
    buildFeatures {
        buildConfig = true
    }
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Security dependencies
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Encryption and secure communication
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    // Certificate pinning
    implementation("com.squareup.okhttp3:okhttp-tls:4.12.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    
    // Room
    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")  // Add KTX for Coroutines support
    ksp("androidx.room:room-compiler:2.5.2")
    
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.github.bumptech.glide:glide:4.15.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.2")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation("org.robolectric:robolectric:4.10.3")
    
    // Android Testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
