class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {

    private var globals = Environment()
    var environment: Environment = globals

    init {
        StandardLibrary.generateLib(environment)
    }

    inline fun eval(expr: Expr): Any? {
        return expr.accept(this)
    }

    inline fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    inline fun execute(stmtList: StmtList) {
        executeBlock(stmtList, environment)
    }

    inline fun executeBlock(stmtList: StmtList, env: Environment) {
        val previous: Environment = environment
        try {
            environment = env
            stmtList.accept(this)
        } finally {
            environment = previous
        }
    }

    private inline fun isTruthy(thing: Any?): Boolean {
        return thing != null && thing != false && thing != 0
    }

    override fun visitLiteral(literal: Literal): Any? {
        return literal.value
    }

    override fun visitAssignment(assignment: AssignmentExpr): Any? {
        var a = eval(assignment.value)
        when(assignment.left) {
            is Property -> (eval(assignment.left.obj) as OasisPrototype).set(assignment.left.indexer.lexeme, a)
            is Variable -> environment.assign(assignment.left.name, a)
            is Indexer -> (eval(assignment.left.expr) as ArrayList<Any?>).set((eval(assignment.left.index) as Double).toInt(), a)
            else -> {
                throw RuntimeError(assignment.line, "Cannot assign")
            }
        }
        return a
    }

    override fun visitProperty(property: Property): Any? {
        return (eval(property.obj) as OasisPrototype).get(property.indexer.lexeme)
    }

    override fun visitFunc(func: Func): Any? {
        return OasisFunction(func, environment)
    }

    override fun visitFcall(fcall: FCallExpr): Any? {
        var callee: Any? = eval(fcall.func)
        var arguments: ArrayList<Any?> = ArrayList()
        for(argument: Expr in fcall.operands) {
            arguments.add(eval(argument))
        }
        var function: OasisCallable = callee as OasisCallable
        return function.call(this, arguments)
    }

    override fun visitBinOp(binop: BinOp): Any? {
        var left = eval(binop.left)
        var right = eval(binop.right)
        when(binop.operator.type) {
            TokenType.PLUS -> {
                when(left) {
                    is OasisPrototype -> return (left.get("__plus") as OasisCallable).call(this, listOf(right))
                    is Double -> return left + right as Double
                    is String -> return  left + right.toString()
                    else -> throw RuntimeError(binop.line, "Cannot add")
                }
            }
            TokenType.MINUS -> {
                when(left) {
                    is OasisPrototype -> return (left.get("__sub") as OasisCallable).call(this, listOf(right))
                    is Double -> return left + right as Double
                    else -> throw RuntimeError(binop.line, "Cannot subtract")
                }
            }
            TokenType.STAR -> {
                when(left) {
                    is OasisPrototype -> return (left.get("__mul") as OasisCallable).call(this, listOf(right))
                    is Double -> return left * right as Double
                    else -> throw RuntimeError(binop.line, "Cannot multiply")
                }
            }
            TokenType.SLASH -> {
                when(left) {
                    is OasisPrototype -> return (left.get("__div") as OasisCallable).call(this, listOf(right))
                    is Double -> return left * right as Double
                    else -> throw RuntimeError(binop.line, "Cannot divide")
                }
            }
            TokenType.EQUAL_EQUAL -> {
                if (left != null) {
                    return left == right
                } else {
                    if (right == null) {
                        return true
                    }
                    return false
                }
            }
            TokenType.BANG_EQUAL -> {
                if (left != null) {
                    return left != right
                } else {
                    if (right != null) {
                        return true
                    }
                    return false
                }
            }
            TokenType.GREATER -> {
                return (left as Double) > (right as Double)
            }
            TokenType.GREATER_EQUAL -> {
                return (left as Double) >= (right as Double)
            }
            TokenType.LESS -> {
                return (left as Double) < (right as Double)
            }
            TokenType.LESS_EQUAL -> {
                return (left as Double) <= (right as Double)
            }
            else -> throw RuntimeError(binop.line, "Invalid operator")
        }
    }

    override fun visitGroup(group: Group): Any? {
        return group.expr.accept(this)
    }

    override fun visitVariable(variable: Variable): Any? {
        return environment.get(variable.name)
    }

    override fun visitProto(proto: Proto): Any {
        var protoType: OasisPrototype = OasisPrototype((if (proto.base != null) environment.get(proto.base) else base) as OasisPrototype?, proto.line)
        for(x in proto.body.stmts) {
            protoType.set((x as Let).left.lexeme, eval(x.value))
        }
        return protoType
    }

    override fun visitLet(let: Let) {
        environment.define(let.left.lexeme, eval(let.value))
    }

    override fun visitIfStmt(ifstmt: IfStmt) {
        if(isTruthy(eval(ifstmt.expr))) {
            execute(ifstmt.stmtlist)
        } else {
            ifstmt.elseBody?.let { execute(it) }
        }
    }

    override fun visitWhileStmt(whilestmt: WhileStmt) {
        while(isTruthy(eval(whilestmt.expr))) {
            execute(whilestmt.body)
        }
    }

    override fun visitStmtList(stmtlist: StmtList) {
        for(stmt in stmtlist.stmts) {
            execute(stmt)
        }
    }

    override fun visitReturnStmt(retstmt: RetStmt) {
        throw Return(if (retstmt.expr != null) eval(retstmt.expr) else null)
    }

    override fun visitExprStmt(exprStmt: ExprStmt) {
        exprStmt.expr.accept(this)
    }

    override fun visitIndexer(indexer: Indexer): Any? {
        var x = eval(indexer.expr)
        when(x) {
            is String -> return x[(eval(indexer.index) as Double).toInt()]
            is ArrayList<*> -> return x[(eval(indexer.index) as Double).toInt()]
            else -> throw throw RuntimeError(indexer.line, "Cannot index")
        }
    }

    override fun visitList(list: OasisList): Any? {
        var ev = ArrayList<Any?>()
        for(i in list.exprs) {
            ev.add(eval(i))
        }
        return ev
    }

}