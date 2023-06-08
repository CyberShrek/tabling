package org.vniizt.tabling.service

import org.springframework.stereotype.Service
import org.vniizt.tabling.entity.RelatedTables

@Service
class RelatedTablesResolver {


    fun findRelatedTables(procedureText: String) = HashSet<RelatedTables>().apply {
        with(prepareForAnalysis(procedureText)){

        }
    }

    fun prepareForAnalysis(procedureText: String) = procedureText
        .erase(regexes.sql.simpleComment)
        .replace(regexes.whitespace, " ")
        .erase(regexes.sql.bracketedComment)
        .trim()
        .replace(regexes.semicolon, "\n")

    private val regexes = object {
        val sql = object {
            val simpleComment = Regex("--.*")
            val bracketedComment = Regex("/\\*.*\\*/")
            val DECLAREField = Regex("(?i)\\s*DECLARE.+BEGIN\\s")
            val tableName = Regex("[a-z_][a-z0-9_]*?\\.[a-z_][a-z0-9_]*?")
        }
        val whitespace = Regex("\\s+")
        val semicolon = Regex("\\s*;\\s*")
    }

    private fun String.findUPDATEExpression() {

    }

    private fun String.erase(regex: Regex) = replace(regex, "")

}