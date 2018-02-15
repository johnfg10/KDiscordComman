package io.github.johnfg10.user

import io.github.johnfg10.Guild
import lombok.Data

@Data
open class User(open val userId: Long, override val guildId: Long) : Guild(guildId)