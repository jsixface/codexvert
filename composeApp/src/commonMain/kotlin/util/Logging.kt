package util

import io.github.aakira.napier.Napier

fun log(msg: String) {
    Napier.i { msg }
}
