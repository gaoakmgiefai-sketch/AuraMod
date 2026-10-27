// language: Java, file: src/main/java/auramod/rotations/SpookyTimeBypassRotations.java, target: Fabric 1.21.x
package auramod.rotations;

import java.util.Random;

public class SpookyTimeBypassRotations {
    private static final Random RANDOM = new Random();

    public static float[] spookyTimeLegit(float currentYaw, float currentPitch, float targetYaw, float targetPitch) {
        float yawDiff = wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = targetPitch - currentPitch;

        float speedFactor = 28.0f + RANDOM.nextFloat() * 14.0f; 
        float yawStep = clamp(yawDiff, -speedFactor, speedFactor);
        float pitchStep = clamp(pitchDiff, -speedFactor, speedFactor);

        float noiseYaw = (RANDOM.nextFloat() - 0.5f) * 0.3f;
        float noisePitch = (RANDOM.nextFloat() - 0.5f) * 0.3f;

        return new float[] { currentYaw + yawStep + noiseYaw, clamp(currentPitch + pitchStep + noisePitch, -90.0f, 90.0f) };
    }

    public static float[] reallyWorldAggressive(float currentYaw, float currentPitch, float targetYaw, float targetPitch) {
        float yawDiff = wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = targetPitch - currentPitch;

        float maxDelta = 78.5f;
        float yawStep = clamp(yawDiff, -maxDelta, maxDelta);
        float pitchStep = clamp(pitchDiff, -maxDelta, maxDelta);

        float pitchJitter = (RANDOM.nextFloat() - 0.5f) * 1.2f;

        return new float[] { currentYaw + yawStep, clamp(currentPitch + pitchStep + pitchJitter, -90.0f, 90.0f) };
    }

    public static float[] silentMatrixBypass(float serverYaw, float serverPitch, float targetYaw, float targetPitch) {
        float yawDiff = wrapDegrees(targetYaw - serverYaw);
        float pitchDiff = targetPitch - serverPitch;

        float maxPacketDelta = 60.0f;
        if (Math.abs(yawDiff) > maxPacketDelta) {
            targetYaw = serverYaw + Math.signum(yawDiff) * maxPacketDelta;
        }

        if (Math.abs(pitchDiff) > maxPacketDelta) {
            targetPitch = serverPitch + Math.signum(pitchDiff) * maxPacketDelta;
        }

        return new float[] { targetYaw, clamp(targetPitch, -90.0f, 90.0f) };
    }

    private static float wrapDegrees(float value) {
        value %= 360.0f;
        if (value >= 180.0f) value -= 360.0f;
        if (value < -180.0f) value += 360.0f;
        return value;
    }

    private static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }
}
