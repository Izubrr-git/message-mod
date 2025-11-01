package com.example.messagemod.database.repository;

import com.example.messagemod.database.entity.MessageEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class MessageRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageRepository.class);
    private final SessionFactory sessionFactory;

    public MessageRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public MessageEntity save(UUID playerUuid, String text) {
        Transaction transaction = null;
        Session session = null;
        MessageEntity entity = new MessageEntity(playerUuid, text);

        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            session.persist(entity);
            transaction.commit();

            LOGGER.info("Сообщение успешно сохранено: {}", entity);
            return entity;

        } catch (Exception e) {
            if (transaction != null && transaction.getStatus().canRollback()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    LOGGER.error("Ошибка при откате транзакции", rollbackEx);
                }
            }
            LOGGER.error("Ошибка при сохранении сообщения", e);
            throw new RuntimeException("Не удалось сохранить сообщение в БД", e);

        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public MessageEntity findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(MessageEntity.class, id);
        } catch (Exception e) {
            LOGGER.error("Ошибка при получении сообщения по ID: {}", id, e);
            return null;
        }
    }

    public boolean testConnection() {
        try (Session session = sessionFactory.openSession()) {
            session.createNativeQuery("SELECT 1", Integer.class).getSingleResult();
            LOGGER.info("Подключение к базе данных успешно!");
            return true;
        } catch (Exception e) {
            LOGGER.error("Ошибка подключения к БД", e);
            return false;
        }
    }
}
