plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.intersec.shared"
        compileSdk = 37
        minSdk = 24
        
        withHostTestBuilder {}.configure {}
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // put your multiplatform dependencies here
        }
        commonTest.dependencies {
            implementation(libs.junit)
        }
        
        androidMain.dependencies {
            // Android-specific dependencies
        }
        
        getByName("androidHostTest") {
            dependencies {
                implementation(libs.junit)
            }
        }
    }
}
