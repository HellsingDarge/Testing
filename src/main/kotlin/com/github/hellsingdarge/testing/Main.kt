package com.github.hellsingdarge.testing

import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>)
{

    val path = if (args.isNotEmpty())
    {
        args.first()
    }
    else
    {
        "lorem.txt"
    }

    val file = File(path)

    if (!file.exists())
    {
        println("Specified path doesn't exist")
        exitProcess(1)
    }

    if (!file.isFile)
    {
        println("Path doesn't lead to file")
        exitProcess(1)
    }

    if (!file.canRead())
    {
        println("Can't read specified file")
        exitProcess(1)
    }

    // file exists, can be read, but empty
    if (file.length() == 0L)
    {
        println(0)
        exitProcess(0)
    }

    val count = file.useLines { it.first().split(" ").count { line -> line.isNotBlank() } }
    println(count)
}