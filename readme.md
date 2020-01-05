![](shared/src/main/resources/icon128.png)
# Vocab Anki Push
A chrome browser extension to learn English->Japanese meaning. It generates quiz for words you searched with below tools/websites.
* [Google Translate Plugin](https://chrome.google.com/webstore/detail/google-translate/aapbdbdomjkkjkaonfhkkikfgjllcleb)
* [ALC](https://eow.alc.co.jp/)

## How to build
```shell script
$ ./gradlew build
# ---> You will get zip file at build/vocab-anki-push.zip
#      and unpacked files at build/unpacked. You can use this for development.
```
## Internal design
[Please check the readme in background module.](background/readme.md)