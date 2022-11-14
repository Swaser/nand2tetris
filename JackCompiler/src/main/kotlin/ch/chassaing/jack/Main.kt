package ch.chassaing.jack

import java.io.File

fun main(args: Array<String>) {

    if (args.size != 1) {
        println("Must have one argument")
    }

    val bufferedReader = File(args[0]).bufferedReader()
    val tokenizer = Tokenizer(bufferedReader)

    var token = tokenizer.advance()
    while (token != null) {
        println(token)
        token = tokenizer.advance()
    }
}
