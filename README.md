# Kotlin Backend API для KNO.MOS.RU

Данный API содержит в себе всю серверную логику для: 

`база данных Чат-Клиентов/Организаций` + `Чат-бот` + `Консультации`
***

## Источник данных (ParseDataSet)
Источником были предоставленные датасеты в Excel файлах. Все они были переконвертированы в JSON формат, а затем добавлены в базу данных с правильной схемой.
[Проект конвертер для данных](ParseDataSet).

## База данных
База данных представлена в бинарном виде No-SQL. При запуске данные кэшируются, кроме пользователей, в оперативную память.

Пользователи в базе данных имеют ленивую загрузку. Это означает что пользователь будет загружен в оперативную память, а затем через некоторое время, вместе с сохранением пользователя, занимаемое им место будет освобождено.

***

## Чат-бот
Чат-бот представлен в виде древовидной структуры данных.
Основной идеей было сделать `поиск по ключевым словам` + `ветвления` ответов. Он итерирует по всем запросам контура чат-бота и отправляет самый релевантный, который имеет наивысшее совпадение с нужным.

`Answer` - тупик, с сообщением.
***
`FloatAnswer` - "Плавающий вопрос". После установки в текущее состояние, отвечает на более приоритетный вопрос. `Требование о кафе` > `Санкции` > `{Ответ про санкции}`

> **[Юзер]** Требование о кафе `Точка входа в FloatAnswer`
> 
> **[Бот]** В требовании есть следующие санкции .... Могу ответить какие конкретно.
> 
> **[Юзер]** Санкции
>
> **[Бот]** Вывожу санкции ...
> 
> **Примечание:** `Если бы мы не задали первый вопрос, то во второй не было бы ответа по данному требованию.`
***
`SingleForwardAnswer` - Логический ответ с памятью `boolean`. При первом запросе - `первое` сообщение, последующие будут `вторым`.
> **[Юзер]** Привет!
> 
> **[Бот]** Приветствую вас! Задавайте мне вопросы.
> 
> **[Юзер]** Привет!
> 
> **[Бот]** Нет необходимости приветствоваться вновь.
> 
> **[Юзер]** Привет!
> 
> **[Бот]** Нет необходимости приветствоваться вновь.
***
## Косультация

`Косультация` работает внутри структуры базы данных, в несколько потоков. Каждую минуту сканируется список всех слотов для записи, и обновляет состояние слота. см `SlotStatus`. Отправляет уведомления (которые не работают щас на фронтенде но есть в бэкенде). Затем идёт создание комнаты через запрос в WebRTC.
***

### **Важные файлы**

>
> [Файл базы данных](org.db.bin)
>
> [Точка входа](ChatBit/src/main/java/ru/bav/entry/Main.java)
>
> [Главный класс для API запросов](ChatBit/src/main/kotlin/ru/bav/server/api/Endpoints.kt)
>
> [User запросы](ChatBit/src/main/kotlin/ru/bav/server/api/core/UserController.kt)
>
> [Data запросы](ChatBit/src/main/kotlin/ru/bav/server/api/core/DataController.kt)
>
> [Base запросы](ChatBit/src/main/kotlin/ru/bav/server/api/core/BaseController.kt)

# Все запросы к API
```
======== POST =========
@Удаляет все записи по айди юзера
[POST] http://46.243.201.240:3077/user/clearConsults (userId)

@Подтверждение записи консультантом
[POST] http://46.243.201.240:3077/user/confirmConsult (consultant, lowName, from)

@Запись на консультацию
[POST] http://46.243.201.240:3077/user/consultBook (userId, lowName, idControl, idRequire, from, question)

@Создать юзера в БД с ФИО и Айди
[POST] http://46.243.201.240:3077/user/makeuser (id, email, roleTag)

@Запрос на сообщение в чат бот
[POST] http://46.243.201.240:3077/user/onmessage (userId, message)

@Установить роль юзеру
[POST] http://46.243.201.240:3077/user/setRole (userId, roleTag)

======== GET =========
@Штука которая выводит инфу всю
[GET] http://46.243.201.240:3077/info (entry?)

@Корень БД
[GET] http://46.243.201.240:3077/root ()

@Получить все записи возможные
[GET] http://46.243.201.240:3077/user/consult/month (lowName)

@Все слоты в виде дней по id
[GET] http://46.243.201.240:3077/user/consults (userId)

@Получить айдишники консультантов
[GET] http://46.243.201.240:3077/user/consultants ()

@Получить системные роли
[GET] http://46.243.201.240:3077/user/roles ()

@Получить инфу о юзере
[GET] http://46.243.201.240:3077/user/info (id)

@Макс пейдж
[GET] http://46.243.201.240:3077/user/maxpages (id)

@Страница последних сообщений пользователя
[GET] http://46.243.201.240:3077/user/page (id, page)

@Получить все даты
[GET] http://46.243.201.240:3077/data/org/schedule/list (lowName)

@Получить всю инфу о КНО
[GET] http://46.243.201.240:3077/data/org/fullinfo (lowName)

@Получить главу КНО
[GET] http://46.243.201.240:3077/data/org/head (lowName)

@Получить список КНО
[GET] http://46.243.201.240:3077/data/org/list ()

@Получить НПА организации
[GET] http://46.243.201.240:3077/data/org/npas (lowName)

@Получить всю инфу о конкретном требовании
[GET] http://46.243.201.240:3077/data/org/requires/body (lowName, idControl, idRequire)

@Получить список требований
[GET] http://46.243.201.240:3077/data/org/requires/list (lowName, idControl)

@Получить акутальные даты записи
[GET] http://46.243.201.240:3077/data/org/schedule/actual (lowName, dayLong)

@Получить список вид КНО
[GET] http://46.243.201.240:3077/data/org/typec/list (lowName)
```