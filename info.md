## Realtime
```bash
watch -n 0.5 'curl -s http://localhost:8080/metrics | python3 -m json.tool'
```

## 🔹 1. Проверка, что сервер жив (`/health`)

```bash
curl -i http://localhost:8080/health
```

Ожидаемо:

* HTTP 200
* простой JSON / текст типа `"OK"`

---

## 🔹 2. Получение метрик (`/metrics`)

```bash
curl -i http://localhost:8080/metrics
```

Смотри:

* `http.in_flight`
* `worker.active`, `worker.queue`
* `tasks.completed / failed / rejected`

👉 Очень полезно дергать **до и после нагрузочных тестов**.

---

## 🔹 3. Отправка одной delay-задачи (`/tasks/submit`)

### Задержка 2 секунды

```bash
curl -i -X POST http://localhost:8080/tasks/submit \
  -H "Content-Type: application/json" \
  -d '{"type":"delay","millis":2000}'
```

Ответ:

```json
{
  "id": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

👉 **ID обязательно скопируй** — дальше он нужен.

---

## 🔹 4. Проверка статуса задачи по ID (`/tasks/{id}`)

```bash
curl -i http://localhost:8080/tasks/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

Возможные состояния:

* `CREATED`
* `RUNNING`
* `COMPLETED`
* `FAILED`

---

## 🔹 5. Получить список всех задач (`/tasks/status`)

```bash
curl -i http://localhost:8080/tasks/status
```

Используй для проверки:

* сколько задач в системе
* какие сейчас выполняются

---

## 🔹 6. Отправка нескольких задач разом (`/tasks/many`)

Например, 5 задач по 1 секунде:

```bash
curl -i -X POST http://localhost:8080/tasks/many \
  -H "Content-Type: application/json" \
  -d '{
    "count": 5,
    "type": "delay",
    "millis": 1000
  }'
```

Ожидаемо:

* массив ID
* рост `worker.active`
* рост `queue`, если задач много

---

## 🔹 7. Нагрузочный тест (проверка пула и очереди)

### 20 быстрых задач

```bash
for i in {1..20}; do
  curl -s -X POST http://localhost:8080/tasks/submit \
    -H "Content-Type: application/json" \
    -d '{"type":"delay","millis":3000}' &
done
wait
```

Потом сразу:

```bash
curl http://localhost:8080/metrics
```

👉 Здесь хорошо видно:

* зачем **отдельный пул фоновых задач**
* что HTTP продолжает отвечать, даже если воркеры заняты

---

## 🔹 8. Проверка отказа при переполнении очереди (`Rejected`)

(если очередь 1024, можно уменьшить в коде до 10 для демонстрации)

```bash
for i in {1..200}; do
  curl -s -X POST http://localhost:8080/tasks/submit \
    -H "Content-Type: application/json" \
    -d '{"type":"delay","millis":10000}' &
done
wait
```

Потом:

```bash
curl http://localhost:8080/metrics
```

Смотри:

* `tasks.rejected > 0`
* задачи со статусом `FAILED` и причиной `"Rejected: queue full"`

---

## 🔹 9. Демонстрация, что HTTP не блокируется

Пока задачи выполняются:

```bash
curl http://localhost:8080/health
curl http://localhost:8080/metrics
```

👉 Это **ключевой аргумент для препода**, зачем **отдельный пул потоков у HTTP-сервера**.

---

## 🔹 10. Неверный ID (проверка edge-case)

```bash
curl -i http://localhost:8080/tasks/123-not-exist
```

Ожидаемо:

* HTTP 404
* корректный JSON об ошибке
