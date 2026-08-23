import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val keystoreProperties=Properties()
val keystoreFile=rootProject.file("keystore.properties")

if(keystoreFile.exists()){
    keystoreProperties.load(
        keystoreFile.inputStream()
    )
}

android {
    namespace="com.lxzrvi.nmix"
    compileSdk=35

    defaultConfig {
        applicationId="com.lxzrvi.nmix"
        minSdk=26
        targetSdk=35

        versionCode=10
        versionName="1.0.10"
    }

    signingConfigs {
        create("release"){
            if(keystoreFile.exists()){
                storeFile=rootProject.file(
                    keystoreProperties.getProperty("storeFile")
                )
                storePassword=
                    keystoreProperties.getProperty("storePassword")
                keyAlias=
                    keystoreProperties.getProperty("keyAlias")
                keyPassword=
                    keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        getByName("release"){
            isMinifyEnabled=false

            if(keystoreFile.exists()){
                signingConfig=
                    signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility=JavaVersion.VERSION_17
        targetCompatibility=JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget="17"
    }

    buildFeatures {
        compose=true
    }
}

dependencies {
    implementation(
        platform(
            "androidx.compose:compose-bom:2024.12.01"
        )
    )

    implementation(
        "androidx.activity:activity-compose:1.10.0"
    )

    implementation(
        "androidx.core:core-ktx:1.15.0"
    )

    implementation(
        "androidx.core:core-splashscreen:1.0.1"
    )

    implementation(
        "androidx.compose.ui:ui"
    )

    implementation(
        "androidx.compose.ui:ui-tooling-preview"
    )

    implementation(
        "androidx.compose.material3:material3"
    )

    debugImplementation(
        "androidx.compose.ui:ui-tooling"
    )
}
