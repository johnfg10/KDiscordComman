package io.github.johnfg10.storage

import io.github.johnfg10.user.AUser
import io.github.johnfg10.permission.AUserPermission

interface IPermmisionStorage {

    fun addUserPermission(userPermission: AUserPermission)

    fun removeUserPermission(userPermission: AUserPermission)

    fun updateUserPermission(userPermission: AUserPermission)

    fun findUserPermission(AUser: AUser) : AUserPermission?

    fun save()

    fun shutdown()
}