package io.github.johnfg10.command

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandArg(val type: CommandArgumentType)