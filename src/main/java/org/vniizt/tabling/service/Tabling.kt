package org.vniizt.tabling.service

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.vniizt.tabling.entity.RelatedTables
import org.vniizt.tabling.entity.TableStructure
import org.vniizt.tabling.service.docx.TablesInfo
import java.util.*

@Service
class Tabling(private var jdbc: JdbcTemplate,
              private val resolver: RelatedTablesResolver) {

    // Key is a schema name, value is a set of its table names
    fun getSchemasTables() = LinkedHashMap<String, MutableSet<String>>().apply {
        with(jdbc.queryForRowSet(
            "SELECT DISTINCT table_schema, table_name " +
                    "FROM information_schema.tables " +
                    "ORDER BY table_schema, table_name")){
            while (next()) {
                val schemaName = getString("table_schema")!!
                computeIfAbsent(schemaName) { LinkedHashSet() }
                get(schemaName)!!.add(getString("table_name")!!)
            }
        }
    }

    // Key is a procedure name, value is a text
    private val proceduresMapBuffer = HashMap<String, String>()
    private val relatedTablesSetBuffer = HashSet<RelatedTables>()
    @Synchronized
    fun getRelatedTables() = relatedTablesSetBuffer.apply {
        with(jdbc.queryForRowSet(
            "SELECT DISTINCT * FROM magic.get_procedures()"
        )) {
            while (next()){
                val procedureName = getString("procedure_name")!!
                val procedureText = getString("procedure")!!
                if(proceduresMapBuffer[procedureName] != procedureText){
                    proceduresMapBuffer[procedureName] = procedureText
                    addAll(resolver.findRelatedTables(procedureText))
                }
            }
        }
    }

    fun getTableStructure(schemaName: String, tableName: String): TableStructure =
        with(TablesInfo(jdbc.dataSource!!.connection)) {
            TableStructure(
                getTableHeading(schemaName, tableName),
                getColumnsDescription(schemaName, tableName)
            )
        }
}
