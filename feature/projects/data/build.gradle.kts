plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
}

android {
    namespace = "com.grappim.taigamobile.feature.projects.data"
}

dependencies {
    implementation(projects.core.api)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(projects.core.async)

    implementation(projects.feature.projects.domain)

    implementation(libs.androidx.paging.common)
    implementation(libs.retrofit)

    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)
}
