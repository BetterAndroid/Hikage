plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hikage)
}

android {
    namespace = gropify.project.samples.demo.android.packageName
    testNamespace = gropify.project.samples.demo.android.testPackageName

    compileSdk = gropify.project.android.compileSdk

    defaultConfig {
        applicationId = gropify.project.samples.demo.android.packageName
        minSdk = gropify.project.android.minSdk
        targetSdk = gropify.project.android.targetSdk
        versionName = gropify.project.samples.demo.android.versionName
        versionCode = gropify.project.samples.demo.android.versionCode
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(projects.hikageCore)
    implementation(projects.hikageRuntime)
    implementation(projects.hikageRuntimeAttribute)
    implementation(projects.hikageExtension)
    implementation(projects.hikageExtensionBetterandroid)
    implementation(projects.hikageWidgetAndroidx)
    implementation(projects.hikageWidgetMaterial)

    implementation(platform(libs.betterandroid.android.bom))
    implementation(libs.betterandroid.ui.component)
    implementation(libs.betterandroid.ui.extension)
    implementation(libs.betterandroid.system.extension)

    implementation(libs.pangutext.android)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.slidingpanelayout)
    implementation(libs.androidx.drawerlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.viewpager)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.recyclerview)
    implementation(libs.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.material)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}