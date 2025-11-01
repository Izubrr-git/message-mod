# Использование Spring Data JPA Repository (Альтернативный подход)

## ⚠️ Важное замечание

**Не рекомендуется** использовать Spring Data JPA в Minecraft модах по следующим причинам:

1. **Размер**: Увеличивает размер мода на 20-30 МБ
2. **Сложность**: Требует Spring Application Context
3. **Конфликты**: Может конфликтовать с системой загрузки Minecraft
4. **Оверкилл**: Для простого CRUD избыточно

Однако, если ты все равно хочешь использовать Spring Data JPA, вот как это сделать:

## 📦 Добавление зависимостей

Обнови `build.gradle`:

```gradle
dependencies {
    // ... существующие зависимости ...
    
    // Spring Data JPA
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa:3.2.0'
    implementation 'org.springframework:spring-context:6.1.0'
}
```

## 📝 Создание JPA Repository

### 1. Интерфейс репозитория

Создай файл `MessageJpaRepository.java`:

```java
package com.example.messagemod.database.repository;

import com.example.messagemod.database.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageJpaRepository extends JpaRepository<MessageEntity, Long> {
    
    // Найти все сообщения игрока
    List<MessageEntity> findByUuid(UUID uuid);
    
    // Найти последние N сообщений
    List<MessageEntity> findTop10ByOrderByIdDesc();
    
    // Подсчитать сообщения игрока
    long countByUuid(UUID uuid);
    
    // Кастомный запрос
    @Query("SELECT m FROM MessageEntity m WHERE LENGTH(m.text) > :minLength")
    List<MessageEntity> findMessagesLongerThan(int minLength);
    
    // Удалить сообщения игрока
    void deleteByUuid(UUID uuid);
}
```

### 2. Spring Configuration

Создай `SpringConfig.java`:

```java
package com.example.messagemod.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(basePackages = "com.example.messagemod.database.repository")
@EntityScan(basePackages = "com.example.messagemod.database.entity")
public class SpringConfig {
    
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5432/minecraft");
        dataSource.setUsername("postgres");
        dataSource.setPassword("your_password");
        return dataSource;
    }
    
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.example.messagemod.database.entity");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());
        
        return em;
    }
    
    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }
    
    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.show_sql", "true");
        return properties;
    }
}
```

### 3. Spring Context Manager

Создай `SpringContextManager.java`:

```java
package com.example.messagemod.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringContextManager {
    private static ApplicationContext context;
    
    public static void initialize() {
        context = new AnnotationConfigApplicationContext(SpringConfig.class);
    }
    
    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }
    
    public static void shutdown() {
        if (context instanceof AnnotationConfigApplicationContext) {
            ((AnnotationConfigApplicationContext) context).close();
        }
    }
}
```

### 4. Использование в NetworkHandler

Обнови `NetworkHandler.java`:

```java
// Вместо прямого создания репозитория:
MessageRepository repository = new MessageRepository(sessionFactory);

// Используй Spring:
MessageJpaRepository repository = SpringContextManager.getBean(MessageJpaRepository.class);
repository.save(new MessageEntity(playerUuid, text));
```

### 5. Инициализация в MessageMod

Обнови `MessageMod.java`:

```java
@Override
public void onInitialize() {
    // ...
    
    ServerLifecycleEvents.SERVER_STARTING.register(server -> {
        try {
            SpringContextManager.initialize();
            LOGGER.info("Spring Context инициализирован!");
        } catch (Exception e) {
            LOGGER.error("Ошибка инициализации Spring!", e);
        }
    });
    
    ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
        SpringContextManager.shutdown();
    });
}
```

## 🎯 Преимущества Spring Data JPA

✅ **Меньше кода** - не нужно писать CRUD методы
✅ **Query методы** - автоматическая генерация запросов из имен методов
✅ **Pagination** - встроенная поддержка пагинации
✅ **Specifications** - динамические запросы
✅ **Auditing** - автоматическое отслеживание времени создания/обновления

## ❌ Недостатки для Minecraft мода

❌ **Большой размер** - Spring Boot Starter весит много
❌ **Медленный старт** - инициализация Spring Context занимает время
❌ **Сложность** - излишняя для простого CRUD
❌ **Зависимости** - множество транзитивных зависимостей

## 💡 Вердикт

**Для Minecraft мода рекомендуется использовать обычный Hibernate** (как в основном проекте), так как:

- Легковесный
- Быстрая инициализация
- Достаточно функционала для задачи
- Нет излишних зависимостей

Spring Data JPA имеет смысл только если:
- У тебя очень сложная логика работы с БД
- Нужны dynamic queries с Specifications
- Ты делаешь большой серверный плагин с множеством таблиц

## 📚 Дополнительные ресурсы

- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Hibernate vs Spring Data JPA](https://www.baeldung.com/hibernate-vs-spring-data-jpa)

---

**В 99% случаев для Minecraft модов лучше использовать обычный Hibernate! ✨**
