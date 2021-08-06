sealed interface FunctionBody

class LoopId
class SlotId

sealed interface Expression<out T>: FunctionBody
sealed interface Variable: Expression<Nothing>
data class NamedVariable(val name: String): Variable
data class LoopBoundVariable(val loopId: LoopId): Variable
data class Slot(val id: SlotId = SlotId()): Variable
data class IntegerConstant(val value: Long): Expression<Long>
data class StringConstant(val value: String): Expression<String>
data class FloatingContant(val value: Double): Expression<Double>
data class FunctionCall<R>(val function: String, val arguments: List<Expression<Any?>>): Expression<R> {
    constructor(function: String, vararg arguments: Expression<Any?>): this(function, arguments.asList())
}

data class Block(val statements: List<Statement>): FunctionBody

sealed interface Statement
data class Assignment<T>(val to: Expression<T>, val from: Expression<T>): Statement
data class ExpressionStatement(val body: Expression<Any?>): Statement
sealed interface LoopStatement: Statement {
    val loopId: LoopId
}
data class DoWhileLoop(val body: Block, val condition: Expression<Boolean>, override val loopId: LoopId = LoopId()): LoopStatement
data class WhileLoop(val condition: Expression<Boolean>, val body: Block, override val loopId: LoopId = LoopId()): LoopStatement
data class ForLoop(val container: Expression<Any?>, val body: Block, override val loopId: LoopId = LoopId()): LoopStatement
