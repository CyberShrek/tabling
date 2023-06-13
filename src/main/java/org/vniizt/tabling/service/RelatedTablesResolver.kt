package org.vniizt.tabling.service

import org.springframework.stereotype.Service
import org.vniizt.tabling.entity.RelatedTables
import org.vniizt.tabling.util.regexes

@Service
class RelatedTablesResolver {


    fun findRelatedTables(procedureText: String) = HashSet<RelatedTables>().apply {
//        with(prepareForAnalysis(procedureText)){
//
//        }
    }

    fun prepareForAnalysis(procedureText: String) = procedureText
        .erase(regexes.sql.simpleComment)
        .replace(regexes.whitespace, " ")
        .erase(regexes.sql.bracketedComment)
        .trim()
        .replace(regexes.semicolon, "\n")


    private fun String.findUPDATEExpression() {

    }

    private fun String.erase(regex: Regex) = replace(regex, "")

}