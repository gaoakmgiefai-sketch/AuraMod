
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

    private final KillAuraModule practiceModule =
            new KillAuraModule();

    private KeyBinding clickGuiKey;
    private KeyBinding toggleAuraKey;

    @Override
    public void onInitializeClient() {

        // Right Shift — open ClickGUI
        clickGuiKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.auramod.click_gui",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_RIGHT_SHIFT,
                        "category.auramod"
                )
        );

        // R — toggle Practice Aura
        toggleAuraKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.auramod.toggle_practice_aura",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_R,
                        "category.auramod"
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            // Open the menu when Right Shift is pressed.
            while (clickGuiKey.wasPressed()) {
                if (client.player != null
                        && client.world != null
                        && client.currentScreen == null) {

                    client.setScreen(
                            new ClickGuiScreen(practiceModule)
                    );
                }
            }

            // Toggle the practice module with R.
            while (toggleAuraKey.wasPressed()) {
                if (client.player != null
                        && client.world != null
                        && client.currentScreen == null) {

                    practiceModule.toggle();

                    client.player.sendMessage(
                            Text.literal(
                                    "Practice Aura: "
                                            + (practiceModule.isEnabled()
                                            ? "ON" : "OFF")
                            ),
                            true
                    );
                }
            }

            // Run the module during normal gameplay.
            if (client.currentScreen == null) {
                practiceModule.onTick();
            }
        });
    }
}
