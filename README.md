# Limits Guard Service 🛡️

Высоконагруженный микросервис валидации транзакционных лимитов и защиты от дублей.

## Технологический стек
* **Java 21** / **Spring Boot 3.4+**
* **PostgreSQL** — основное хранилище транзакций
* **Redis** — распределенные блокировки (Distributed Lock) и Rate Limiting (Sliding Window)
* **MapStruct** & **Lombok**
* **Testcontainers** & **MockMvc** — полное интеграционное и веб-тестирование

## Главные фичи
1. **Idempotency Guard (`409 Conflict`):** Защита от дублирующих параллельных транзакций с помощью Redis-блокировок.
2. **Rate & Amount Limiter (`429` / `422`):** Контроль частоты запросов и суточных денежных лимитов.
3. **Resilient Architecture:** Разделение транзакций Postgres и внешних I/O вызовов в Redis для защиты HikariCP пула.