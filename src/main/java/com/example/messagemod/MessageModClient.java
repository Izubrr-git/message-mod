package com.example.messagemod;

import com.example.messagemod.client.gui.MessageScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageModClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("messagemod-client");

    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Инициализация клиентской части Message Mod...");

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.messagemod.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.messagemod.keys"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new MessageScreen(null));
                }
            }
        });

        LOGGER.info("Клиентская часть Message Mod инициализирована!");
        LOGGER.info("Нажмите 'M' для открытия экрана отправки сообщения");
    }
}
