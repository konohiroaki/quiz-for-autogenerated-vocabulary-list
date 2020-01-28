import org.w3c.dom.url.URL
import kotlin.browser.window

external val chrome: dynamic

fun main() {
    GoogleTranslatePlugin().addListener()

    when (URL(window.location.href).hostname) {
        "eow.alc.co.jp" -> AlcCoJp().register()
        "ejje.weblio.jp" -> WeblioJp().register()
    }
}
