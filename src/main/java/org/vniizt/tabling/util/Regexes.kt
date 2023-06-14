package org.vniizt.tabling.util


object Regexes {
    val whitespace = Regex("\\s+")
    object Sql {
        val simpleComment = Regex("--.*")
        val bracketedComment = Regex("/\\*.*\\*/")
        val tableName = Regex("[a-z_][a-z0-9_]*?\\.[a-z_][a-z0-9_]*?")
        val semicolonSeparator = Regex("\\s*;\\s*(?=(?:[^']*'[^']*')*[^']*\$)") // Also ignores semicolons inside of apostrophe pairs

        // Procedure must be semicolon-separated to trimmed expressions. All spaces must be single.
        object Procedure{
            val DECLARE = Regex("(?i)^DECLARE\\s")
            val assignmentOperator = Regex("\\s?(=|:=)\\s?")
            val BEGIN = Regex("(?i)^BEGIN\\s")

            object IFExpression {
                val IF = Regex("(?i)")
                val THEN = Regex("(?i)")
                val ELSEIF = Regex("(?i)")
                val ELSE = Regex("(?i)")
            }
        }
    }
}