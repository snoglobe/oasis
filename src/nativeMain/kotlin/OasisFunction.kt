class OasisFunction(private val declaration: Func, val closure: Environment) : OasisCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment= Environment(closure)
        for(i in (0 until declaration.operands.size)) {
            environment.define(declaration.operands[i].lexeme, arguments[i])
        }
        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return) {
            return returnValue.value
        }
        return null
    }

    override fun arity(): Int {
        return declaration.operands.size
    }

}