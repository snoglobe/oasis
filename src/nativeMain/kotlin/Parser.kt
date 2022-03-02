import TokenType.*

class Parser(private val tokens: List<Token>) {
    private var current: Int = 0

    private var operators: List<TokenType> = listOf<TokenType>(
        MINUS, PLUS, SLASH, STAR,
        BANG, BANG_EQUAL,
        EQUAL, EQUAL_EQUAL,
        GREATER, GREATER_EQUAL,
        LESS, LESS_EQUAL
    )

    private fun peek(type: TokenType): Boolean {
        return tokens[current].type == type
    }

    private fun eat(type: TokenType): Token {
        if (!peek(type)) {
            println(tokens)
            Oasis.error(tokens[current].line, "Unexpected token ${tokens[current].lexeme}, expected ${type.name}")
            throw Exception()
        }
        return tokens[current++]
    }

    private fun fnDef(): Expr {
        eat(FN)
        eat(LEFT_PAREN)
        var operands: ArrayList<Token> = ArrayList()
        if (peek(IDENTIFIER)) {
            operands.add(eat(IDENTIFIER))
            while (!peek(RIGHT_PAREN)) {
                eat(COMMA)
                operands.add(eat(IDENTIFIER))
            }
        }
        eat(RIGHT_PAREN)
        var body: ArrayList<Stmt> = ArrayList()
        while (!peek(END)) {
            body.add(statement())
        }
        eat(END)
        return Func(operands, StmtList(body))
    }

    private fun proto(): Expr {
        eat(PROTO)
        var base: Token? = null
        if (peek(GREATER)) {
            eat(GREATER)
            base = eat(IDENTIFIER)
        }
        var body: ArrayList<Let> = ArrayList()
        while (!peek(END)) {
            body.add(lets())
        }
        eat(END)
        return Proto(base, StmtList(body))
    }

    private fun expression(): Expr {
        var result: Expr = Literal(null)
        if (peek(IDENTIFIER)) {
            result = Variable(eat(IDENTIFIER))
        } else if (peek(FN)) {
            result = fnDef()
        } else if (peek(NUMBER)) {
            result = Literal(eat(NUMBER).literal)
        } else if (peek(STRING)) {
            result = Literal(eat(STRING).literal)
        } else if(peek(CHAR)) {
            result = Literal(eat(CHAR).literal)
        } else if (peek(TRUE)) {
            eat(TRUE)
            result = Literal(true)
        } else if (peek(FALSE)) {
            eat(FALSE)
            result = Literal(false)
        } else if (peek(PROTO)) {
            result = proto()
        } else if (peek(LEFT_PAREN)) {
            eat(LEFT_PAREN)
            result = Group(expression())
            eat(RIGHT_PAREN)
        } else if (peek(NIL)) {
            eat(NIL)
            // nothing...
        } else if (peek(LBRAC)) {
            eat(LBRAC)
            var body: ArrayList<Expr> = ArrayList()
            if (!peek(RBRAC)) {
                body.add(expression())
                while (!peek(RBRAC)) {
                    eat(COMMA)
                    body.add(expression())
                }
            }
            eat(RBRAC)
            result = OasisList(body)
        } else {
            Oasis.error(tokens[current].line, "Invalid expression")
            println(tokens.slice(current until tokens.size))
            throw Exception()
        }

        if (peek(COLON)) {
            eat(COLON)
            if(peek(IDENTIFIER))
                result = Property(result, eat(IDENTIFIER))
            else if(peek(LEFT_PAREN)) {
                eat(LEFT_PAREN)
                result = Indexer(result, expression())
                eat(RIGHT_PAREN)
            }

            while (peek(COLON)) {
                eat(COLON)
                if(peek(IDENTIFIER))
                    result = Property(result, eat(IDENTIFIER))
                else if(peek(LEFT_PAREN)) {
                    eat(LEFT_PAREN)
                    result = Indexer(result, expression())
                    eat(RIGHT_PAREN)
                }
            }
        }

        if (peek(EQUAL)) {
            eat(EQUAL)
            result = AssignmentExpr(result, expression())
        }
        if (operators.contains(tokens[current].type)) {
            while (operators.contains(tokens[current].type)) {
                result = BinOp(result, eat(tokens[current].type), expression())
            }
        }
        if (peek(LEFT_PAREN)) {
            eat(LEFT_PAREN)
            var operands: ArrayList<Expr> = ArrayList()
            if (!peek(RIGHT_PAREN)) {
                operands.add(expression())
                while (!peek(RIGHT_PAREN)) {
                    eat(COMMA)
                    operands.add(expression())
                }
            }
            eat(RIGHT_PAREN)
            result = FCallExpr(result, operands)
        }
        result.line = tokens[current - 1].line
        return result
    }

    private fun statement(): Stmt {
        var result = if (peek(LET)) {
            lets()
        } else if (peek(IF)) {
            ifs()
        } else if (peek(WHILE)) {
            whiles()
        } else if (peek(RETURN)) {
            eat(RETURN)
            RetStmt(expression())
        } else {
            ExprStmt(expression())
        }
        result.line = tokens[current - 1].line
        return result
    }

    private fun ifs(): IfStmt {
        eat(IF)
        var cond = expression()
        var body: ArrayList<Stmt> = ArrayList()
        var elseBody: ArrayList<Stmt> = ArrayList()
        while (!peek(END) && !peek(ELSE)) {
            body.add(statement())
        }
        return if (peek(END)) {
            eat(END)
            IfStmt(cond, StmtList(body), null)
        } else {
            eat(ELSE)
            while (!peek(END)) {
                elseBody.add(statement())
            }
            eat(END)
            IfStmt(cond, StmtList(body), StmtList(elseBody))
        }
    }

    private fun whiles(): WhileStmt {
        eat(WHILE)
        var cond = expression()
        var body: ArrayList<Stmt> = ArrayList()
        while (!peek(END)) {
            body.add(statement())
        }
        eat(END)
        return WhileStmt(cond, StmtList(body))
    }

    private fun lets(): Let {
        eat(LET)
        var name: Token = eat(IDENTIFIER)
        eat(EQUAL)
        var value: Expr = expression()
        return Let(name, value)
    }

    fun parse(): Stmt {
        var stmtList: ArrayList<Stmt> = ArrayList()
        while (!peek(EOF)) {
            stmtList.add(statement())
        }
        return StmtList(stmtList)
    }
}