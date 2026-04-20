import java.util.Properties


plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.appdistribution")
}


android {
    namespace = "com.finorix.signals"
    compileSdk = 34


    defaultConfig {
        applicationId = "com.finorix.signals"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"


        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }
        val openRouterApiKey = properties.getProperty("OPENROUTER_API_KEY") 
            ?: System.getenv("OPENROUTER_API_KEY") 
            ?: ""
        buildConfigField("String", "OPENROUTER_API_KEY", "\"$openRouterApiKey\"")


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )


            // @ts-ignore
            configure<com.google.firebase.appdistribution.gradle.AppDistributionExtension> {
                artifactType = "APK"
                releaseNotesFile = "release-notes.txt"
                groups = "beta-testers"
            }
        }


        debug {
            // @ts-ignore
            configure<com.google.firebase.appdistribution.gradle.AppDistributionExtension> {
                artifactType = "APK"
                releaseNotesFile = "../release-notes.txt"
                testers = "mdf602039@gmail.com"
            }
        }
    }
