package io.github.jsixface.codexvert.utils

class AspectRatio(private val w: Float, private val h: Float) {

    override fun toString(): String {
        val (wi, hi) = when {
            w < 10 || h < 10 -> w to h
            w > h -> w / h to 1.0f
            else -> 1.0f to h / w
        }
        return "${wi.shortString()}:${hi.shortString()}"
    }

    companion object {
        operator fun invoke(ratio: String): AspectRatio {
            val measures = ratio.split(":").mapNotNull { it.toFloatOrNull() }
            assert(measures.size == 2) { "Should be in the format 'w:h" }
            return AspectRatio(measures[0], measures[1])
        }
    }
}