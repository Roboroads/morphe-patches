package app.roboroads.patches.discoveryplus.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.patch
import app.roboroads.patches.shared.Constants.COMPATIBILITY_DISCOVERYPLUS_FIRETV
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

private const val EXTENSION_CLASS = "Lapp/roboroads/extension/discoveryplus/SkipAdsPatch;"

@Suppress("unused")
val skipAdsPatch = patch(
    name = "Skip ads",
    description = "Ads are stitched into the video stream by the server and cannot be removed. " +
        "This patch treats every ad break as already watched so the player skips over it " +
        "automatically, allows seeking out of and past ad breaks, and disables pause ads.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_DISCOVERYPLUS_FIRETV)

    extendWith("extensions/extension.mpe")

    execute {
        // Legacy adtech path: mark every ad break as watched when the ad skip state is created.
        AdSkipInitializeStateFingerprint.let {
            val putIndex = it.instructionMatches[1].index
            val stateRegister = it.method.getInstruction<TwoRegisterInstruction>(putIndex).registerA

            it.method.addInstruction(
                putIndex + 1,
                "invoke-static { v$stateRegister }, $EXTENSION_CLASS->markAllWatched(Ljava/lang/Object;)V",
            )
        }

        // Legacy adtech path: mark every ad break as watched when the timeline is updated.
        AdSkipUpdateStateFingerprint.let {
            val getIndex = it.instructionMatches[1].index
            val stateRegister = it.method.getInstruction<TwoRegisterInstruction>(getIndex).registerA

            it.method.addInstruction(
                getIndex + 1,
                "invoke-static { v$stateRegister }, $EXTENSION_CLASS->markAllWatched(Ljava/lang/Object;)V",
            )
        }

        // Legacy adtech path: allow user seeks while inside an ad break.
        AdSkipOnSeekRequestFingerprint.let {
            val moveResultIndex = it.instructionMatches[1].index
            val register = it.method.getInstruction<OneRegisterInstruction>(moveResultIndex).registerA

            it.method.addInstruction(moveResultIndex + 1, "const/4 v$register, 0x0")
        }

        // TimelineManager path: every ad break counts as already watched.
        TimelineIsAdBreakAlreadyWatchedFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )

        // Pause ads: take the existing "Pause Ads disabled, no config" branch.
        BuildAdTechSdkPauseAdsFingerprint.let {
            val moveResultIndex = it.instructionMatches[1].index
            val register = it.method.getInstruction<OneRegisterInstruction>(moveResultIndex).registerA

            it.method.addInstruction(moveResultIndex + 1, "const/4 v$register, 0x0")
        }
    }
}
