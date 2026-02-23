# Document Management System (Test Assignment)

## Краткое описание
Проект представляет собой серверное приложение для управления жизненным циклом документов. Поддерживаются статусы `DRAFT`, `SUBMITTED`, и `APPROVED`. Система реализует транзакционные операции одиночной и поточной (batch) передачи статусов, защиту паралельного редактирования (Concurrency Check), а также фоновую обработку документов scheduled worker-ами (из `DRAFT` в `APPROVED`). 

В комплекте поставляется отдельный Java CLI утилита (`document-generator-cli`) для быстрой поточной генерации тестовых данных.

## Реализованный функционал
- Управление документами (Create, Read, Search).
- Переходы по жизненному циклу: одиночные `submit` и `approve`.
- Пакетная обработка: `batch submit` и `batch approve` с обработкой "частичного успеха" (Partial Success) без обрыва транзакций из-за единичных ошибок.
- Ведение истории: `Approval Registry` и `Document History` для аудита статусов.
- Concurrency Check API (защита от записи старых версий).
- Фоновые worker-ы:
  - `submit-worker` (автоматика `DRAFT` -> `SUBMITTED`)
  - `approve-worker` (автоматика `SUBMITTED` -> `APPROVED`)
- CLI Generator утилита для тестирования нагрузки (через HTTP API).

## Технологии
- Java 21
- Spring Boot 3
- Spring Data JPA / Hibernate
- PostgreSQL (через Docker Compose)
- Liquibase (Автоматические миграции БД)
- OpenAPI / Swagger (Интерактивная документация)
- Maven
- Docker / Docker Compose

## Структура проекта
```
/document-management-system
├── /document-management-service     # Основной Spring Boot backend
├── /document-generator-cli          # Независимая Java CLI для генерации данных
└── docker-compose.yml               # PostgreSQL DB 
```

## Как запустить

### 1. Запуск БД
Из корня проекта поднимите PostgreSQL базу данных:
```bash
docker compose up -d
```

### 2. Запуск сервиса
Перейдите в директорию бекенда и запустите приложение через maven wrapper:
```bash
cd document-management-service
./mvnw spring-boot:run
```
*Примечание: Liquibase автоматически накатит схемы при старте.*

### 3. Swagger
Интерактивная документация доступна по адресу:
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## Конфигурация
Ключевые настройки `document-management-service` (`application.yml`):
- База данных: URL `jdbc:postgresql://localhost:5433/documents_db`.
- Фоновые Background Worker-ы:
  ```yaml
  app:
    workers:
      enabled: true
      batch-size: 20
      submit-interval-ms: 10000
      approve-interval-ms: 15000
  ```

## Как запустить CLI-генератор

Генератор собран в независимый fat JAR.

**1. Как собрать:**
```bash
cd document-generator-cli
mvn clean package
```

**2. Как запустить:**
Запуск генерации $N$ документов через конфигурационный аргумент `--count`:
```bash
java -jar target/document-generator-cli-1.0-SNAPSHOT-shaded.jar --count=50
```

## Примеры API / сценарий проверки

**Сценарий:**
1. Поднимите БД и запустите сервис.
2. Сгенерируйте данные через CLI: `java -jar document-generator-cli/target/document-generator-cli-1.0-SNAPSHOT-shaded.jar --count=20`.
3. Откройте `app.log` или консоль Spring Boot: вы увидите, как `submit-worker` подхватывает `DRAFT` батчами и переводит в `SUBMITTED`. Затем `approve-worker` подхватит их и переведет в `APPROVED`.
4. Откройте Swagger UI и запросите `GET /api/v1/documents`. Вы увидите созданные документы со статусом `APPROVED`.

**Примеры тестов через API (Swagger / Curl):**
- **Concurrency Check Test**: Попробуйте отправить `PUT /api/v1/documents/{id}` с `version: 0`, когда актуальная версия документа в БД уже равна `1`. Вы получите HTTP 409 Conflict.
- **Batch Endpoint Test**: Передайте список UUID в `/api/v1/documents/batch/submit`. Если 1 ID правильный, а 2-й ошибочный (не найден или уже `SUBMITTED`), сервис вернет 200 OK (Partial Support) со сводкой `total=2, success=1, error=1`.

## Логирование

Работа организована в минималистичном формате без лишнего спама.
- **CLI**: Выводит `[generator] creating document 1/N` и итоговую сводку `[generator] finished: requested=X, success=Y, failed=Z, tookMs=...`.
- **Workers**: Показывают старт итерации, короткий сэмпл UUID (первые 3), и сводку результатов батча `[worker] batch result: total=X, success=Y, error=Z, tookMs=...`. При пустой базе спам гасится уведомлением `no documents found`.

## Ограничения / допущения
- Локальный запуск (тестовое задание) без внешних S3 buckets хранения бинарных данных, хранятся только метаданные.
- Авторизация не предусмотрена (открытый API) в угоду фокусу на транзакционную безопасность.
- Внешний Message Broker (Kafka/RabbitMQ) для Background Workers не использован, реализована родная schedule-архитектура на базе БД для Zero-Dependency запуска проекта.
