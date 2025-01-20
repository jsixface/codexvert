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
        fun from(ratio: String): AspectRatio? {
            val measures = ratio.split(":").mapNotNull { it.toFloatOrNull() }
            return if (measures.size == 2) AspectRatio(measures[0], measures[1]) else null
        }
    }
}