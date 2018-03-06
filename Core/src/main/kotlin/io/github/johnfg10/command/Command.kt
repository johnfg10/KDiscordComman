package io.github.johnfg10.command

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Command(val id: String, val aliases: Array<out String>, val description: String = "")