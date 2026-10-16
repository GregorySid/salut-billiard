# Salut — бронирование бильярдных столов

Демонстрационное Android‑приложение для просмотра бильярдных столов и бронирования
игровых сессий. Написано на Kotlin с Jetpack Compose (Material 3), построено по
многослойной архитектуре (domain / data / ui) с MVVM, DI через Hilt, локальным
кэшем на Room и офлайн‑режимом.

> Приложение работает с **мок‑API** (`MockInterceptor` эмулирует REST‑бэкенд),
> поэтому запускается и полностью функционирует без реального сервера.

---

## Возможности

- **Список столов** — карточки с фото, названием, типом, вместимостью, ценой и
  статусом; поиск по названию и постраничная догрузка (пагинация).
- **Фильтры** — по типу стола (русский / американский / снукер), статусу
  (свободен / занят / обслуживание), вместимости, цене и оборудованию. Выбранные
  фильтры сохраняются между запусками (DataStore) и отображаются счётчиком активных.
- **Детали стола** — крупное изображение, характеристики, список оборудования,
  стоимость часа и переход к бронированию.
- **Бронирование** — выбор даты, просмотр слотов и создание брони с расчётом
  итоговой стоимости.
- **Мои бронирования** — активные, прошедшие и отменённые брони с возможностью
  отмены.
- **Настройки** — переключение темы: системная / светлая / тёмная.
- **Офлайн и кэш** — данные кэшируются в Room; при отсутствии сети показывается
  индикатор офлайн‑режима и используются сохранённые данные.
- **Локализация** — интерфейс на русском языке.
- **Edge‑to‑edge UI**, светлая и тёмная темы, фирменная бордовая палитра.

---

## Технологии

| Область | Стек |
|---|---|
| Язык | Kotlin (2.3.x), Coroutines, Flow |
| UI | Jetpack Compose, Material 3, Navigation Compose |
| Архитектура | MVVM, Clean‑подход (domain / data / ui), Repository |
| DI | Hilt (Dagger) + KSP |
| Сеть | Retrofit + OkHttp + kotlinx.serialization |
| Хранилище | Room (кэш), DataStore Preferences (настройки) |
| Изображения | Coil |
| Логирование | Timber |
| Сборка | Android Gradle Plugin, Version Catalog |

Минимальные требования: **minSdk 26**, целевой/compile SDK **37**.
Java/Kotlin target — **17**.

---

## Архитектура

```
UI (Compose) ──► ViewModel ──► Repository ──► { API (Retrofit) , Cache (Room) }
      ▲                                              │
      └──────────────── Flow<State> ◄────────────────┘
```

- **domain** — модели (`BilliardTable`, `Booking`, `TimeSlot`, `TableFilter`,
  `ThemeMode`) и интерфейсы репозиториев, без зависимостей от Android.
- **data** — реализации репозиториев, DTO, мапперы, Retrofit‑API, Room
  (сущности и DAO), DataStore и мок‑сервер (`MockInterceptor`, `MockData`,
  `MockServerState`).
- **ui** — экраны и ViewModel'и Compose, тема, навигация, общие компоненты.
- **di** — Hilt‑модули (сеть, БД, DataStore, репозитории).
- **core** — утилиты: сетевой монитор, форматы даты/времени, `Result`‑обёртка,
  квалификаторы.

---

## Структура проекта

```
app/src/main/java/com/salut/
├── core/                 # утилиты, сетевой монитор, result-обёртка
├── data/
│   ├── local/            # Room: БД, DAO, сущности, DataStore-настройки
│   ├── mapper/           # DTO ↔ domain
│   ├── remote/           # Retrofit API, DTO, mock-сервер
│   └── repository/       # реализации репозиториев
├── di/                   # Hilt-модули
├── domain/               # модели и интерфейсы репозиториев
└── ui/
    ├── tables/           # список столов
    ├── filters/          # фильтры
    ├── detail/           # детали стола
    ├── booking/          # бронирование
    ├── bookings/         # мои бронирования
    ├── settings/         # настройки
    ├── navigation/       # маршруты и NavHost
    ├── components/       # общие composable-компоненты
    └── theme/            # цвета, типографика, тема
```

---


## Скриншоты

<img align="left"  width="240" src="https://github.com/user-attachments/assets/5e95de78-1d6f-47de-a7ca-ea9987a4a7c1" />
<img align="left"  width="240" src="https://github.com/user-attachments/assets/7eaedf48-0ed7-4e1e-83fd-676db44097d3" />
<img align="left"  width="240" src="https://github.com/user-attachments/assets/1283f807-483d-4f4d-84e1-82fb62f2ec8d" />
<img align="left"  width="240" src="https://github.com/user-attachments/assets/dde255e6-b391-48c0-8a8e-3496e000da99" />
<img align="left"  width="240" src="https://github.com/user-attachments/assets/ebc1641e-306f-4030-975d-721244e45df2" />

