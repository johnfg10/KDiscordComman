package io.github.johnfg10.roles.storage

import io.github.johnfg10.Guild
import io.github.johnfg10.user.User

interface IRoleStorage {
    fun init()

    fun shutdown()

    fun addRoleToUser(guild: Guild, user: User, role: String)

    fun removeRoleFromUser(guild: Guild, user: User, role: String)

    fun checkRole(guild: Guild, user: User, role: String) : Boolean
}