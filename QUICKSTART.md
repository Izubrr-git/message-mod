# 🚀 Быстрый старт

## За 5 минут от нуля до работающего мода!

### ⚡ Вариант 1: С Docker (самый простой)

```bash
# 1. Клонируй проект
cd minecraft-message-mod

# 2. Запусти PostgreSQL через Docker
docker-compose up -d

# 3. Скопируй и настрой конфиг Hibernate
cp hibernate.cfg.xml.example src/main/resources/hibernate.cfg.xml

# В hibernate.cfg.xml используй эти настройки:
# URL: jdbc:postgresql://localhost:5432/minecraft
# Username: postgres
# Password: minecraft123

# 4. Собери мод
./gradlew build

# 5. Установи мод
cp build/libs/message-mod-1.0.0.jar ~/.minecraft/mods/

# 6. Запусти Minecraft 1.21.8 с Fabric
# Не забудь установить Fabric API!

# 7. В игре нажми 'M' и отправь сообщение!
```

### 📝 Вариант 2: С локальным PostgreSQL

```bash
# 1. Установи PostgreSQL (если ещё не установлен)
# Ubuntu/Debian:
sudo apt install postgresql postgresql-contrib

# macOS:
brew install postgresql@16

# Windows: скачай с postgresql.org

# 2. Создай базу данных
sudo -u postgres psql
postgres=# CREATE DATABASE minecraft;
postgres=# \q

# 3. Настрой пароль (опционально)
sudo -u postgres psql
postgres=# ALTER USER postgres PASSWORD 'your_password';
postgres=# \q

# 4. Настрой проект
cd minecraft-message-mod
cp hibernate.cfg.xml.example src/main/resources/hibernate.cfg.xml

# Отредактируй src/main/resources/hibernate.cfg.xml:
# Замени YOUR_PASSWORD на свой пароль

# 5. Собери мод
./gradlew build

# 6. Установи в Minecraft
cp build/libs/message-mod-1.0.0.jar ~/.minecraft/mods/

# 7. Играй!
```

## ✅ Checklist перед запуском

- [ ] Java 21+ установлена (`java -version`)
- [ ] PostgreSQL запущен (`docker-compose ps` или `sudo systemctl status postgresql`)
- [ ] База данных `minecraft` создана
- [ ] Файл `hibernate.cfg.xml` настроен с правильным паролем
- [ ] Fabric Loader установлен в Minecraft 1.21.8
- [ ] Fabric API скачан и установлен в `.minecraft/mods/`
- [ ] Мод собран (`./gradlew build` успешно)
- [ ] `.jar` файл скопирован в папку mods

## 🎮 Первый запуск

1. **Запусти Minecraft**
2. **Зайди в мир** (или на сервер)
3. **Нажми клавишу 'M'**
4. **Введи сообщение** и нажми "Отправить"
5. **Проверь в консоли** - должно быть:
   ```
   [messagemod] Получено сообщение от игрока YourName: Hello!
   [messagemod] Сообщение успешно сохранено: MessageEntity{id=1, ...}
   ```
6. **Проверь в БД:**
   ```bash
   docker exec -it minecraft-postgres psql -U postgres -d minecraft
   # или
   psql -U postgres -d minecraft
   
   minecraft=# SELECT * FROM messages;
   ```

## 🔍 Проверка работы

### Проверка 1: GUI открывается?
- Нажми 'M'
- Должен открыться экран с текстовым полем

### Проверка 2: Сообщение отправляется?
- Введи текст
- Нажми "Отправить"
- Должно появиться: "§aСообщение успешно сохранено!"

### Проверка 3: Данные в БД?
```sql
SELECT * FROM messages ORDER BY id DESC LIMIT 1;
```
Должна быть твоя запись!

## 🐛 Если что-то не работает

### GUI не открывается
```bash
# Проверь логи клиента
tail -f ~/.minecraft/logs/latest.log | grep messagemod
```
Должна быть строка: `"Клиентская часть Message Mod инициализирована!"`

### Ошибка подключения к БД
```bash
# Проверь, что PostgreSQL работает
docker-compose ps  # Должен быть "Up"
# или
sudo systemctl status postgresql  # Должен быть "active (running)"
```

### Protobuf ошибки при сборке
```bash
# Пересобери с генерацией
./gradlew clean generateProto build
```

### Полный troubleshooting
См. файл `docs/TROUBLESHOOTING.md`

## 📚 Дальше куда?

- 📖 **Архитектура:** `docs/ARCHITECTURE.md` - как всё работает
- 🔧 **Troubleshooting:** `docs/TROUBLESHOOTING.md` - решение проблем
- 🌟 **JPA Repository:** `docs/SPRING_DATA_JPA.md` - альтернативный подход
- 📝 **Основной README:** `README.md` - полная документация

## 💡 Полезные команды

```bash
# Сборка мода
./gradlew build

# Очистка и пересборка
./gradlew clean build

# Запуск тестового клиента
./gradlew runClient

# Запуск тестового сервера
./gradlew runServer

# Проверка зависимостей
./gradlew dependencies

# Генерация Protobuf классов
./gradlew generateProto

# Просмотр БД
docker exec -it minecraft-postgres psql -U postgres -d minecraft
```

## 🎉 Готово!

Теперь у тебя работающий мод, который:
- ✅ Показывает GUI по нажатию 'M'
- ✅ Отправляет Protobuf сообщения на сервер
- ✅ Сохраняет их в PostgreSQL через Hibernate
- ✅ Использует официальные Mojang маппинги

**Удачи в разработке! 🚀✨**

---

*Есть вопросы? Открой issue или смотри документацию в папке `docs/`*
