
import kotlinx.cinterop.*
import platform.posix.*

fun main(args: Array<String>) {
    Oasis.main(args)
}
var globalInterpreter : Interpreter? = null
object Oasis {
    fun readAllText(filePath: String): String {
        val returnBuffer = StringBuilder()
        val file = fopen(filePath, "r") ?:
        throw IllegalArgumentException("Cannot open input file $filePath")

        try {
            memScoped {
                val readBufferLength = 64 * 1024
                val buffer = allocArray<ByteVar>(readBufferLength)
                var line = fgets(buffer, readBufferLength, file)?.toKString()
                while (line != null) {
                    returnBuffer.append(line)
                    line = fgets(buffer, readBufferLength, file)?.toKString()
                }
            }
        } finally {
            fclose(file)
        }

        return returnBuffer.toString()
    }

    fun writeAllText(filePath:String, text:String) {
        val file = fopen(filePath, "w") ?:
        throw IllegalArgumentException("Cannot open output file $filePath")
        try {
            memScoped {
                if(fputs(text, file) == EOF) throw Error("File write error")
            }
        } finally {
            fclose(file)
        }
    }

    fun main(args: Array<String>) {
        if (args.contains("--welcome") or args.contains("-w")) {
            println("Welcome to the Oasis!")
        }
        var interpreter = Interpreter()
        globalInterpreter = interpreter
        (interpreter.environment.get(Token(TokenType.IDENTIFIER, "sys", null, -1)) as OasisPrototype).set("argv", ArrayList<Any?>())
        var program = args.find {
            var result = false
            memScoped {
                var buffer: stat = alloc()
                var exist = stat(it, buffer.ptr)
                result = exist == 0
            }
            return@find result
        }
        if(program != null) {
            (interpreter.environment.get(Token(TokenType.IDENTIFIER, "sys", null, -1)) as OasisPrototype).set("argv", args.copyOfRange(1, args.size).toCollection(ArrayList<Any?>()))
            var scanner = Scanner(readAllText(program))
            var tokens = scanner.scanTokens()
            var parser = Parser(tokens)
            var ast = parser.parse()
            try {
                interpreter.execute(ast)
            }
            catch (e: RuntimeError) {
                error(e.line, e.s)
                exit(1)
            } /*catch (e: Exception) {
                error(-1, e.toString())
                exit(1)
            }*/
        } else while(true) {
            print("oasis> ")
            var scanner: Scanner? = readLine()?.let { Scanner(it) }
            if(scanner != null){
                var tokens: List<Token> = scanner.scanTokens()
                var parser = Parser(tokens)
                var ast = parser.parse()
                try {
                    interpreter.execute(ast)
                }
                catch (e: RuntimeError) {
                    error(e.line, e.s)
                } catch (e: Exception) {
                    println(e)
                }

            }
        }
    }

    fun error(line: Int, reason: String) {
        println("On line $line: $reason")
    }
}
