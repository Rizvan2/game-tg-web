# Используем официальный образ с JDK 21
FROM openjdk:21-jdk-slim

# Указываем рабочую директорию
WORKDIR /app

# Копируем готовый jar-файл из target
COPY build/libs/*.jar /app/game-tg-bot.jar

# Команда запуска Spring Boot приложения
ENTRYPOINT ["java", "-jar", "game-tg-bot.jar"]