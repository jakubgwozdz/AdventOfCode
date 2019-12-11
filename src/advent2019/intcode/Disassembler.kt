package advent2019.intcode

import java.math.BigInteger
import java.math.BigInteger.ONE

sealed class Param {
    abstract val value: BigInteger
}

data class PositionParam(override val value: BigInteger) : Param() {
    override fun toString(): String {
        return "[$value]"
    }
}

data class ImmediateParam(override val value: BigInteger) : Param() {
    override fun toString(): String {
        return "$value"
    }
}

data class RelativeParam(override val value: BigInteger) : Param() {
    override fun toString(): String {
        return "[rb+$value]"
    }
}

sealed class Disassembly {
    abstract val size: BigInteger
}

data class Op(val name: String, val params: List<Param>) : Disassembly() {
    override fun toString(): String {
        return "$name ${params.joinToString(", ")}"
    }
    override val size: BigInteger
        get() = ONE + params.size
}

data class Data(val value: BigInteger) : Disassembly() {
    override fun toString(): String {
        return "DATA  $value"
    }
    override val size: BigInteger
        get() = ONE
}

fun dissassembly(memory: Memory, addr: BigInteger): Disassembly {

    fun params(count: Int): List<Param> {
        return (1..count).map {
            when (nthParamMode(it, memory[addr])) {
                ParamMode.Position -> PositionParam(
                    memory[addr + it]
                )
                ParamMode.Immediate -> ImmediateParam(
                    memory[addr + it]
                )
                ParamMode.Relative -> RelativeParam(
                    memory[addr + it]
                )
            }
        }
    }

    return try {
        val opcode = opcode(memory[addr])
        when (opcode) {
            1 -> Op("ADD  ", params(3))
            2 -> Op("MUL  ", params(3))
            3 -> Op("IN   ", params(1))
            4 -> Op("OUT  ", params(1))
            5 -> Op("JNZ  ", params(2))
            6 -> Op("JZ   ", params(2))
            7 -> Op("SETL ", params(3))
            8 -> Op("SETE ", params(3))
            9 -> Op("MOV   rb,", params(1))
            99 -> Op("HALT ", emptyList())
            else -> error("unknown opcode ${memory[addr]} at addr $addr")
        }
    } catch (e: Exception) {
        Data(memory[addr])
    }
}