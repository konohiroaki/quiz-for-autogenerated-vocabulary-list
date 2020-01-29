import org.w3c.dom.url.URL
import websites.AlcCoJp
import websites.WeblioJp
import kotlin.browser.window

external val chrome: dynamic

fun main() {
    when (URL(window.location.href).hostname) {
        "eow.alc.co.jp" -> AlcCoJp().register()
        "ejje.weblio.jp" -> WeblioJp().register()
    }
}
