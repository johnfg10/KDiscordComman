package io.github.johnfg10.permission

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class Permission(val permission: String)