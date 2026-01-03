# URL Shortener CLI

CLI-сервис сокращения ссылок (Picocli + JSON-хранилище).

Реализовано:
- Генерация коротких кодов `clck.ru/<code>` (с проверкой коллизий)
- UUID пользователя без авторизации (UUID сохраняется локально и переиспользуется)
- Лимит переходов и TTL (протухшие ссылки удаляются в фоне)
- Уведомления в консоль: создание / приближение к лимиту / лимит исчерпан / TTL истёк / удаление
- Открытие ссылки в браузере (`open`)
- Персистентность: ссылки хранятся в `{dataDir}/links.json`

## Сборка
```bash
mvn clean package
```

## Запуск
После сборки появится fat-jar:
```bash
java -jar target/url-shortener-cli-1.0.0.jar --help
```

## Команды

Создать ссылку:
```bash
java -jar target/url-shortener-cli-1.0.0.jar create https://example.com --max 10
```

Открыть по коду (учтёт клик и откроет браузер):
```bash
java -jar target/url-shortener-cli-1.0.0.jar open 3DZHeG
```

Список ваших ссылок:
```bash
java -jar target/url-shortener-cli-1.0.0.jar list
```

Удалить (только владелец):
```bash
java -jar target/url-shortener-cli-1.0.0.jar delete 3DZHeG
```

Обновить лимит (только владелец):
```bash
java -jar target/url-shortener-cli-1.0.0.jar update 3DZHeG --max 100
```

## Хранилище данных
По умолчанию данные лежат в `~/.urlshortener/`:
- `links.json` — ссылки
- `user.properties` — UUID пользователя

Для тестов/CI можно переопределить:
```bash
-Durlshortener.dataDir=/tmp/urlshortener
```

## Тестирование
```bash
mvn test
```
