
package com.auramod.module.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.stream.StreamSupport;

/**
 * AuraMod practice module.
 * Designed for hostile mobs in a local singleplayer world.
 */
public final class KillAuraModule {

    private static final MinecraftClient mc =
            MinecraftClient.getInstance();

    public enum RotationMode {
        SMOOTH,
        SNAP
    }

    private boolean enabled = false;
    private double range = 4.2;
    private double fov = 180.0;
    private boolean raycast = true;

    private RotationMode rotationMode = RotationMode.SMOOTH;

    private float clientYaw;
    private float clientPitch;

    // Module controls

    public void toggle() {
        enabled = !enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    // Settings getters required by ClickGuiScreen

    public double getRange() {
        return range;
    }

    public double getFov() {
        return fov;
    }

    public boolean isRaycastEnabled() {
        return raycast;
    }

    public RotationMode getRotationMode() {
        return rotationMode;
    }

    // Settings setters required by ClickGuiScreen

    public void setRange(double value) {
        range = MathHelper.clamp(value, 1.0, 6.0);
    }

    public void setFov(double value) {
        fov = MathHelper.clamp(value, 30.0, 360.0);
    }

    public void setRaycast(boolean value) {
        raycast = value;
    }

    public void setRotationMode(RotationMode mode) {
        if (mode != null) {
            rotationMode = mode;
        }
    }

    // Practice module tick

    public void onTick() {
        if (!enabled
                || mc.player == null
                || mc.world == null
                || mc.interactionManager == null) {
            return;
        }

        // Restrict this practice module to singleplayer.
        if (!mc.isInSingleplayer()) {
            return;
        }

        LivingEntity target = findBestTarget();

        if (target == null) {
            return;
        }

        float[] rotations = calculateRotations(target);

        applyRotations(rotations[0], rotations[1]);

        if (mc.player.getAttackCooldownProgress(0.5F) >= 1.0F
                && (!raycast || isLookingAt(target))) {

            mc.interactionManager.attackEntity(
                    mc.player,
                    target
            );

            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    // Find the nearest hostile mob within range and FOV.

    private LivingEntity findBestTarget() {
        return StreamSupport.stream(
                        mc.world.getEntities().spliterator(),
                        false
                )
                .filter(HostileEntity.class::isInstance)
                .map(HostileEntity.class::cast)
                .filter(Entity::isAlive)
                .filter(entity ->
                        mc.player.distanceTo(entity) <= range)
                .filter(this::isInFov)
                .min(Comparator.comparingDouble(
                        entity -> mc.player.distanceTo(entity)
                ))
                .orElse(null);
    }

    private boolean isInFov(LivingEntity entity) {
        float[] rotations = calculateRotations(entity);

        float yawDifference = MathHelper.wrapDegrees(
                rotations[0] - mc.player.getYaw()
        );

        return Math.abs(yawDifference) <= fov / 2.0;
    }

    // Calculate rotations toward the target.

    private float[] calculateRotations(Entity entity) {
        Vec3d eyes = mc.player.getEyePos();

        double targetX = entity.getX();
        double targetY = entity.getY()
                + entity.getHeight() / 2.0;
        double targetZ = entity.getZ();

        double diffX = targetX - eyes.x;
        double diffY = targetY - eyes.y;
        double diffZ = targetZ - eyes.z;

        double horizontal = Math.sqrt(
                diffX * diffX + diffZ * diffZ
        );

        float yaw = (float) (
                Math.toDegrees(
                        Math.atan2(diffZ, diffX)
                ) - 90.0
        );

        float pitch = (float) -Math.toDegrees(
                Math.atan2(diffY, horizontal)
        );

        return new float[] {
                MathHelper.wrapDegrees(yaw),
                MathHelper.clamp(pitch, -90.0F, 90.0F)
        };
    }

    // Smooth or instant rotation.

    private void applyRotations(
            float targetYaw,
            float targetPitch
    ) {
        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();

        if (rotationMode == RotationMode.SNAP) {
            clientYaw = targetYaw;
            clientPitch = targetPitch;
        } else {
            clientYaw = currentYaw
                    + MathHelper.wrapDegrees(
                            targetYaw - currentYaw
                    ) * 0.35F;

            clientPitch = currentPitch
                    + (targetPitch - currentPitch) * 0.35F;
        }

        mc.player.setYaw(clientYaw);

        mc.player.setPitch(
                MathHelper.clamp(
                        clientPitch,
                        -90.0F,
                        90.0F
                )
        );
    }

    // Verify the entity under the crosshair.

    private boolean isLookingAt(Entity entity) {
        return mc.crosshairTarget instanceof EntityHitResult hit
                && hit.getEntity() == entity;
    }
}
