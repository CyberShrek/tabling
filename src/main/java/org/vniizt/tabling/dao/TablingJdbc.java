package org.vniizt.tabling.dao;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.vniizt.tabling.entity.TableStructure;
import org.vniizt.tabling.entity.TableStructureRow;
import org.vniizt.tabling.service.docx.TablesInfo;

import javax.sql.RowSet;
import java.sql.Connection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;

/**
 * @author Alexander Ilyin
 */

@Repository
public class TablingJdbc implements Tabling {

    @Autowired
    private JdbcTemplate template;

    // Возвращает полный RowSet (схемы,таблицы, внешние таблицы, процедуры, etc) для прокси
    public SqlRowSet getFullRowSet() {
        return template.queryForRowSet(
                "WITH bindings AS (\n" +
                        "    SELECT DISTINCT gbt.main_table, gbt.inlet_table,\n" +
                        "                    procedure_name, section\n" +
                        "        FROM magic.get_procedures() prc\n" +
                        "        JOIN magic.get_binding_tables(prc.procedure) gbt USING (procedure)\n" +

                        // !!!!!!!!!!!!!!!!!!!!!!! КОСТЫЛИ !!!!!!!!!!!!!!!!!!!!
                        "    WHERE procedure_name != 'svod.prig_agreg_1'      " +
                        "      AND procedure_name != 'svod.prig_obog_4'" +
                        "      AND procedure_name != 'poo.intruders_transfer_to_ng'" +
                        "      AND procedure_name != 'ng.rmest_dayli_dtpzd_insert' " +   // На этих процедуре алгорим виснет
                        "    UNION ALL SELECT 'car.expind', 'car.expindplan', 'car.set_expind2', '' \n" +
                        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

                        "),\n" +
                        "foreign_tables AS (\n" +
                        "    SELECT inlet_table AS main_table, main_table AS foreign_table\n" +
                        "    FROM bindings\n" +
                        "),\n" +
                        "filling_procedures AS (\n" +
                        "    SELECT main_table, procedure_name, section FROM bindings\n" +
                        ")\n" +
                        "SELECT DISTINCT t.table_schema, t.table_name, f_t.foreign_table, p.procedure_name, p.section\n" +
                        "    FROM information_schema.tables t\n" +
                        "    LEFT JOIN foreign_tables f_t ON (f_t.main_table = t.table_schema || '.' || t.table_name)\n" +
                        "    LEFT JOIN filling_procedures p ON (p.main_table = t.table_schema || '.' || t.table_name)\n" +
                        "\n" +
                        "ORDER BY table_schema, table_name");
    }


    public SqlRowSet getProcedures(){
        return template.queryForRowSet("SELECT DISTINCT * FROM magic.get_procedures()");
    }

    // Остальные методы-запросы подразумевается использовать исключительно для тестирования корректности работы прокси

    @Override
    public LinkedHashSet<String> getSchemasSet() {
        SqlRowSet rowSet = template.queryForRowSet("SELECT DISTINCT table_schema " +
                "FROM information_schema.tables " +
                "ORDER BY table_schema");
        LinkedHashSet<String> schemas = new LinkedHashSet<>();
        while (rowSet.next())
            schemas.add(rowSet.getString(1));
        return schemas;
    }

    @Override
    public LinkedHashSet<String> getTablesBySchemaSet(String schema) {
        SqlRowSet rowSet = template.queryForRowSet("SELECT table_name " +
                "FROM information_schema.tables " +
                "WHERE table_schema = '" + schema + "' " +
                "ORDER BY table_name");
        LinkedHashSet<String> tables = new LinkedHashSet<>();
        while (rowSet.next())
            tables.add(rowSet.getString(1));
        return tables;
    }
    @Override
    public HashSet<String> getForeignTables(String schemaName, String tableName) {
        SqlRowSet rowSet = template.queryForRowSet(
                "WITH procedures AS (" +
                "    SELECT pg_proc.oid AS procedure_id, prosrc AS procedure" +
                "    FROM pg_catalog.pg_proc" +
                "             LEFT JOIN pg_catalog.pg_namespace n ON n.oid = pronamespace" +
                "    WHERE n.nspname <> 'pg_catalog'" +
                "      AND n.nspname <> 'information_schema'" +
                "      AND n.nspname <> 'public'" +
                ")" +
                "SELECT DISTINCT main_table AS foreign_table FROM procedures prc " +
                "JOIN magic.get_binding_tables(prc.procedure) gbt USING (procedure) " +
                "WHERE inlet_table = '"+ schemaName + "." + tableName +"'");
        HashSet<String> foreignTables = new HashSet<>();
        while (rowSet.next()) {
            String foreignTable = rowSet.getString("foreign_table");
            if (foreignTable != null && !foreignTable.equals(schemaName + "." + tableName))
                foreignTables.add(foreignTable);
        }
        return foreignTables;
    }

    @Override
    public HashSet<String> getProcedureNames(String schemaName, String tableName) {
        SqlRowSet rowSet = template.queryForRowSet("SELECT procedure_name " +
                "FROM magic.get_procedures() prc " +
                "JOIN magic.get_binding_tables(prc.procedure) gbt USING (procedure) " +
                "WHERE main_table = '"+ schemaName +'.'+ tableName +'\'');
        HashSet<String> procedureNames = new HashSet<>();
        while (rowSet.next()){
            procedureNames.add(rowSet.getString("procedure_name"));
        }
        return procedureNames;
    }

    @Override
    @SneakyThrows
    public TableStructure getTableStructure(String schemaName, String tableName) {
        try (Connection connection = Objects.requireNonNull(template.getDataSource()).getConnection()){
            TablesInfo tablesInfo = new TablesInfo(connection);

            return new TableStructure(
                    tablesInfo.getTableHeading(schemaName, tableName),
                    tablesInfo.getColumnsDescription(schemaName, tableName));
        }
    }
}
