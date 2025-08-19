plugins {
    autowire(libs.plugins.android.library)
    autowire(libs.plugins.kotlin.android)
    autowire(libs.plugins.maven.publish)
    autowire(libs.plugins.kotlin.ksp)
}

group = property.project.groupName
version = property.project.hikage.widget.androidx.version

android {
    namespace = property.project.hikage.widget.androidx.namespace
    compileSdk = property.project.android.compileSdk

    defaultConfig {
        minSdk = property.project.android.minSdk
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
    implementation(projects.hikageCore)
    api(androidx.appcompat.appcompat)
    api(androidx.constraintlayout.constraintlayout)
    api(androidx.coordinatorlayout.coordinatorlayout)
    api(androidx.swiperefreshlayout.swiperefreshlayout)
    api(androidx.slidingpanelayout.slidingpanelayout)
    api(androidx.drawerlayout.drawerlayout)
    api(androidx.cardview.cardview)
    api(androidx.viewpager.viewpager)
    api(androidx.viewpager2.viewpager2)
    api(androidx.recyclerview.recyclerview)
    testImplementation(junit.junit)
    androidTestImplementation(androidx.test.ext.junit)
    androidTestImplementation(androidx.test.espresso.espresso.core)
}