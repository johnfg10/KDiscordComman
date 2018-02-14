package io.github.johnfg10.permission

import io.github.johnfg10.user.AUser

data class AUserPermission(override val guildId: Long, override val userId: Long, val permissions: List<String>) : AUser()