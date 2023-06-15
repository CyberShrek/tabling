package org.vniizt.tabling.util


object Regexes {
    val whitespace = Regex("\\s+")
    object Sql {
        val simpleComment = Regex("--.*")
        val bracketedComment = Regex("/\\*.*\\*/")
        val tableName = Regex("[a-z_][a-z0-9_]*?\\.[a-z_][a-z0-9_]*?")
        val semicolonSeparator = Regex("\\s*;\\s*(?=(?:[^']*'[^']*')*[^']*\$)") // Also ignores semicolons inside of apostrophe pairs

        // The number of spaces should be the minimum necessary for the PLSQL syntax, single-spaced, trimmed, without \n or \t
        object Procedure{

            val DECLARE = Regex("(?i)^DECLARE\\s")
            val assignmentOperator = Regex("\\s?(=|:=)\\s?")
            val BEGIN = Regex("(?i)^BEGIN\\s")

            val DECLAREBlock = Block(
                content = Regex("(?<=^DECLARE\\s)[\\s\\S]*?(?=\\sBEGIN)")
            )

            val BEGINBlock = Block(
                content = Regex("(?<=BEGIN\\s)[\\s\\S]*?(?=\\sEND;)")
            )

            object IFBlock {
                val IF = Regex("(?i)")
                val THEN = Regex("(?i)")
                val ELSEIF = Regex("(?i)")
                val ELSE = Regex("(?i)")
            }

            class Block(
                val content: Regex
            )
        }
    }
}