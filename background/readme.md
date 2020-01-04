# Sequence diagram of how word is registered and quiz is generated.
![](sequence.png)

# Storage Design
chrome.storage.sync
```json
{
  "words": {
    "foo": {
      "translation":  "フー",
      "quizResult": [ true, false, true, true ]
    },
    "bar": {
      "translation":  "バー",
      "quizResult": [ true, false, true, false ]
    }
  },
  "quizQueue": [ "foo", "bar" ],
  "alarm": { "dummy": 12345 }
}
```
| Key | Value |
|----|----|
| words | registered words |
| words[key].translation | translation for registered word |
| words[key].quizResult | array of boolean for history of quiz results |
| quizQueue | words in queue for quiz |
| alarm | dummy data for firing storage.onChange on alarm change |

# Message Design
## registerWord
content_script -> background
### request
```
{ "msgType": "registerWord",
  "word": <targetWordString>,
  "translation": <targetWordTranslationString> }
```
### response
none.

## requestQuiz
browser_action -> background
### request
```
{ "msgType": "requestQuiz" }
```
### response
```
{ "word": <wordInQueue>,
  "choices":[ <choice1>, <choice2>, <choice3>, <choice4> ] }
```
## answerQuiz
browser_action -> background
### request
```
{ "msgType": "answerQuiz",
  "word": <wordForQuiz>,
  "choice": <chosenAnswer> }
```
### response
```
{ "result": <Boolean>,
  "answer": <translationText> }
```
## option page related messages
omitted
