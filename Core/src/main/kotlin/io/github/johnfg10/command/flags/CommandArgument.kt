package io.github.johnfg10.command.flags

data class CommandArgument(val flag: CommandFlag, val value: String)

fun List<CommandArgument>.containsFlag(flag: String) : Boolean {
    return this.any { it.flag.flag == flag }
}

fun List<CommandArgument>.getValue(flag: String) : String? {
    return this
            .firstOrNull { it.flag.flag == flag }
            ?.value
}
