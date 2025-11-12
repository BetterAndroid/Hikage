plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.kotlin.ksp)
}

group = gropify.project.groupName
version = gropify.project.hikage.widget.androidx.version

android {
    namespace = gropify.project.hikage.widget.androidx.namespace
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    ksp(projects.hikageCompiler)

    api(libs.androidx.appcompat)
    api(libs.androidx.constraintlayout)
    api(libs.androidx.coordinatorlayout)
    api(libs.androidx.swiperefreshlayout)
    api(libs.androidx.slidingpanelayout)
    api(libs.androidx.drawerlayout)
    api(libs.androidx.cardview)
    api(libs.androidx.viewpager)
    api(libs.androidx.viewpager2)
    api(libs.androidx.recyclerview)

    implementation(projects.hikageCore)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}