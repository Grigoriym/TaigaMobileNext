plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
    alias(libs.plugins.taigamobile.android.library.compose)
}

android {
    namespace = "com.grappim.taigamobile.feature.userstories.ui"
}

dependencies {
    implementation(projects.feature.userstories.domain)
    implementation(projects.strings)
}
