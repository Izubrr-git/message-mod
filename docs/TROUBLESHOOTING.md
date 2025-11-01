# Руководство по устранению неполадок и FAQ

## 🔧 Устранение неполадок

### Проблема 1: Мод не загружается

**Симптомы:**
- Игра вылетает при запуске
- В логах ошибка "Mod not found" или "Class not found"

**Решения:**

1. **Проверьте версию Java:**
   ```bash
   java -version
   ```
   Должна быть **Java 21** или выше!

2. **Убедитесь, что установлен Fabric API:**
   - Скачайте с [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
   - Версия должна соответствовать Minecraft 1.21.x

3. **Проверьте правильность сборки:**
   ```bash
   ./gradlew clean build
   ```

4. **Проверьте логи:**
   Откройте `.minecraft/logs/latest.log` и найдите строки с `[messagemod]`

---

### Проблема 2: Ошибка подключения к базе данных

**Симптомы:**
- В логах: "Connection refused" или "Could not connect to database"
- Сообщения не сохраняются

**Решения:**

1. **Проверьте, что PostgreSQL запущен:**
   ```bash
   # Linux/Mac
   sudo systemctl status postgresql
   
   # Windows
   services.msc  # Найдите PostgreSQL
   
   # Docker
   docker-compose ps
   ```

2. **Проверьте настройки подключения в `hibernate.cfg.xml`:**
   ```xml
   <property name="hibernate.connection.url">
       jdbc:postgresql://localhost:5432/minecraft
   </property>
   ```

3. **Проверьте, что база данных создана:**
   ```sql
   psql -U postgres -l
   ```
   В списке должна быть база `minecraft`

4. **Проверьте пароль:**
   Убедитесь, что пароль в `hibernate.cfg.xml` совпадает с паролем PostgreSQL

5. **Проверьте firewall:**
   Порт 5432 должен быть открыт для localhost

---

### Проблема 3: Protobuf классы не генерируются

**Симптомы:**
- Ошибки компиляции: "Cannot resolve symbol MessageProto"
- Папка `src/generated` пуста

**Решения:**

1. **Принудительная генерация:**
   ```bash
   ./gradlew clean generateProto build
   ```

2. **Проверьте Protobuf плагин в build.gradle:**
   ```gradle
   plugins {
       id 'com.google.protobuf' version '0.9.4'
   }
   ```

3. **Проверьте, что .proto файл существует:**
   ```
   src/main/proto/message.proto
   ```

4. **Проверьте синтаксис .proto файла:**
   Должен начинаться с `syntax = "proto3";`

---

### Проблема 4: GUI не открывается при нажатии клавиши

**Симптомы:**
- Клавиша M не реагирует
- GUI не появляется

**Решения:**

1. **Проверьте, что клавиша не занята:**
   - Зайдите в настройки управления
   - Найдите секцию "Message Mod"
   - Переназначьте клавишу если нужно

2. **Проверьте логи клиента:**
   Должна быть строка: `"Клиентская часть Message Mod инициализирована!"`

3. **Попробуйте открыть через команду (для теста):**
   Добавьте временно команду в `MessageModClient.java`:
   ```java
   ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
       dispatcher.register(literal("openmsg")
           .executes(ctx -> {
               MinecraftClient.getInstance().setScreen(new MessageScreen(null));
               return 1;
           }));
   });
   ```

---

### Проблема 5: Hibernate ошибки при старте

**Симптомы:**
- `SessionFactory creation failed`
- `Mapping exception`

**Решения:**

1. **Проверьте, что Entity класс правильно аннотирован:**
   ```java
   @Entity
   @Table(name = "messages")
   public class MessageEntity { ... }
   ```

2. **Проверьте classpath:**
   Entity должна быть указана в `hibernate.cfg.xml`:
   ```xml
   <mapping class="com.example.messagemod.database.entity.MessageEntity"/>
   ```

3. **Проверьте зависимости Hibernate:**
   ```bash
   ./gradlew dependencies | grep hibernate
   ```

4. **Включите подробное логирование:**
   В `hibernate.cfg.xml`:
   ```xml
   <property name="hibernate.show_sql">true</property>
   <property name="hibernate.format_sql">true</property>
   ```

---

### Проблема 6: "Table doesn't exist" ошибка

**Симптомы:**
- SQL ошибка: relation "messages" does not exist

**Решения:**

1. **Проверьте настройку hbm2ddl:**
   В `hibernate.cfg.xml` должно быть:
   ```xml
   <property name="hibernate.hbm2ddl.auto">update</property>
   ```

2. **Создайте таблицу вручную:**
   ```bash
   psql -U postgres -d minecraft -f database/init.sql
   ```

3. **Проверьте права пользователя БД:**
   ```sql
   GRANT ALL PRIVILEGES ON DATABASE minecraft TO postgres;
   GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
   ```

---

## ❓ FAQ (Часто задаваемые вопросы)

### Q: Можно ли использовать MySQL вместо PostgreSQL?

**A:** Да! Измените:

1. В `build.gradle`:
   ```gradle
   implementation 'mysql:mysql-connector-java:8.0.33'
   ```

2. В `hibernate.cfg.xml`:
   ```xml
   <property name="hibernate.connection.driver_class">com.mysql.cj.jdbc.Driver</property>
   <property name="hibernate.connection.url">jdbc:mysql://localhost:3306/minecraft</property>
   <property name="hibernate.dialect">org.hibernate.dialect.MySQLDialect</property>
   ```

---

### Q: Как сделать поле для UUID автоматическим (брать от игрока)?

**A:** Уже реализовано! UUID берется автоматически в `NetworkHandler.java`:
```java
context.player().getUuid()
```

---

### Q: Как добавить timestamp в таблицу?

**A:** 

1. Измените Entity:
   ```java
   @Column(name = "created_at")
   private LocalDateTime createdAt = LocalDateTime.now();
   ```

2. Или используйте Hibernate аудит:
   ```java
   @CreationTimestamp
   @Column(name = "created_at", updatable = false)
   private LocalDateTime createdAt;
   ```

---

### Q: Как увеличить лимит символов с 256?

**A:**

1. В `MessageScreen.java`:
   ```java
   textField.setMaxLength(512);  // Измените на нужное значение
   ```

2. В `MessageEntity.java`:
   ```java
   @Column(name = "text", length = 512, nullable = false)
   ```

3. В SQL:
   ```sql
   ALTER TABLE messages ALTER COLUMN text TYPE VARCHAR(512);
   ```

---

### Q: Как посмотреть все сообщения в игре?

**A:** Можно создать команду:

```java
// В MessageMod.java
CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
    dispatcher.register(literal("messages")
        .requires(source -> source.hasPermissionLevel(2))
        .executes(ctx -> {
            MessageRepository repo = new MessageRepository(
                DatabaseManager.getInstance().getSessionFactory()
            );
            // Загрузите и отобразите сообщения
            return 1;
        }));
});
```

---

### Q: Безопасно ли хранить пароль БД в hibernate.cfg.xml?

**A:** Нет! Для продакшена используй:

1. **Переменные окружения:**
   ```java
   String password = System.getenv("DB_PASSWORD");
   ```

2. **Файл .env (не коммитить в git):**
   ```properties
   DB_PASSWORD=your_secure_password
   ```

3. **Шифрование паролей в конфиге**

---

### Q: Мод работает на сервере без клиента?

**A:** Да, но GUI будет доступен только на клиенте. На dedicated сервере только серверная часть (БД + networking) будет работать.

---

### Q: Как добавить валидацию текста (фильтр мата)?

**A:** В `NetworkHandler.java` перед сохранением:

```java
if (text.matches(".*\\b(badword1|badword2)\\b.*")) {
    context.player().sendMessage(
        Text.literal("§cСообщение содержит недопустимые слова!")
    );
    return;
}
```

---

### Q: Можно ли добавить rich text (форматирование)?

**A:** Да, но нужно:
1. Хранить в БД Minecraft text component JSON
2. Изменить TextFieldWidget на более сложный редактор
3. Парсить форматирование при отображении

---

## 📊 Мониторинг

### Полезные SQL запросы для мониторинга:

```sql
-- Топ-10 самых активных игроков
SELECT uuid, COUNT(*) as msg_count 
FROM messages 
GROUP BY uuid 
ORDER BY msg_count DESC 
LIMIT 10;

-- Активность по времени (если есть created_at)
SELECT DATE(created_at) as date, COUNT(*) as messages
FROM messages
GROUP BY DATE(created_at)
ORDER BY date DESC;

-- Средняя длина сообщений
SELECT AVG(LENGTH(text)) as avg_length FROM messages;

-- Самые длинные сообщения
SELECT uuid, text, LENGTH(text) as length
FROM messages
ORDER BY length DESC
LIMIT 10;
```

---

## 🎯 Производительность

### Оптимизация при большом количестве записей:

1. **Индексы:**
   ```sql
   CREATE INDEX idx_messages_uuid ON messages(uuid);
   CREATE INDEX idx_messages_created_at ON messages(created_at);
   ```

2. **Пагинация в репозитории:**
   ```java
   public List<MessageEntity> findRecent(int limit, int offset) {
       return session.createQuery("FROM MessageEntity ORDER BY id DESC", MessageEntity.class)
           .setMaxResults(limit)
           .setFirstResult(offset)
           .list();
   }
   ```

3. **Периодическая очистка старых записей:**
   ```sql
   DELETE FROM messages WHERE created_at < NOW() - INTERVAL '90 days';
   ```

---

**Если твоя проблема не описана здесь, проверь логи и создай issue на GitHub! 🚀**
