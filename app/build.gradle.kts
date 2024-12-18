plugins {
  id("com.android.application")
  kotlin("android")
  kotlin("plugin.compose")
  id("com.google.devtools.ksp")
  id("androidx.room")
}

android {
  namespace = "com.robinwersich.todue"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.robinwersich.todue"
    minSdk = 24
    targetSdk = 35
    versionCode = 2
    versionName = "0.2.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables { useSupportLibrary = true }
  }

  buildTypes {
    getByName("release") {
      isDebuggable = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    getByName("debug") { isDebuggable = true }
  }
  compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions { jvmTarget = "1.8" }
  buildFeatures { compose = true }
  packaging { resources { excludes.add("/META-INF/{AL2.0,LGPL2.1}") } }
}

composeCompiler {
  enableStrongSkippingMode = true
  stabilityConfigurationFile = project.layout.projectDirectory.file("compose-stability.conf")
  reportsDestination = project.layout.buildDirectory.dir("reports")
}

room { schemaDirectory(project.layout.projectDirectory.dir("schemas").toString()) }

ksp { arg("room.generateKotlin", "true") }

dependencies {
  // Core Android Libraries
  implementation("androidx.core:core-ktx:1.15.0")
  implementation("androidx.activity:activity-compose:1.9.3")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

  // Compose
  val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
  implementation(composeBom)
  testImplementation(composeBom)
  androidTestImplementation(composeBom)

  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-tooling-preview")
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
  implementation("androidx.compose.foundation:foundation")
  implementation("androidx.compose.material3:material3")

  // Room
  val roomVersion = "2.6.1"
  implementation("androidx.room:room-runtime:$roomVersion")
  ksp("androidx.room:room-compiler:$roomVersion")
  implementation("androidx.room:room-ktx:$roomVersion")

  // Paging
  val pagingVersion = "3.3.5"
  implementation("androidx.paging:paging-runtime:$pagingVersion")
  implementation("androidx.paging:paging-compose:$pagingVersion")

  // Utilities
  implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
  implementation("org.threeten:threeten-extra:1.7.2")

  // Tests
  testImplementation("junit:junit:4.13.2")
  testImplementation("com.google.truth:truth:1.4.4")
  testImplementation("org.jetbrains.kotlin:kotlin-test:2.0.0")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("com.google.truth:truth:1.4.4")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")

  // Desugaring
  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.3")
}
