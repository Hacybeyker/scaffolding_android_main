plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
    id("scabbard.gradle") version "0.5.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
    id("io.gitlab.arturbosch.detekt") version "1.18.1"
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
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
        versionName = ConfigureApp.versionName
        testInstrumentationRunner = AppVersion.testInstrumentationRunner
        renderscriptSupportModeEnabled = true
        vectorDrawables.useSupportLibrary = true
        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }

    testOptions {
        animationsDisabled = true
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    signingConfigs {
        // TODO change to your app name
        create("release") {
            keyAlias = findProperty("SIGNING_KEY_ALIAS_YOUR") as String?
                ?: System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = findProperty("SIGNING_KEY_PASSWORD_YOUR") as String?
                ?: System.getenv("SIGNING_KEY_PASSWORD")
            storeFile = file("../.signing/release-your-key.jks")
            storePassword = findProperty("SIGNING_STORE_PASSWORD_YOUR") as String?
                ?: System.getenv("SIGNING_STORE_PASSWORD")
        }
    }

    buildTypes {
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", ConstantsApp.Release.BASE_URL)
            buildConfigField(
                "boolean",
                "IS_DEVELOPMENT",
                ConstantsApp.Release.IS_DEVELOPMENT.toString()
            )
        }
        create("qa") {
            initWith(getByName("debug"))
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".qa"
            versionNameSuffix = "-qa"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", ConstantsApp.Debug.BASE_URL)
            buildConfigField(
                "boolean",
                "IS_DEVELOPMENT",
                ConstantsApp.QA.IS_DEVELOPMENT.toString()
            )
        }
        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", ConstantsApp.Debug.BASE_URL)
            buildConfigField(
                "boolean",
                "IS_DEVELOPMENT",
                ConstantsApp.Debug.IS_DEVELOPMENT.toString()
            )
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
        disable.addAll(
            listOf(
                "TypographyFractions",
                "TypographyQuotes",
                "JvmStaticProvidesInObjectDetector",
                "FieldSiteTargetOnQualifierAnnotation",
                "ModuleCompanionObjects",
                "ModuleCompanionObjectsNotInModuleParent"
            )
        )
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
        enabled = false
        failOnError = false
        fullBindingGraphValidation = true
        qualifiedNames = true
        outputFormat = "png"
    }

    tasks {
        "preBuild" {
            dependsOn("ktlintFormat")
            dependsOn("ktlintCheck")
            dependsOn("detekt")
        }
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
    // Glide
    implementation(AppDependencies.glide)
    kapt(AppDependencies.glideCompiler)
    // Shimmer Facebook
    implementation(AppDependencies.shimmerFacebook)
    // Test
    testImplementation(TestDependencies.junit)
    testImplementation(TestDependencies.robolectric)
    testImplementation(TestDependencies.archCore)
    testImplementation(TestDependencies.coreKtx)
    testImplementation(TestDependencies.junitKtx)
    testImplementation(TestDependencies.kotlinCoroutines)
    testImplementation(TestDependencies.mockitoKotlin)
    testImplementation(TestDependencies.mockitoInline)
    androidTestImplementation(TestDependencies.extJUnit)
    androidTestImplementation(TestDependencies.espressoCore)
    // Chucker
    debugImplementation(AppDependencies.chucker)
    "qaImplementation"(AppDependencies.chucker)
    releaseImplementation(AppDependencies.chuckerNoOp)
    // Detekt
    detektPlugins(ValidationDependencies.detekt)
}
