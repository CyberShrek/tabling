package org.vniizt.tabling.service

import org.springframework.stereotype.Service
import org.vniizt.tabling.entity.RelatedTables

@Service
class RelatedTablesResolver {

    fun findRelatedTables(procedureText: String) = HashSet<RelatedTables>().apply {

        println("--- procedureText --- \n$procedureText")

        // Key is variable name, values are sources
        val variablesSources = mutableMapOf<String, List<String>>()

        procedureText
            .prepareForAnalysis()
            .also {
                println("--- trimmed --- \n$it")
            }
            .split(semicolon)
            .forEach {
                println(it)

                // Looking for record variables
                it.findAll(recordName).forEach {recordNameResult ->
                    val recordName = recordNameResult.value
                    variablesSources[recordName] = it.find(getRecordQueryRegex(recordName))?.value
                        ?.findAll(sourceName)?.map { sourceNameResult -> sourceNameResult.value }
                        ?.toList() ?: emptyList()

                    println("--- sources for record $recordName: ${variablesSources[recordName]}")
                }

                // Looking for select-into variables
                val sourceNames = it.findAll(sourceName).map { sourceNameResult -> sourceNameResult.value }.toList()
                it.find(intoBody)?.value?.split(comma)?.forEach { variableName ->
                    variablesSources[variableName] = sourceNames

                    println("--- sources for variable $variableName: $sourceNames")
                }

                val targetName = it.find(targetName)?.value

                // Resulting
                if(targetName != null) {
                    println("--- targetName $targetName")

                    fun addSourceName(name: String) {
                        if (name != targetName)
                            this.add(RelatedTables(startTable = name, endTable = targetName))
                    }

                    it.findAll(sourceName).forEach { sourceNameResult ->

                        val sourceName = sourceNameResult.value

                        println("--- sourceName $sourceName")

                        if(sourceName.contains('.'))
                            // Checking if the source is a record
                            variablesSources[sourceName.substringBefore(".")]
                                ?.forEach { recordSourceName -> addSourceName(recordSourceName) }
                                ?: addSourceName(sourceName)
                    }

                    it.find(valuesBody)?.value?.split(comma)?.forEach {value ->
                        println("--- value $value")
                        if (value.contains('.'))
                            variablesSources[value.substringBefore(".")]
                                ?.forEach { recordSourceName -> addSourceName(recordSourceName) }
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
        val semicolon = Regex("\\s*;\\s*")
        val comma = Regex("\\s*,\\s*")

        val BEGINBody   = Regex("(?i)(?<=BEGIN\\s)[\\s\\S]*?(?=END(?:\\s[^;]*|);\$)")

        val quotedText = Regex("'(?:''|[^'])*'")

        val simpleName = Regex("\\D\\w*")
        val entityName = Regex("$simpleName(?:\\.(?=\\S)$simpleName|)")
        val targetName = Regex("(?<=(?:^|\\W)(?:(?i)UPDATE|INSERT\\sINTO)\\s)$entityName")
        val sourceName = Regex("(?<=(?:\\W)(?:(?i)FROM|JOIN)\\s)$entityName")
        val recordName = Regex("(?<=(?:^|\\W)(?:(?i)FOR)\\s)$simpleName(?=\\s(?i)IN(?:\\s|\\s?\\())")
        val intoBody   = Regex("(?i)(?<=\\WINTO\\s).*?(?=\\WFROM)")
        val valuesBody = Regex("(?<=(?i)VALUES\\s?\\().*?(?=\\))")

        fun getRecordQueryRegex(recordName: String) = Regex("(?<=\\W$recordName\\s(?i)IN\\W).*?(?=\\W(?i)LOOP\\W)")
    }

    private fun String.erase(value: String) = replace(value, "")
    private fun String.erase(regex: Regex) = replace(regex, "")
    private fun String.find(regex: Regex) = regex.find(this)
    private fun String.findAll(regex: Regex) = regex.findAll(this)
}