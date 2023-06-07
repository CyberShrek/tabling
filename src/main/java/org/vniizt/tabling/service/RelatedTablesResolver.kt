package org.vniizt.tabling.service

import org.springframework.stereotype.Service
import org.vniizt.tabling.entity.RelatedTables

@Service
class RelatedTablesResolver {

    fun findRelatedTables(procedureText: String) = ArrayList<RelatedTables>().also {
//        println(
//            procedureText
//                .eraseComments()
//        )
    }

    fun prepareForAnalysis(text: String) = text
        .erase(regexes.sql.simpleComment)
        .replace(regexes.whitespace, " ")
        .erase(regexes.sql.bracketedComment)
        .trim()
        .split(regexes.semicolon)


    private val regexes = object {
        val sql = object {
            val simpleComment = Regex("--.*")
            val bracketedComment = Regex("/\\*.*\\*/")
        }
        val whitespace = Regex("\\s+")
        val semicolon = Regex("\\s*;\\s*")
    }


    private fun String.erase(regex: Regex) = replace(regex, "")

}