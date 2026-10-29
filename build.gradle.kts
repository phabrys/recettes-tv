plugins {
    id("com.android.application") version "9.4.1" apply false
    // Non utilisé (application en Java) : aligne la version du plugin Kotlin requis
    // par AGP sur celle déjà présente dans le cache Gradle, pour compiler hors ligne
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.21" apply false
}
