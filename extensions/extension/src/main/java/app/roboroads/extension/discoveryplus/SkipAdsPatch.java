package app.roboroads.extension.discoveryplus;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * Discovery Plus (Fire TV) ad skipping.
 * <p>
 * Ads are stitched into the stream server side. The app's own AdSkipModule skips ad breaks that
 * are already watched. This marks every ad break as watched in
 * com.discovery.adtech.adskip.AdSkipState, so the module skips them all.
 * <p>
 * Reflection is used because the app classes are not available at compile time.
 * The accessed members are public and not obfuscated:
 * AdSkipState.getAllAdBreaks(), getWatchedSlots(), getWatchedAdBreakIds()
 * and AdBreak.getBreakId(). Both sets are mutable LinkedHashSet instances.
 */
@SuppressWarnings({"unused", "unchecked", "rawtypes"})
public final class SkipAdsPatch {

    private SkipAdsPatch() {
    }

    /**
     * Injection point: AdSkipModule.initializeState and AdSkipModule.updateState.
     *
     * @param state com.discovery.adtech.adskip.AdSkipState instance.
     */
    public static void markAllWatched(Object state) {
        if (state == null) return;
        try {
            Class<?> stateClass = state.getClass();
            List adBreaks = (List) stateClass.getMethod("getAllAdBreaks").invoke(state);
            Set watchedSlots = (Set) stateClass.getMethod("getWatchedSlots").invoke(state);
            Set watchedIds = (Set) stateClass.getMethod("getWatchedAdBreakIds").invoke(state);
            if (adBreaks == null || watchedSlots == null || watchedIds == null) return;

            Method getBreakId = null;
            for (int i = 0, size = adBreaks.size(); i < size; i++) {
                watchedSlots.add(i);

                Object adBreak = adBreaks.get(i);
                if (adBreak == null) continue;
                if (getBreakId == null) {
                    getBreakId = adBreak.getClass().getMethod("getBreakId");
                }
                Object breakId = getBreakId.invoke(adBreak);
                if (breakId != null) watchedIds.add(breakId);
            }
        } catch (Throwable ignored) {
            // Never break playback. Ads then behave as unpatched.
        }
    }
}
