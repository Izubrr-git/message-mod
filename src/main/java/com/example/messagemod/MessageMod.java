package com.example.messagemod;

import com.example.messagemod.database.DatabaseManager;
import com.example.messagemod.database.repository.MessageRepository;
import com.example.messagemod.network.NetworkHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageMod implements ModInitializer {
    public static final String MOD_ID = "messagemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Инициализация Message Mod...");

        NetworkHandler.registerServerReceivers();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            LOGGER.info("Сервер запускается, инициализация базы данных...");

            try {
                DatabaseManager.getInstance().initialize();

                MessageRepository repository = new MessageRepository(
                        DatabaseManager.getInstance().getSessionFactory()
                );

                if (repository.testConnection()) {
                    LOGGER.info("База данных готова к работе!");
                } else {
                    LOGGER.error("Не удалось подключиться к базе данных!");
                }

            } catch (Exception e) {
                LOGGER.error("Критическая ошибка при инициализации БД!", e);
                LOGGER.error("Мод будет работать без сохранения сообщений в БД.");
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("Сервер останавливается, закрытие подключения к БД...");
            DatabaseManager.getInstance().shutdown();
        });

        LOGGER.info("Message Mod успешно инициализирован!");
    }
}
