package standardLibrary
import Environment
import Interpreter
import KotlinFunction0
import KotlinFunction1
import KotlinFunction2
import Oasis
import OasisCallable
import OasisPrototype
import kotlinx.cinterop.*
import platform.posix.getchar
import platform.posix.localtime
import platform.posix.time
import platform.posix.tm
import kotlin.system.getTimeMillis

@ThreadLocal
var base = OasisPrototype(null, -1)
object StandardLibrary {
    fun generateLib(x: Environment, i: Interpreter) {
        base.set("toString", KotlinFunction0 { return@KotlinFunction0 "<obj>" })
        val io = OasisPrototype(base, -1)
        val time = OasisPrototype(base, -1)
        val list = OasisPrototype(base, -1)
        val sys = OasisPrototype(base, -1)
        val string = OasisPrototype(base, -1)
        val type = OasisPrototype(base, -1)
        val socket = OasisPrototype(base, -1)
        io.set("print", KotlinFunction1<Unit, Any?> { z -> println(z.toString()) })
        io.set("read", KotlinFunction0(::readLine))
        io.set("readc", KotlinFunction0 { return@KotlinFunction0 getchar().toChar() })
        io.set("open", KotlinFunction1(Oasis::readAllText))
        io.set("write", KotlinFunction2(Oasis::writeAllText))
        io.set("printf", KotlinFunction2<Unit, String, OasisCallable> { z, y -> print(y.call(i, listOf(z))) })
        time.set("clock", KotlinFunction0 { getTimeMillis() / 1000.0 })
        time.set("now", KotlinFunction0 {
            val currentTime = OasisPrototype(base, -1)
            memScoped {
                val ttime: Long = time(null)
                val tmStruct: CPointer<tm> = localtime(cValuesOf(ttime.convert<Long>()))!!
                currentTime.set("year", tmStruct.pointed.tm_year + 1900)
                currentTime.set("month", tmStruct.pointed.tm_mon)
                currentTime.set("day", tmStruct.pointed.tm_mday)
                currentTime.set("hour", tmStruct.pointed.tm_hour)
                currentTime.set("min", tmStruct.pointed.tm_min)
                currentTime.set("sec", tmStruct.pointed.tm_sec)
                currentTime.set("toString", KotlinFunction0 {
                    "${currentTime.get("hour")}:${currentTime.get("min")}:${currentTime.get("sec")} ${currentTime.get("day")}/${currentTime.get("month")}/${currentTime.get("year")}"
                })
            }

            return@KotlinFunction0 currentTime
        })
        list.set("add", KotlinFunction2<Unit, ArrayList<Any?>, Any?> { z, y -> z.add(y) })
        list.set("size", KotlinFunction1<Double, ArrayList<Any?>> { z -> z.size.toDouble() })
        list.set("remove",
            KotlinFunction2<Any?, ArrayList<Any?>, Double> { z, y ->
                val tZ = z[y.toInt()]; z.remove(y); return@KotlinFunction2 tZ
            })
        string.set("size", KotlinFunction1<Double, String> { z -> z.length.toDouble() })
        type.set("string", KotlinFunction1<String, Any?> { z -> z.toString() })
        type.set("char", KotlinFunction1<Char, Any?> { z -> z as Char })
        type.set("num", KotlinFunction1<Double, Any?> { z -> z.toString().toDouble() })
        type.set("bytes", KotlinFunction1<ByteArray, Any?> { z ->
            if (z is List<*>) {
                val r = ByteArray(z.size)
                var li = 0
                z.map {v -> r[li] = when (v) {
                    is Double -> v.toInt().toByte()
                    is Int -> v.toByte()
                    is Boolean -> if (v) 0x1 else 0x0
                    is Char -> v.code.toByte()
                    else -> throw Exception("'${v} cannot be converted to byte")
                }; li += 1}
                return@KotlinFunction1 r
            } else if (z is String) {
                val r = ByteArray(z.length)
                var li = 0
                z.map {v -> r[li] = v.code.toByte(); li += 1}
                return@KotlinFunction1 r
            } else {
                throw Exception("'${z.toString()}' cannot be represented as a bytes-object")
            }
        })
        socket.set("open", KotlinFunction1(::constructSocket))
        io.set("socket", socket)
        x.define("io", io)
        x.define("time", time)
        x.define("list", list)
        x.define("sys", sys)
        x.define("type", type)
        x.define("string", string)
    }
}
