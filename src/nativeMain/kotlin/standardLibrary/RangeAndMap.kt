package standardLibrary

import KotlinFunction1
import Oasis
import OasisCallable
import OasisPrototype
import globalInterpreter

fun range(base: Double, ceil: Double): ArrayList<Int> {
    return if(base > ceil) {
        ArrayList(
            (ceil.toInt() .. base.toInt())
                .iterator()
                .asSequence()
                .toList()
                .reversed())
    } else {
        ArrayList(
            (base.toInt() .. ceil.toInt())
                .iterator()
                .asSequence()
                .toList())
    }
}

fun createRange(vals: ArrayList<Int>) : OasisPrototype {
    var rangeProto = OasisPrototype(base, -1)
    rangeProto.set("iter", KotlinFunction1<OasisPrototype, OasisCallable> {
        func -> createRange(vals.map {x -> func.call(globalInterpreter!!, listOf(x))} as ArrayList<Int>)
    })
    rangeProto.set("list", vals)
    return rangeProto
}

fun rangeFun(base: Double, ceil: Double) = createRange(range(base, ceil))