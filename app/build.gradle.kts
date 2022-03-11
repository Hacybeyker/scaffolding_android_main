plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
    id("scabbard.gradle") version "0.5.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
    id("io.gitlab.arturbosch.detekt") version "1.18.1"
}

apply {
    from("sonarqube.gradle")
    from("jacoco.gradle")
}

android {
    compileSdk = AppVersion.compileSdkVersion
    buildToolsVersion = AppVersion.buildToolsVersion

    defaultConfig {
        applicationId = ConfigureApp.applicationId
        minSdk = AppVersion.minSdkVersion
        targetSdk = AppVersion.targetSdkVersion
        versionCode = ConfigureApp.versionCode
        versionName = ConfigureApp.version
        testInstrumentationRunner = AppVersion.testInstrumentationRunner
        renderscriptSupportModeEnabled = true
        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    buildTypes {
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", ConstantsApp.Release.BASE_URL)
        }
        create("qa") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", ConstantsApp.Debug.BASE_URL)
        }
        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", ConstantsApp.Debug.BASE_URL)
        }
    }

    externalNativeBuild {
        ndkBuild {
            path("src/main/jni/Android.mk")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    bundle {
        language {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }

    lint {
        disable.addAll(listOf("TypographyFractions", "TypographyQuotes"))
        checkDependencies = true
        abortOnError = false
        ignoreWarnings = false
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    detekt {
        buildUponDefaultConfig = true
        allRules = true
        config = files("$projectDir/config/detekt.yml")
        reports {
            html.enabled = true
            xml.enabled = true
            txt.enabled = false
            sarif.enabled = false
        }
    }

    scabbard {
        enabled = true
    }
}

dependencies {
    implementation(fileTree("libs") { include(listOf("*.jar", "*.aar")) })
    implementation(AppDependencies.kotlinStdlib)
    implementation(AppDependencies.coreKtx)
    // View
    implementation(AppDependencies.appCompat)
    implementation(AppDependencies.material)
    implementation(AppDependencies.constraintLayout)
    implementation(AppDependencies.viewPager2)
    // Hilt
    implementation(AppDependencies.hilt)
    kapt(AppDependencies.hiltCompiler)
    // ViewModel & Livedata
    implementation(AppDependencies.lifecycleViewModel)
    implementation(AppDependencies.lifecycleLiveData)
    implementation(AppDependencies.lifecycleRuntime)
    // Coroutines
    implementation(AppDependencies.coroutinesCore)
    implementation(AppDependencies.coroutinesAndroid)
    // Retrofit
    implementation(AppDependencies.retrofit)
    implementation(AppDependencies.converterGson)
    implementation(AppDependencies.loggingInterceptor)
    implementation(AppDependencies.okHttpJsonMock)
    // Room
    implementation(AppDependencies.roomRuntime)
    kapt(AppDependencies.roomCompiler)
    implementation(AppDependencies.roomKtx)
    // Test
    testImplementation(TestDependencies.junit)
    testImplementation(TestDependencies.robolectric)
    testImplementation(TestDependencies.hamcrest)
    testImplementation(TestDependencies.archCore)
    testImplementation(TestDependencies.coreKtx)
    testImplementation(TestDependencies.junitKtx)
    testImplementation(TestDependencies.kotlinCoroutines)
    testImplementation(TestDependencies.mockWebServer)
    testImplementation(TestDependencies.mockitoCore)
    testImplementation(TestDependencies.mockitoInline)
    testImplementation(TestDependencies.archCore)
    debugImplementation(TestDependencies.fragment)
    androidTestImplementation(TestDependencies.extJUnit)
    androidTestImplementation(TestDependencies.espressoCore)
    androidTestImplementation(TestDependencies.espressoContrib)
    androidTestImplementation(TestDependencies.hiltAndroid)
    kaptAndroidTest(TestDependencies.hiltCompiler)
    testImplementation(TestDependencies.hiltAndroid)
    // Chucker
    debugImplementation(AppDependencies.chucker)
    releaseImplementation(AppDependencies.chuckerNoOp)
    // Glide
    implementation(AppDependencies.glide)
    kapt(AppDependencies.glideCompiler)
    // Detekt
    detektPlugins(ValidationDependencies.detekt)
}
