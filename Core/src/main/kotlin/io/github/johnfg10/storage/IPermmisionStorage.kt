package io.github.johnfg10.storage

import io.github.johnfg10.permission.AUserPermission
import io.github.johnfg10.user.User

interface IPermmisionStorage {

    fun giveUserPermission(user: User, permission: String)

    fun findUserPermission(user: User) : AUserPermission?

    fun load()

    fun save()

    fun shutdown()
}