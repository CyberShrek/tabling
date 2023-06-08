package org.vniizt.tabling.dao

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.vniizt.tabling.entity.RelatedTables
import org.vniizt.tabling.service.RelatedTablesResolver

@Component
class TablingNew(private var jdbc: JdbcTemplate,
                 private val resolver: RelatedTablesResolver) {

    fun getProcedureRelatedTables() = HashMap<String, Set<RelatedTables>>().apply {
        with(jdbc.queryForRowSet(
            "SELECT DISTINCT * FROM magic.get_procedures()"
        )) {
            while (next())
                put(
                    getString("procedure_name")!!,
                    getString("procedure")!!.let { resolver.findRelatedTables(it) }
                )
        }
    }
}
