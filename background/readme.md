# Sequence diagram of how word is registered and quiz is generated.
![](sequence.png)

# Storage Design
chrome.storage.sync
```
{
  "storageVersion": 2
  "words: {
    "enja:foo": {
      "translation": "フー",
      "correctCount": 3
    },
    "jaen:あああ": {
      "translation": "aaa",
      "correctCount": 2
    },
    ...
  }
  "quizQueue": [
    "enja:foo"
    ...
  ],
  "quiz": {
    "wordKey": "enja:foo",
    "choices":[ <choice1>, <choice2>, <choice3>, <choice4> ],
    "answer": 2,
    "translation": "フー"
  },
  "alarm": { "dummy": 12345 }
}
```

| Key | Value |
|----|----|
| words | Registered words |
| words[\<wordKey>] | A record for each word. `wordKey` format is \<srcLang>\<dstLang>:\<word> |
| words[\<wordKey>].translation | Translation for the word specified by `wordKey` |
| words[\<wordKey>].correctCount | Count of correct for its quiz. |
| quizQueue[] | Queue for quiz. Each element in queue is `wordKey` |
| quiz | Current active quiz content |
| quiz.wordKey | `wordKey` |
| quiz.choices | Choices provided to client |
| quiz.answer | Correct answer index number [0, 4] (4 means "none of the above") |
| quiz.translation | Translation |
| alarm | Dummy data for firing storage.onChange on alarm change |

# Message Design
## registerWord
content_script -> background
### request
```
{ "msgType": "registerWord",
  "wordKey": "<srcLang><dstLang>:<word>"
  "translation": <translation> }
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
{ "wordKey": "<srcLang><dstLang>:<word>"
  "choices":[ <choice1>, <choice2>, <choice3>, <choice4> ] }
```
## answerQuiz
browser_action -> background
### request
```
{ "msgType": "answerQuiz",
  "wordKey": "<srcLang><dstLang>:<word>"
  "guess": <index> }
```
| Key | Value |
|----|----|
| guess | range is [0, 4] |
### response
```
{ "result": <boolean>,
  "answer": <index>,
  "translation": <translation> }
```
| Key | Value |
|----|----|
| answer | range is [0, 4] |
| translation | Optional. Present only when <index> is 4. |
## option page related messages
omitted
