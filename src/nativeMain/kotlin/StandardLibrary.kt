
import kotlinx.cinterop.*
import platform.posix.localtime
import platform.posix.time
import platform.posix.tm
import kotlin.system.getTimeMillis

@ThreadLocal
var base = OasisPrototype(null, -1)
object StandardLibrary {
    fun generateLib(x: Environment) {
        base.set("toString", KotlinFunction0 { return@KotlinFunction0 "<obj>" })
        val io = OasisPrototype(base, -1)
        val time = OasisPrototype(base, -1)
        val list = OasisPrototype(base, -1)
        val sys = OasisPrototype(base, -1)
        val string = OasisPrototype(base, -1)
        io.set("print", KotlinFunction1<Unit, Any?> { z ->  println(z.toString())} )
        io.set("read", KotlinFunction0(::readLine))
        io.set("open", KotlinFunction1(Oasis::readAllText))
        io.set("write", KotlinFunction2(Oasis::writeAllText))
        time.set("clock", KotlinFunction0 { getTimeMillis() / 1000.0 })
        time.set("now", KotlinFunction0 {
            val currentTime = OasisPrototype(base, -1)
            memScoped{
                val ttime: Long = time(null)
                val tmStruct: CPointer<tm> = localtime(cValuesOf(ttime.convert<Long>()))!!
                currentTime.set("year", tmStruct.pointed.tm_year + 1900)
                currentTime.set("month", tmStruct.pointed.tm_mon)
                currentTime.set("day", tmStruct.pointed.tm_mday)
                currentTime.set("hour", tmStruct.pointed.tm_hour)
                currentTime.set("min", tmStruct.pointed.tm_min)
                currentTime.set("sec", tmStruct.pointed.tm_sec)
            }

            return@KotlinFunction0 currentTime
        })
        list.set("add", KotlinFunction2<Unit, ArrayList<Any?>, Any?> { z, y -> z.add(y)})
        list.set("size", KotlinFunction1<Double, ArrayList<Any?>> { z -> z.size.toDouble() })
        list.set("remove", KotlinFunction2<Any?, ArrayList<Any?>, Double> {z, y -> val tZ = z[y.toInt()]; z.remove(y); return@KotlinFunction2 tZ})
        string.set("size", KotlinFunction1<Double, String> {z -> z.length.toDouble()})
        x.define("io", io)
        x.define("time", time)
        x.define("list", list)
        x.define("sys", sys)
    }
}
