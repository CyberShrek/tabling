package org.vniizt.tabling.util

import org.vniizt.tabling.entity.RelatedTables


object Regexes {
    object Sql {
        val simpleComment = Regex("--.*")
        val bracketedComment = Regex("/\\*.*\\*/")
        val DECLAREField = Regex("(?i)\\s*DECLARE.+BEGIN\\s")
        val tableName = Regex("[a-z_][a-z0-9_]*?\\.[a-z_][a-z0-9_]*?")
    }
    val whitespace = Regex("\\s+")
    val semicolon = Regex("\\s*;\\s*")
}

class RelatedTablesResolver {


    fun findRelatedTables(procedureText: String) = HashSet<RelatedTables>().apply {
//        with(prepareForAnalysis(procedureText)){
//
//        }
    }

    fun prepareForAnalysis(procedureText: String) = procedureText
        .erase(Regexes.Sql.simpleComment)
        .replace(Regexes.whitespace, " ")
        .erase(Regexes.Sql.bracketedComment)
        .trim()
        .replace(Regexes.semicolon, "\n")


    private fun String.findUPDATEExpression() {

    }

    private fun String.erase(regex: Regex) = replace(regex, "")

}