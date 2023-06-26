package org.vniizt.tabling.service

import org.springframework.stereotype.Service
import org.vniizt.tabling.entity.RelatedTables

@Service
class RelatedTablesResolver {

    fun findRelatedTables(procedureText: String) = HashSet<RelatedTables>().apply {
        // Key is variable name, values are sources
        val variablesSources = mutableMapOf<String, List<String>>()

        procedureText
            .prepareForAnalysis()
            .split(semicolon)
            .forEach {
                // Looking for record variables
                it.findAll(recordName).forEach {recordNameResult ->
                    val recordName = recordNameResult.value
                    variablesSources[recordName] = it.find(getRecordQueryRegex(recordName))?.value
                        ?.findAll(sourceName)?.map { sourceNameResult -> sourceNameResult.value }
                        ?.toList() ?: emptyList()
                }

                // Looking for select-into variables
                val sourceNames = it.findAll(sourceName).map { sourceNameResult -> sourceNameResult.value }.toList()
                it.find(intoBody)?.value?.split(comma)?.forEach { variableName ->
                    variablesSources[variableName] = sourceNames
                }

                val targetName = it.find(targetName)?.value

                // Resulting
                if(targetName != null) {
                    fun addSourceName(name: String) {
                        if (name != targetName)

                            this.add(RelatedTables(startTable = name, endTable = targetName))
                    }

                    it.findAll(sourceName).forEach { sourceNameResult ->
                        val sourceName = sourceNameResult.value
                        if(sourceName.contains('.'))
                            // Checking if the source is a record
                            variablesSources[sourceName.substringBefore(".")]
                                ?.forEach { recordSourceName -> addSourceName(recordSourceName) }
                                ?: addSourceName(sourceName)
                    }

                    it.find(valuesBody)?.value?.split(comma)?.forEach {value ->
                        if (!value.equals(quotedText) && value.contains('.'))
                            variablesSources[value.substringBefore(".")]
                                ?.forEach { recordSourceName -> addSourceName(recordSourceName) }
                    }

                    it.find(setBody)?.value?.findAll(entityName)?.forEach {entityName ->
                            variablesSources[entityName.value.substringBefore(".")]
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

        private const val excludeOperatorNamesPattern =
            "(?!BEGIN|CASE|WHEN|IF|THEN|FROM|IS|DISTINCT|ELSE|WHERE|LOOP|AND|OR|NOT|NULL|ISNULL|NOTNULL|TRUE|FALSE|LIKE|UNKNOWN|BETWEEN|SELECT|END)"
        private const val name = "(?<=\\W|^)[a-z_\$]\\w*(?=\\W|\$)"

        val simpleName = Regex("(?:(?i)$excludeOperatorNamesPattern$name)")
        val entityName = Regex("(?:(?i)$excludeOperatorNamesPattern$name(?:\\.$name|))")
        val targetName = Regex("(?<=(?:^|\\W)(?:(?i)UPDATE|INSERT\\sINTO)\\s)$entityName")
        val sourceName = Regex("(?<=(?:\\W)(?:(?i)FROM|JOIN)\\s)$entityName")
        val recordName = Regex("(?<=(?:^|\\W)(?:(?i)FOR)\\s)$simpleName(?=\\s(?i)IN(?:\\s|\\s?\\())")
        val intoBody   = Regex("(?i)(?<=\\WINTO\\s).*?(?=\\WFROM)")
        val valuesBody = Regex("(?<=(?i)VALUES\\s?\\().*?(?=\\))")
        val setBody    = Regex("(?i)(?<=\\WSET\\s).*\$")

        fun getRecordQueryRegex(recordName: String) = Regex("(?<=\\W$recordName\\s(?i)IN\\W).*?(?=\\W(?i)LOOP\\W)")
    }

    private fun String.erase(value: String) = replace(value, "")
    private fun String.erase(regex: Regex) = replace(regex, "")
    private fun String.find(regex: Regex) = regex.find(this)
    private fun String.findAll(regex: Regex) = regex.findAll(this)
}