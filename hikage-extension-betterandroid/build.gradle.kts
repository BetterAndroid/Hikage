plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.dokka)
    alias(libs.plugins.maven.publish)
}

group = gropify.project.groupName
version = gropify.project.hikage.bom.version

android {
    namespace = gropify.project.hikage.extension.betterandroid.namespace
    compileSdk = gropify.project.android.compileSdk

    defaultConfig {
        minSdk = gropify.project.android.minSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(projects.hikageCore)

    implementation(platform(libs.kavaref.bom))
    implementation(libs.kavaref.core)
    implementation(libs.kavaref.android)
    implementation(libs.kavaref.extension)

    implementation(platform(libs.betterandroid.android.bom))
    implementation(libs.betterandroid.ui.component.adapter)
    implementation(libs.betterandroid.ui.extension)
    implementation(libs.betterandroid.system.extension)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}