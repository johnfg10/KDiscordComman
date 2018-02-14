package io.github.johnfg10.command.flags

data class CommandFlag(val flag: String)

fun List<CommandFlag>.containsFlag(flag: String) : Boolean {
    return this.any { it.flag == flag }
}