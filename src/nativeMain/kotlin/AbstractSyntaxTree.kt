abstract class Expr {
    interface Visitor<T> {
        fun visitLiteral(literal: Literal): T
        fun visitAssignment(assignment: AssignmentExpr): T
        fun visitProperty(property: Property): T
        fun visitFunc(func: Func): T
        fun visitFcall(fcall: FCallExpr): T
        fun visitBinOp(binop: BinOp): T
        fun visitGroup(group: Group): T
        fun visitVariable(variable: Variable): T
        fun visitProto(proto: Proto): T
        fun visitIndexer(indexer: Indexer): T
        fun visitList(list: OasisList): T
    }
    var line = 0
    abstract fun <T> accept(visitor: Visitor<T>): T
}

abstract class Stmt {
    interface Visitor<T> {
        fun visitLet(let: Let): T
        fun visitIfStmt(ifstmt: IfStmt): T
        fun visitWhileStmt(whilestmt: WhileStmt): T
        fun visitStmtList(stmtlist: StmtList): T
        fun visitReturnStmt(retstmt: RetStmt): T
        fun visitExprStmt(exprStmt: ExprStmt): T
    }
    var line = 0
    abstract fun <T> accept(visitor: Visitor<T>): T
}

class ExprStmt(val expr: Expr) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitExprStmt(this)
    }

    override fun toString(): String {
        return "ExprStmt($expr"
    }
}

class BinOp(val left: Expr, val operator: Token, val right: Expr) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitBinOp(this)
    }
    override fun toString(): String {
        return "BinOp($left ${operator.lexeme} $right)"
    }
}

class Literal(val value: Any?) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitLiteral(this)
    }
    override fun toString(): String {
        return "Literal($value)"
    }
}

class AssignmentExpr(val left: Expr, val value: Expr) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitAssignment(this)
    }
    override fun toString(): String {
        return "Assign($left = $value)"
    }
}

class Let(val left: Token, val value: Expr) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitLet(this)
    }

    override fun toString(): String {
        return "Let(${left.lexeme} = $value)"
    }
}

class StmtList(val stmts: List<Stmt>) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitStmtList(this)
    }
    override fun toString(): String {
        return "StmtList($stmts)"
    }
}

class Property(val obj: Expr, val indexer: Token) : Expr(){
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitProperty(this)
    }
    override fun toString(): String {
        return "Property($obj : ${indexer.lexeme})"
    }
}

class Func(val operands: List<Token>, val body: StmtList): Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitFunc(this)
    }
    override fun toString(): String {
        return "Func(${operands} : $body)"
    }
}

class FCallExpr(val func: Expr, val operands: List<Expr>) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitFcall(this)
    }
    override fun toString(): String {
        return "Call(${func} : ${operands})"
    }
}

class Group(val expr: Expr) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitGroup(this)
    }
    override fun toString(): String {
        return "Group($expr)"
    }
}

class IfStmt(val expr: Expr, val stmtlist: StmtList, val elseBody: StmtList?) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitIfStmt(this)
    }
}

class WhileStmt(val expr: Expr, val body: StmtList) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitWhileStmt(this)
    }
}

class Variable(val name: Token) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitVariable(this)
    }
    override fun toString(): String {
        return "Variable($name)"
    }
}

class Proto(val base: Token?, val body: StmtList): Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitProto(this)
    }
    override fun toString(): String {
        return "Variable($base : $body)"
    }
}

class RetStmt(val expr: Expr?): Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitReturnStmt(this)
    }

    override fun toString(): String {
        return "Return($expr)"
    }
}

class Indexer(val expr: Expr, val index: Expr): Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitIndexer(this)
    }

    override fun toString(): String {
        return "Index($expr : $index)"
    }
}

class OasisList(val exprs: ArrayList<Expr>): Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitList(this)
    }
}