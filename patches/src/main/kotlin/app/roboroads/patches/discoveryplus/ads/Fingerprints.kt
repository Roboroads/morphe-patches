package app.roboroads.patches.discoveryplus.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.newInstance
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

private const val AD_SKIP_MODULE = "Lcom/discovery/adtech/adskip/AdSkipModule;"
private const val AD_SKIP_STATE = "Lcom/discovery/adtech/adskip/AdSkipState;"
private const val TIMELINE_INFO = "Lcom/discovery/adtech/core/models/timeline/TimelineInfo;"

/**
 * Legacy adtech path. AdSkipModule.initializeState(TimelineInfo) creates a new AdSkipState
 * and stores it in the "state" field, then logs "State: ".
 */
internal object AdSkipInitializeStateFingerprint : Fingerprint(
    definingClass = AD_SKIP_MODULE,
    name = "initializeState",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(TIMELINE_INFO),
    filters = listOf(
        newInstance(AD_SKIP_STATE),
        fieldAccess(
            opcode = Opcode.IPUT_OBJECT,
            definingClass = AD_SKIP_MODULE,
            name = "state",
            type = AD_SKIP_STATE,
        ),
        string("State: "),
    ),
)

/**
 * Legacy adtech path. AdSkipModule.updateState(TimelineInfo) refreshes the ad breaks of the
 * existing AdSkipState, then reads the "state" field to log "Updated State: ".
 */
internal object AdSkipUpdateStateFingerprint : Fingerprint(
    definingClass = AD_SKIP_MODULE,
    name = "updateState",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(TIMELINE_INFO),
    filters = listOf(
        methodCall(definingClass = AD_SKIP_STATE, name = "setChapterTimelineEntries"),
        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            definingClass = AD_SKIP_MODULE,
            name = "state",
            type = AD_SKIP_STATE,
        ),
        string("Updated State: "),
    ),
)

/**
 * Legacy adtech path. AdSkipModule.onSeekRequest blocks user seeks while inside an ad break.
 */
internal object AdSkipOnSeekRequestFingerprint : Fingerprint(
    definingClass = AD_SKIP_MODULE,
    name = "onSeekRequest",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Lcom/discovery/adtech/core/modules/events/PlaybackEvent\$SeekRequest;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/discovery/adtech/adskip/AdSkipUtility;",
            name = "isInAdBreak",
            returnType = "Z",
        ),
        opcode(Opcode.MOVE_RESULT, MatchAfterImmediately()),
        string("Seeking during ad break is not allowed."),
    ),
)

/**
 * TimelineManager path (only active when the remote flag enables it).
 * Sole watched-ad-break check used by DefaultAdSkipManager.
 */
internal object TimelineIsAdBreakAlreadyWatchedFingerprint : Fingerprint(
    definingClass = "Lcom/wbd/beam/player/timelinemanager/seekmediator/gmssseekmediator/models/TimelineStateKt;",
    name = "isAdBreakAlreadyWatched",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(
        "Lcom/wbd/beam/player/timelinemanager/seekmediator/gmssseekmediator/models/TimelineState;",
        "I",
    ),
    filters = listOf(
        methodCall(name = "getWatchedAdBreakIndices", returnType = "Ljava/util/Set;"),
        methodCall(definingClass = "Ljava/util/Set;", name = "contains"),
    ),
)

/**
 * Adtech SDK builder lambda. Registers the pause ads plugin when
 * DefaultAdTechSdkConfig.Features.getPauseAds() is not null.
 * The obfuscated Unit return type and parameters are declared as "L".
 */
internal object BuildAdTechSdkPauseAdsFingerprint : Fingerprint(
    definingClass = "Lcom/discovery/adtech/sdk/defaultsdk/BuildDefaultAdTechSdkKt;",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "L",
    parameters = listOf(
        "Lcom/discovery/adtech/sdk/defaultsdk/DefaultAdTechSdkConfig;",
        "Lcom/discovery/adtech/eventstreams/AdTechEventStreamsCallbacks;",
        "Ljava/util/List;",
        "L",
        "L",
        "Lcom/discovery/adtech/sdk/AdTechSdkBuilder;",
    ),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/discovery/adtech/sdk/defaultsdk/DefaultAdTechSdkConfig\$Features;",
            name = "getPauseAds",
        ),
        opcode(Opcode.MOVE_RESULT_OBJECT, MatchAfterImmediately()),
        string("PauseAds enabled with "),
    ),
)
