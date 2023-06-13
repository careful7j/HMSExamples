# Huawei Mobile Services (HMS) Examples
This examples cover ALL commonly used sdk (June 2023 latest) of Huawei Mobile Services. Proved to compile for android 13 and are limited with minimal API showcase - no extra libraries, no fancy architecture.

## Account Kit
- sign in without Id verification
- sign in via Id Token
- sign in via Authorization Code (OAuth 2.0)
- code from SMS consent receiver dialog

## Ads Kit
- ads banner, interstitial banner, rewarded ads, instream video ads, native banner ads, splash screens ads
- ads consent dialog, privacy policy feature

## Analytics Kit & Crash Kit
- analytics sdk launch, events collecting, user sessions
- crash event collecting

## FIDO kit
- fingerprint authorization
- face recognition authorization

## IAP Kit
- purchase and consume consumable products
- purchase non-consumable products and subscriptions
- view purchasement history, obtained products

## Location Kit
- get user location, last location, periodic location
- geofence (enter/exit geo zone)
- user activity recognition (like walking/running/driving)

## Map Kit
- standard huawei maps (petal maps), map styling, map fragment
- markers and clusterization, camera animations
- map overlay, heatmap, lite map (high performance picture mode)

## ML kit
- bank cards recognition
- human voice recognition
- text-to-speech feature

## Push Kit
- receive data/notification push messages
- subscribe topics, audience, delete token, multi-sender reciever

## Safety Detect Kit
- check system integrity
- check if known malicious apps installed on a device
- check url of a website for malicious/virus/phishing content
- check user is a robot (captcha)

## Scan Kit
- deafult mode scan QR/Bar/other codes
- customized UI mode scan QR/Bar/other code
- generate QR/Bar/other code

## Test results:
<img width="1336" alt="Screenshot 2023-06-13 at 05 15 02" src="https://github.com/careful7j/HMS-Examples/assets/2966645/9d6e8ab8-8dff-433e-976c-b871aba9981d">
all sdk clean-build ~30 seconds (M1 chip) | Android Studio 2022.2.1 Patch 2 | sdk = 33 | Huawei Mobile Services 6.11.0.302

## Test devices:
- HUAWEI nova 8, Android 10, EMUI 12.0.0 (build 12.0.0.183 June 2023)
- Redmi Note 11, Android 13, MIUI Global (build 14.0.2.0 May 2023)
- Samsung A33,   Android 13, One UI 5.1  (build 5.10.136 May 2023)

## Useful links:
- HUAWEI AppGallery: https://appgallery1.huawei.com/#/app/C27162
- HUAWEI Mobile Services: https://appgallery.cloud.huawei.com/ag/n/app/C10132067
- Official Documentation: https://developer.huawei.com/consumer/en/hms/huawei-accountkit
- Official Examples: https://github.com/HMS-Core
