package com.github.hellsingdarge.testing

object Helpers
{
    fun randomString(length: Int = 16): String
    {
        val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        return (1..length)
                .map { charPool.random() }
                .joinToString("")
    }
}

