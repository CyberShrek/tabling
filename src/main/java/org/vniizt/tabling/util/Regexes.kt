package org.vniizt.tabling.util

import org.vniizt.tabling.entity.RelatedTables



object Regexes {
    val whitespace = Regex("\\s+")
    val semicolon = Regex("\\s*;\\s*")
    object Sql {
        val simpleComment = Regex("--.*")
        val bracketedComment = Regex("/\\*.*\\*/")
        val tableName = Regex("[a-z_][a-z0-9_]*?\\.[a-z_][a-z0-9_]*?")
        object Procedure{
            val DECLAREField = Regex("(?i)\\s*DECLARE.+BEGIN\\s")
            object IFExpression {
                val IF = Regex("(?i)")
                val THEN = Regex("(?i)")
                val ELSEIF = Regex("(?i)")
                val ELSE = Regex("(?i)")
            }
        }
    }
}