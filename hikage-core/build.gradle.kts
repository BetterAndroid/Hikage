plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.dokka)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.kotlin.ksp)
}

group = gropify.project.groupName
version = gropify.project.hikage.core.version

android {
    namespace = gropify.project.hikage.core.namespace
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
    lintPublish(projects.hikageCoreLint)

    ksp(projects.hikageCompiler)

    implementation(libs.kavaref.core)
    implementation(libs.kavaref.extension)

    implementation(libs.hiddenapibypass)

    implementation(platform(libs.betterandroid.android.bom))
    api(libs.betterandroid.ui.extension)
    implementation(libs.betterandroid.system.extension)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}