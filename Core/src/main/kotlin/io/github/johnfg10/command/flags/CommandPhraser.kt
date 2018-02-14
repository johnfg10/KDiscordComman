package io.github.johnfg10.command.flags

class CommandPhraser {
    companion object {
        /**
         *
         */
        fun PhraseFlags(arguments: List<String>) : List<CommandFlag> {
            val mutableList = mutableListOf<CommandFlag>()

            arguments.forEach {
                if (it.startsWith('-') && !it.contains(Regex("-{2,}"))){
                    val flagValue = it.replace("-", "")
                    mutableList.add(CommandFlag(flagValue))
                }
            }
            return mutableList
        }

        /**
         *
         */
        fun PhraseArguments(arguments: List<String>) : List<CommandArgument> {
            val mutableList = mutableListOf<CommandArgument>()
            arguments.forEach {
                if (it.startsWith("--") && !it.contains(Regex("-{3,}"))) {
                    val argKey: String
                    val argValue: String = it.replace(Regex("--\\S*="), "")
                    val argKeyResult = Regex("--\\S*=").find(it)
                    argKey = argKeyResult?.value?.replace("-", "")?.replace("=", "") ?: return emptyList()
                    mutableList.add(CommandArgument(CommandFlag(argKey), argValue))
                }
            }
            return mutableList
        }

    }
}