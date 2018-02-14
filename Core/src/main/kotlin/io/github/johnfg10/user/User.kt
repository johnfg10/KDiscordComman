package io.github.johnfg10.user

data class User(override val userId: Long, override val guildId: Long) : AUser()