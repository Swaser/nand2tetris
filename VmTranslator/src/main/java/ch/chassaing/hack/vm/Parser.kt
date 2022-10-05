package ch.chassaing.hack.vm

import ch.chassaing.hack.vm.command.*
import org.apache.commons.lang3.StringUtils
import java.util.*

class Parser(private val lines: List<String>) : IParser {

    private var currentLine = -1
    private lateinit var fields: Array<String>

    override fun advance(): Int {
        while (++currentLine < lines.size) {
            val nextLine = StringUtils.trim(lines[currentLine])
            if (nextLine.startsWith("//") || StringUtils.isBlank(nextLine)) {
                continue
            }
            fields = StringUtils.split(nextLine)
            return currentLine
        }
        return -1
    }

    override fun command(): Command {
        val segment: Segment
        val position: Int
        return when (fields[0]) {
            "push" -> {
                segment = Segment.valueOf(fields[1].uppercase(Locale.getDefault()))
                position = fields[2].toInt()
                Push(currentLine, segment, position)
            }

            "pop" -> {
                segment = Segment.valueOf(fields[1].uppercase(Locale.getDefault()))
                position = fields[2].toInt()
                Pop(currentLine, segment, position)
            }

            "add" -> Add(currentLine)
            "sub" -> Sub(currentLine)
            "and" -> And(currentLine)
            "or" -> Or(currentLine)
            "eq" -> Eq(currentLine)
            "lt" -> Lt(currentLine)
            "gt" -> Gt(currentLine)
            "neg" -> Neg(currentLine)
            "not" -> Not(currentLine)
            else -> throw UnsupportedOperationException("Unknown command " + fields[0])
        }
    }
}