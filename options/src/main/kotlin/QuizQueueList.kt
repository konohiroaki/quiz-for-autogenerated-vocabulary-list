import Util.Companion.select
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.id
import org.w3c.dom.HTMLDivElement
import kotlin.browser.document

class QuizQueueList {

    companion object {

        fun update(quizQueue: Array<String>) {
            select<HTMLDivElement>("#quizQueue").replaceWith(document.create.div {
                id = "quizQueue"
                quizQueue.forEach { div("list-group-item list-group-item-action") { +it } }
            })
        }
    }
}
