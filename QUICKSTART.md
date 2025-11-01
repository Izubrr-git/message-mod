# 🚀 Быстрый старт

### ⚡ Вариант 1: С Docker (самый простой)

```bash
# 1. Клонируй проект
cd minecraft-message-mod

# 2. Запусти PostgreSQL через Docker
docker-compose up -d

# 3. Скопируй и настрой конфиг Hibernate
cp hibernate.cfg.xml.example src/main/resources/hibernate.cfg.xml

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