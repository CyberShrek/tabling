package org.vniizt.tabling.service

import org.springframework.stereotype.Service
import org.vniizt.tabling.entity.RelatedTables
import org.vniizt.tabling.util.Regexes

@Service
class RelatedTablesResolver {


    fun findRelatedTables(procedureText: String) = HashSet<RelatedTables>().apply {
//        with(prepareForAnalysis(procedureText)){
//
//        }
    }

    private fun String.findUPDATEExpression() {

    }

    private fun String.erase(regex: Regex) = replace(regex, "")

}