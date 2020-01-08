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
    window.onload = { prepareNextQuiz() }
}

fun addOptionsPageLink() {
    select<HTMLButtonElement>("#go-to-options").addEventListener("click", {
        chrome.runtime.openOptionsPage()
    })
}

fun setNextQuizButton() {
    val nextButton = select<HTMLDivElement>("#go-next-quiz")
    nextButton.addEventListener("click", { prepareNextQuiz() })
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

fun setWord(word: String) {
    select<HTMLHeadingElement>("#word").innerText = word
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

fun choiceDom(text: String, word: String, actual: Int = CHOICE_COUNT): HTMLElement {
    return document.create.button(classes = "list-group-item list-group-item-action") {
        style = "outline:none;"
        +text
        onClickFunction = { answerQuiz(word, actual) }
    }
}

fun answerQuiz(word: String, actual: Int) {
    chrome.runtime.sendMessage(null, answerQuizRequest(word, actual)) { response ->
        printlnWithTime("actual  : $actual")
        printlnWithTime("expected: ${response.expected}")

        val choices = selectAll<HTMLButtonElement>("#choices > button")
        if (!response.result) {
            choices[actual].setAttribute("style", "background-color:#ffdddd")
        }
        choices[response.expected].setAttribute("style", "background-color:#ddffdd")

        showNextQuizButton()
    }
}

fun answerQuizRequest(word: String, actual: Int): dynamic {
    return createProps(
        "msgType", "answerQuiz",
        "word", word,
        "actual", actual
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

