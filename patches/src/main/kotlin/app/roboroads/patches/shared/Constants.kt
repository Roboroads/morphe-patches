package app.roboroads.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_DISCOVERYPLUS_FIRETV = Compatibility(
        name = "Discovery Plus", // App name as it appears in the Android launcher.
        packageName = "com.discovery.discoveryplus.firetv",
        apkFileType = ApkFileType.APK,
        targets = listOf(
            AppTarget(
                version = "22.0.0.67"
            )
        )
    )
}
