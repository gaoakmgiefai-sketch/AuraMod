package com.auramod;

import com.auramod.module.combat.KillAuraModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class AuraModClient implements ClientModInitializer {
    private final KillAuraModule practiceModule = new KillAuraModule();

    private final KeyBinding toggleKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.auramod.toggle",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_R,
                    "category.auramod"
            )
    );

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                practiceModule.toggle();

                if (client.player != null) {
                    client.player.sendMessage(
                            Text.literal("Practice Aura: " +
                                    (practiceModule.isEnabled() ? "ON" : "OFF")),
                            true
                    );
                }
            }

            practiceModule.onTick();
        });
    }
}
