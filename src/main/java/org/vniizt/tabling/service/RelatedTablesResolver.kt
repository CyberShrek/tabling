package org.vniizt.tabling.service

import org.springframework.stereotype.Service
import org.vniizt.tabling.entity.RelatedTables

@Service
class RelatedTablesResolver {

    fun findRelatedTables(procedureText: String) = HashSet<RelatedTables>().apply {



        procedureText
            .prepareForAnalysis()
            .split(semicolonSeparatorQuotesIgnored)
            .forEach {
//                this.add(it)
            }
    }

    private fun String.findRelatedTables(): RelatedTables{

    }

    // Removes some unnecessary content, such as comments, space sequences, etc.
    private fun String.prepareForAnalysis() = this
        .erase(simpleComment)
        .erase("\n")
        .erase(bracketedComment)
        .trim()
        .replace(whitespace, " ")
        .find(BEGINBody)?.value ?: ""

    private companion object Regexes {
        val whitespace = Regex("\\s+")
        val simpleComment = Regex("--.*")
        val bracketedComment = Regex("/\\*.*\\*/")
        val semicolonSeparatorQuotesIgnored = Regex("\\s*;\\s*(?=(?:[^']*'[^']*')*[^']*\$)")
        val assignmentOperator = Regex("\\s?(=|:=)\\s?")

        val BEGINBody   = Regex("(?i)(?<=BEGIN\\s)[\\s\\S]*?(?=END(?: [^;]*|);\$)")

        val quotedText = Regex("'(?:''|[^'])*'")

        val name = Regex("\\D\\w*")
        val entityName = Regex("$name\\.$name")

        val targetTable = Regex("(?i)(?<=(?:^|\\W)(?:UPDATE|INSERT INTO) )\\D\\w*\\.\\D\\w*")
    }

    private fun String.erase(value: String) = replace(value, "")
    private fun String.erase(regex: Regex) = replace(regex, "")
    private fun String.find(regex: Regex) = regex.find(this)
    private fun String.findAll(regex: Regex) = regex.findAll(this)

    private inner class
}