// language: Java, file: src/main/java/auramod/AuraModClient.java, target: Fabric 1.21.x
package auramod;

import auramod.module.combat.KillAuraModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class AuraModClient implements ClientModInitializer {
    public static KillAuraModule killAura;

    @Override
    public void onInitializeClient() {
        killAura = new KillAuraModule();

        // Регистрируем вызов тиков килки, чтобы она работала на SpookyTime и ReallyWorld
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (killAura != null) {
                killAura.onTick();
            }
        });
    }
}
