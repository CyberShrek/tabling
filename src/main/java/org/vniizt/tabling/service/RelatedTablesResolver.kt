package org.vniizt.tabling.service

import org.springframework.stereotype.Service
import org.vniizt.tabling.entity.RelatedTables

@Service
class RelatedTablesResolver {

    fun findRelatedTables(procedureText: String) = HashSet<RelatedTables>().apply {

        println("--- procedureText --- \n$procedureText")

        // Key is record name, values are sources
        val records = mutableMapOf<String, List<String>>()

        procedureText
            .prepareForAnalysis()
            .also {
                println("--- trimmed --- \n$it")
            }
            .split(semicolonSeparatorQuotesIgnored)
            .forEach {

                println("--- row --- \n$it")

                // Filling in records
                it.findAll(recordName).forEach {recordNameResult ->
                    val recordName = recordNameResult.value
                    records[recordName] = it.find(getRecordQueryRegex(recordName))?.value
                        ?.findAll(sourceName)?.map { sourceNameResult -> sourceNameResult.value }
                        ?.toList() ?: emptyList()

                    println("--- sources for record $recordName: ${records[recordName]}")
                }

                val targetName = it.find(targetName)?.value

                if(targetName != null) {
                    println("--- targetName --- \n$targetName")

                    // Resulting
                    it.findAll(sourceName).forEach { sourceNameResult ->
                        fun addSourceName(name: String) =
                            this.add(RelatedTables(startTable = name, endTable = targetName))

                        val sourceName = sourceNameResult.value

                        println("--- sourceName --- \n$sourceName")

                        if(sourceName.contains('.'))
                            // Checking if the source is a record
                            records[sourceName.substringBefore(".")]
                                ?.forEach { recordSourceName -> addSourceName(recordSourceName) }
                                ?: addSourceName(sourceName)
                    }
                }
            }
    }

    // Removes some unnecessary content, such as comments, space sequences, etc.
    private fun String.prepareForAnalysis() = this
        .erase(simpleComment)
        .replace(whitespace, " ")
        .erase(bracketedComment)
        .trim()
        .find(BEGINBody)?.value ?: ""

    private companion object Regexes {
        val whitespace = Regex("\\s+")
        val simpleComment = Regex("--.*")
        val bracketedComment = Regex("/\\*.*\\*/")
        val semicolonSeparatorQuotesIgnored = Regex("\\s*;\\s*(?=(?:[^']*'[^']*')*[^']*\$)")

        val BEGINBody   = Regex("(?i)(?<=BEGIN\\s)[\\s\\S]*?(?=END(?:\\s[^;]*|);\$)")

        val quotedText = Regex("'(?:''|[^'])*'")

        val simpleName = Regex("\\D\\w*")
        val entityName = Regex("$simpleName(?:\\.(?=\\S)$simpleName|)")
        val targetName = Regex("(?<=(?:^|\\W)(?:(?i)UPDATE|INSERT\\sINTO)\\s)$entityName")
        val sourceName = Regex("(?<=(?:\\W)(?:(?i)FROM|JOIN)\\s)$entityName")
        val recordName = Regex("(?<=(?:^|\\W)(?:(?i)FOR)\\s)$simpleName(?=\\s(?i)IN(?:\\s|\\s?\\())")
        val valuesBody = Regex("(?<=(?i)VALUES\\().*?(?=\\))")

        fun getRecordQueryRegex(recordName: String) = Regex("(?<=\\W$recordName\\s(?i)IN\\W).*?(?=\\W(?i)LOOP\\W)")
    }

    private fun String.erase(value: String) = replace(value, "")
    private fun String.erase(regex: Regex) = replace(regex, "")
    private fun String.find(regex: Regex) = regex.find(this)
    private fun String.findAll(regex: Regex) = regex.findAll(this)
}