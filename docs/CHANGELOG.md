# Changelog

## [2.1.1] - 2020-02-22
- Fix parsing error on translate.google.com

## [2.1.0] - 2020-02-22
- Support translate.google.com
  - Only Japanese and English is supported
  - Only community verified translation is registered

## [2.0.1] - 2020-02-15
- Show "None of the above" in proper language. (It was always Japanese)
- Fix probability of "None of the above". (It was 4/5, but now it's 1/4)

## [2.0.0] - 2020-02-13
- (#4) Support arbitrary language to arbitrary language instead of only English to Japanese
  - Support Japanese to English on alc.co.jp
  - Support Japanese to English on ejje.weblio.jp
- "None of the above" probability was [less than 1/2], but now it's [always 1/5]
- Remove wipe-out functionality
- (#11) Update badge text on word registration timing when necessary

## [1.2.0] - 2020-02-04
- (#9) Show translation string when the correct answer is "None of the above"

## [1.1.1] - 2020-01-29
- (#8) Remove support for google translate plugin

## [1.1.0] - 2020-01-28
- (#7) Support weblio.jp

## [1.0.2] - 2020-01-28
- Pop a notification to jump to changelog when extension is updated

## [1.0.1] - 2020-01-22
- (#6) Improved translation scraping logic for alc.co.jp

## [1.0] - 2020-01-20
- initial release
