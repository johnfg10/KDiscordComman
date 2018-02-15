package io.github.johnfg10.permission

import io.github.johnfg10.user.User
import lombok.Data
import java.io.Serializable

@Data
class AUserPermission(var guildId: Long, public var userId: Long, public var permissions: MutableList<String>) : Serializable {
    constructor() : this(0,0, mutableListOf())
}
