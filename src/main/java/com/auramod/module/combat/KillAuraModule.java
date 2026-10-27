// language: Java, file: src/main/java/auramod/module/combat/KillAuraModule.java, target: Fabric 1.21.x
package auramod.module.combat;

import auramod.rotations.SpookyTimeBypassRotations;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

public class KillAuraModule {
    private boolean toggled = true;
    private String currentRotationMode = "ReallyWorld Aggressive";
    private float range = 4.2f;
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public void onTick() {
        if (!toggled || mc.player == null || mc.world == null) return;

        Entity target = findTarget();
        if (target != null) {
            float currentYaw = mc.player.getYaw();
            float currentPitch = mc.player.getPitch();
            
            float targetYaw = calculateYaw(target);
            float targetPitch = calculatePitch(target);

            float[] calculatedRotations = switch (currentRotationMode) {
                case "SpookyTime Legit" -> SpookyTimeBypassRotations.spookyTimeLegit(currentYaw, currentPitch, targetYaw, targetPitch);
                case "Silent Matrix" -> SpookyTimeBypassRotations.silentMatrixBypass(currentYaw, currentPitch, targetYaw, targetPitch);
                default -> SpookyTimeBypassRotations.reallyWorldAggressive(currentYaw, currentPitch, targetYaw, targetPitch);
            };

            mc.player.setYaw(calculatedRotations[0]);
            mc.player.setPitch(calculatedRotations[1]);

            if (mc.player.getAttackCooldownProgress(0.5f) >= 1.0f) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }

    private Entity findTarget() {
        Entity bestTarget = null;
        double minDistance = range;
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity && entity != mc.player) {
                double dist = mc.player.distanceTo(entity);
                if (dist <= minDistance) {
                    minDistance = dist;
                    bestTarget = entity;
                }
            }
        }
        return bestTarget;
    }

    private float calculateYaw(Entity target) {
        double dx = target.getX() - mc.player.getX();
        double dz = target.getZ() - mc.player.getZ();
        return (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
    }

    private float calculatePitch(Entity target) {
        double dx = target.getX() - mc.player.getX();
        double dy = (target.getY() + target.getStandingEyeHeight()) - (mc.player.getY() + mc.player.getStandingEyeHeight());
        double dz = target.getZ() - mc.player.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        return (float) (-(Math.atan2(dy, distance) * (180.0 / Math.PI)));
    }

    public void setMode(String mode) {
        this.currentRotationMode = mode;
    }
}
