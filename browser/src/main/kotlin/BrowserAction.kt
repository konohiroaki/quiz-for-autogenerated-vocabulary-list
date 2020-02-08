import Util.Companion.CHOICE_COUNT
import Util.Companion.createProps
import Util.Companion.printlnWithTime
import Util.Companion.select
import Util.Companion.selectAll
import kotlinx.html.button
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import kotlinx.html.style
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLHeadingElement
import kotlin.browser.document
import kotlin.browser.window

external val chrome: dynamic

fun main() {
    addOptionsPageLink()
    setNextQuizButton()
//    window.onload = { prepareNextQuiz() }
    window.onload = { prepareNextQuizV2() }
}

fun addOptionsPageLink() {
    select<HTMLButtonElement>("#go-to-options").addEventListener("click", {
        chrome.runtime.openOptionsPage()
    })
}

fun setNextQuizButton() {
    val nextButton = select<HTMLDivElement>("#go-next-quiz")
//    nextButton.addEventListener("click", { prepareNextQuiz() })
    nextButton.addEventListener("click", { prepareNextQuizV2() })
}

fun prepareNextQuiz() {
    chrome.runtime.sendMessage(null, createProps("msgType", "requestQuiz")) { response ->
        printlnWithTime(JSON.stringify(response))

        hideNextQuizButton()
        emptyChoices()
        if (response.word == undefined) {
            showNoQuizMessage()
            setWord("")
        } else {
            hideNoQuizMessage()
            setWord(response.word)
            setChoices(response)
        }
    }
}

fun prepareNextQuizV2() {
    chrome.runtime.sendMessage(null, createProps("msgType", "requestQuiz")) { response ->
        printlnWithTime(JSON.stringify(response))

        hideNextQuizButton()
        emptyChoices()
        if (response.wordKey == undefined) {
            showNoQuizMessage()
            setWordV2("")
        } else {
            hideNoQuizMessage()
            setWordV2(response.wordKey)
            setChoicesV2(response)
        }
    }
}

fun setWord(word: String) {
    select<HTMLHeadingElement>("#word").innerText = word
}

// TODO: show language information somewhere.
// TODO: wordKey can be empty string. handle it properly.
fun setWordV2(wordKey: String) {
    select<HTMLHeadingElement>("#word").innerText = Languages.getWord(wordKey)
}

fun emptyChoices() {
    select<HTMLDivElement>("#choices").innerHTML = ""
}

fun setChoices(response: dynamic) {
    val choices = select<HTMLDivElement>("#choices")
    repeat(CHOICE_COUNT) { idx ->
        val choiceButton = choiceDom(response.choices[idx], response.word, idx)
        choices.appendChild(choiceButton)
    }
    val choiceButton = choiceDom("上記のどれでもない", response.word)
    choices.appendChild(choiceButton)
}

fun setChoicesV2(response: dynamic) {
    val choices = select<HTMLDivElement>("#choices")
    repeat(CHOICE_COUNT) { idx ->
        val choiceButton = choiceDomV2(response.wordKey, response.choices[idx], idx)
        choices.appendChild(choiceButton)
    }
    val choiceButton = choiceDomV2(response.wordKey, "上記のどれでもない")
    choices.appendChild(choiceButton)
}

fun choiceDom(text: String, word: String, guess: Int = CHOICE_COUNT): HTMLElement {
    return document.create.button(classes = "list-group-item list-group-item-action") {
        style = "outline:none;"
        +text
        onClickFunction = { answerQuiz(word, guess) }
    }
}

fun choiceDomV2(wordKey: String, translation: String, guess: Int = CHOICE_COUNT): HTMLElement {
    return document.create.button(classes = "list-group-item list-group-item-action") {
        style = "outline:none;"
        +translation
        onClickFunction = { answerQuizV2(wordKey, guess) }
    }
}

fun answerQuiz(word: String, guess: Int) {
    chrome.runtime.sendMessage(null, answerQuizRequest(word, guess)) { response ->
        printlnWithTime("guess : $guess")
        printlnWithTime("answer: ${response.answer}")

        val choices = selectAll<HTMLButtonElement>("#choices > button")
        if (!response.result) {
            choices[guess].style.backgroundColor = "#ffdddd"
        }
        choices[response.answer].style.backgroundColor = "#ddffdd"
        if (response.answer == 4) {
            choices[response.answer].style.color = "#0044ff"
            choices[response.answer].innerText = response.translation
        }

        showNextQuizButton()
    }
}

fun answerQuizV2(wordKey: String, guess: Int) {
    chrome.runtime.sendMessage(null, answerQuizRequestV2(wordKey, guess)) { response ->
        printlnWithTime("guess : $guess")
        printlnWithTime("answer: ${response.answer}")

        val choices = selectAll<HTMLButtonElement>("#choices > button")
        if (!response.result) {
            choices[guess].style.backgroundColor = "#ffdddd"
        }
        choices[response.answer].style.backgroundColor = "#ddffdd"
        if (response.answer == 4) {
            choices[response.answer].style.color = "#0044ff"
            choices[response.answer].innerText = response.translation
        }

        showNextQuizButton()
    }
}

fun answerQuizRequest(word: String, guess: Int): dynamic {
    return createProps(
        "msgType", "answerQuiz",
        "word", word,
        "guess", guess
    )
}

fun answerQuizRequestV2(wordKey: String, guess: Int): dynamic {
    return createProps(
        "msgType", "answerQuiz",
        "wordKey", wordKey,
        "guess", guess
    )
}

fun showNoQuizMessage() = select<HTMLDivElement>("#no-quiz-alert").removeAttribute("style")
fun hideNoQuizMessage() = select<HTMLDivElement>("#no-quiz-alert").setAttribute("style", "display:none")
fun showNextQuizButton() {
    val nextButton = select<HTMLDivElement>("#go-next-quiz")
    val choices = select<HTMLDivElement>("#choices")
    nextButton.setAttribute(
        "style", "position:absolute;z-index:1;" +
                "height:${choices.clientHeight}px;width:${choices.clientWidth}px;" +
                "top:${choices.offsetTop}px;left:${choices.offsetLeft}px"
    )
}

fun hideNextQuizButton() = select<HTMLDivElement>("#go-next-quiz").setAttribute("style", "display:none")
