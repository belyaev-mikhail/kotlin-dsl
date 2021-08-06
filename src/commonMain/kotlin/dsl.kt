import kotlin.reflect.KProperty

operator fun <T> Expression<T>.plus(rhv: Expression<T>): Expression<T> = FunctionCall("operator plus", this, rhv)
operator fun <T> Expression<T>.minus(rhv: Expression<T>): Expression<T> = FunctionCall("operator minus", this, rhv)
operator fun <T> Expression<T>.times(rhv: Expression<T>): Expression<T> = FunctionCall("operator times", this, rhv)
operator fun <T> Expression<T>.div(rhv: Expression<T>): Expression<T> = FunctionCall("operator div", this, rhv)
operator fun <T> Expression<T>.rem(rhv: Expression<T>): Expression<T> = FunctionCall("operator rem", this, rhv)
operator fun <T> Expression<T>.rangeTo(rhv: Expression<T>): Expression<T> = FunctionCall("operator rangeTo", this, rhv)

interface ExecutionContext {
    operator fun Int.invoke(): Expression<Long> = IntegerConstant(this.toLong())
    operator fun Long.invoke(): Expression<Long> = IntegerConstant(this)

    operator fun <T> Expression<T>.getValue(self: Nothing?, prop: KProperty<*>): Expression<T>
    operator fun <T> Expression<T>.setValue(self: Nothing?, prop: KProperty<*>, rhv: Expression<T>)

    fun call(function: String, vararg arguments: Expression<Any?>): Expression<Any?>

    interface LoopContext: ExecutionContext {
        val loopId: LoopId
    }

    fun loopContext(): LoopContext

    fun Expression<Any?>.forLoop(body: LoopContext.(element: Expression<Nothing>) -> Unit)

    open class BlockAST: ExecutionContext {
        inner class LoopContext: ExecutionContext.LoopContext, BlockAST() {
            override val loopId = LoopId()
        }

        override fun loopContext(): LoopContext = LoopContext()

        val statements: MutableList<Statement> = mutableListOf()
        override fun <T> Expression<T>.getValue(self: Nothing?, prop: KProperty<*>): Expression<T> {
            val res = NamedVariable(prop.name)
            statements += Assignment(NamedVariable(prop.name), this)
            return res
        }
        override fun <T> Expression<T>.setValue(self: Nothing?, prop: KProperty<*>, rhv: Expression<T>) {
            statements += Assignment(NamedVariable(prop.name), rhv)
        }

        override fun Expression<Any?>.forLoop(body: ExecutionContext.LoopContext.(element: Expression<Nothing>) -> Unit) {
            val loop = loopContext()

            val boundVar = LoopBoundVariable(loop.loopId)
            loop.body(boundVar)
            statements += ForLoop(this, Block(loop.statements), loop.loopId)
        }

        override fun call(function: String, vararg arguments: Expression<Any?>): Expression<Any?> {
            val res = Slot()
            statements += Assignment(Slot(), FunctionCall(function, *arguments))
            return res
        }
    }
}

fun main() {
    with(ExecutionContext.BlockAST()) {
        var x by 2()
        var y by x * 2()
        x = y
        y += 4()

        (x..y).forLoop { i ->
            val cc by i - 2()

        }

        this.statements
    }.let { println(it.joinToString("\n")) }
}


