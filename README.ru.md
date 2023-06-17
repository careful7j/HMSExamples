# Мобильные Сервисы Хуавей (HMS)
[![en](https://img.shields.io/badge/lang-en-red.svg)](https://github.com/careful7j/HMS-Examples/blob/master/README.md)
[![ru](https://img.shields.io/badge/lang-ru-red.svg)](https://github.com/careful7j/HMS-Examples/blob/master/README.ru.md)

Примеры показывают возможности наиболее используемых sdk (последние на июнь 2023) мобильных сервисов Хуавей 6.11.0.302. Примеры проверены на работоспособность под android 13. Использование сторонних библиотек и архитектурные излишества сведены к минимуму.

## Account Kit
- вход без Id верификации
- вход через Id токен
- вход по авторизационному коду (OAuth 2.0)
- диалог-подтверждение чтения кодов из входящих смс

[Официальная документация](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-0000001050048870) | [Описание API](https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/account-apk-api-index-0000001052863604)

## Ads Kit
- реклама: баннер, полноэкранная, с вознаграждением, встроенная в видео, нативный баннер, загрузочный экран
- диалог согласия на обработку данных, политика конфиденциальности

[Официальная документация](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/publisher-service-introduction-0000001070671805) | [Описание API](https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/package-summary-0000001050064868)

## Analytics Kit & Crash Kit
- запуск sdk аналитики, сбор событий, сессии пользователя
- сбор событий вылетов приложения

[Официальная документация](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-0000001050745149) | [Описание API](https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/package-summary-0000001085947990)

## FIDO kit
- вход по отпечатку пальца
- вход по лицу

[Официальная документация](https://developer.huawei.com/consumer/en/doc/development/Security-Guides/introduction-0000001051069988) | [Описание API](https://developer.huawei.com/consumer/en/doc/development/Security-References/package-summary-0000001074610341)

## IAP Kit
- покупка и потребление потребляемых продуктов
- покупка не потребляемых продуктов и подписок
- получение списка купленных продуктов, просмотр истории

[Официальная документация](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-introduction-0000001265784086) | [Описание API](https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/client-package-summary-0000001063498539)

## Location Kit
- получение геолокации, последней геолокации, регулярное обновление геолокации
- геозона (вход и выход из отмеченной области)
- распознавание активности (ходьба/бег/вождение автомобиля)

[Официальная документация](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-introduction-0000001121930588) | [Описание API](https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/overview-0000001051066102)

## Map Kit
- стандартные карты Хуавей (Petal maps), стилизирование карт, карта-фрагмент
- маркеры и кластеризация маркеров, анимация камеры
- оверлей для карты, легковесная карта (высокопроизводительный режим)

[Официальная документация](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-sdk-brief-introduction-0000001061991343) | [Описание API](https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/package-summary-0000001063736331)

## ML kit
- распознавание банковских карт
- распознавание человеческой речи
- чтение текста голосом

[Официальная документация](https://developer.huawei.com/consumer/en/doc/development/hiai-Guides/service-introduction-0000001050040017) | [Описание API](https://developer.huawei.com/consumer/en/doc/development/hiai-References/android-api-overview-0000001051426068)

## Push Kit
- получение пушей с данными и пушей с уведомлением (в шторке)
- подписка на темы, аудиторию, удаление токена, одновременное получение пушей от нескольких серверов

[Официальная документация](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-app-quickstart-0000001071490422) | [Описание API](https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/android-api-pkgsummary-0000001071362489)

## Safety Detect Kit
- проверка целостности системы (рут)
- проверка на наличие уже установленных вредоносных программ
- проверка ссылки на вредоносное/вирусное/фишинговое содержимое
- проверка является ли пользователь роботом (капча)

[Официальная документация](https://developer.huawei.com/consumer/en/doc/development/Security-Guides/introduction-0000001050156325) | [Описание API](https://developer.huawei.com/consumer/en/doc/development/Security-References/package-summary-0000001074502929)

## Scan Kit
- стандартный интерфейс сканирования QR/bar и других кодов
- свой интерфейс сканирования QR/bar и других кодов
- генерация QR/Bar и других кодов

[Официальная документация](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/service-introduction-0000001050041994) | [Описание API](https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/scan-apioverview-0000001050185407)

## Результаты тестов:
<img width="1336" alt="Screenshot 2023-06-13 at 05 15 02" src="https://github.com/careful7j/HMS-Examples/assets/2966645/9d6e8ab8-8dff-433e-976c-b871aba9981d">

## Тестовые устройства:
- HUAWEI nova 8, Android 10, EMUI 12.0.0 (build 12.0.0.183 June 2023)
- Redmi Note 11, Android 13, MIUI Global (build 14.0.2.0 May 2023)
- Samsung A33,   Android 13, One UI 5.1  (build 5.10.136 May 2023)

## Полезные ссылки:
- HUAWEI AppGallery: https://appgallery1.huawei.com/#/app/C27162
- HUAWEI Mobile Services: https://appgallery.cloud.huawei.com/ag/n/app/C10132067
- Официальные примеры: https://github.com/HMS-Core
