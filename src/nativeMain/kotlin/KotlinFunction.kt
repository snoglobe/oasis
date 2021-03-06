class KotlinFunction0<T>(val function: () -> T) : OasisCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        return function()
    }

    override fun arity(): Int {
        return 0
    }
}

class KotlinFunction1<T, A>(val function: (a: A) -> T) : OasisCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        return function(arguments[0] as A)
    }

    override fun arity(): Int {
        return 1
    }
}

class KotlinFunction2<T, A, B>(val function: (a: A, b: B) -> T) : OasisCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        return function(arguments[0] as A, arguments[1] as B)
    }

    override fun arity(): Int {
        return 2
    }
}

class KotlinFunction3<T, A, B, C>(val function: (a: A, b: B, c: C) -> T) : OasisCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        return function(arguments[0] as A, arguments[1] as B, arguments[2] as C)
    }

    override fun arity(): Int {
        return 3
    }
}
