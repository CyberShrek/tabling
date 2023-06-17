package org.vniizt.tabling.service

import org.springframework.stereotype.Service
import org.vniizt.tabling.entity.RelatedTables
import org.vniizt.tabling.entity.SqlProcedure
import org.vniizt.tabling.util.Regexes

@Service
class RelatedTablesResolver {

    fun findRelatedTables(procedureText: String) = HashSet<RelatedTables>().apply {
        BEGINBody.find(
            // Preparing for analysis
            procedureText
                .erase(simpleComment)
                .erase("\n")
                .erase(bracketedComment)
                .replace(whitespace, " ")
                .replace(semicolonSeparator, ";")
                .trim()
        )
    }

    private fun String.findUPDATEExpression() {

    }

    private fun String.erase(value: String) = replace(value, "")
    private fun String.erase(regex: Regex) = replace(regex, "")

    private companion object Regexes {

        val whitespace = Regex("\\s+")
        val simpleComment = Regex("--.*")
        val bracketedComment = Regex("/\\*.*\\*/")
        val semicolonSeparator = Regex("\\s*;\\s*$ignoreQuotesPattern")
        val assignmentOperator = Regex("\\s?(=|:=)\\s?")

        val BEGINBody   = Regex("(?<=BEGIN\\s)[\\s\\S]*(?=\\sEND;$)")

        val blockStartOperators = Regex("(?i)")
        val blockEndOperators = Regex("(?i)")

        val tableName = Regex("[a-z_][a-z0-9_]*?\\.[a-z_][a-z0-9_]*?")

        private val ignoreQuotesPattern
            get() = "(?=(?:[^']*'[^']*')*[^']*\$)"
    }
}