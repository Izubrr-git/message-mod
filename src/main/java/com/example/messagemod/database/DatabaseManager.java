package com.example.messagemod.database;

import com.example.messagemod.database.entity.MessageEntity; // 👈 обязательно
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);
    private static DatabaseManager instance;

    private SessionFactory sessionFactory;

    private DatabaseManager() {}

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void initialize() {
        try {
            LOGGER.info("Инициализация подключения к базе данных...");

            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");

            // 👇 вот это добавляем
            configuration.addAnnotatedClass(MessageEntity.class);

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();

            sessionFactory = configuration.buildSessionFactory(serviceRegistry);

            LOGGER.info("SessionFactory успешно создана!");

        } catch (Exception e) {
            LOGGER.error("Критическая ошибка при инициализации БД!", e);
            throw new RuntimeException("Не удалось инициализировать подключение к БД", e);
        }
    }

    public SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            throw new IllegalStateException("База данных не инициализирована! Вызовите initialize() сначала.");
        }
        return sessionFactory;
    }

    public boolean isInitialized() {
        return sessionFactory != null && !sessionFactory.isClosed();
    }

    public void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            LOGGER.info("Закрытие подключения к базе данных...");
            sessionFactory.close();
            LOGGER.info("База данных закрыта.");
        }
    }
}