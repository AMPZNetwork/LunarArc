package io.lunararcdevs.lunararc.common.mixin.core.server;

/**
 * Retired in Runtime Fix 04.
 * DistanceManager.simulationDistance is accessed directly through LunarArc's access widener.
 * This inert marker remains so overlay updates cannot leave an obsolete accessor behind.
 */
final class DistanceManagerAccessor {
    private DistanceManagerAccessor() {
    }
}
