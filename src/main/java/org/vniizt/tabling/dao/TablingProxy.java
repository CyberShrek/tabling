package org.vniizt.tabling.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.vniizt.tabling.entity.TableStructure;
import org.vniizt.tabling.entity.TableStructureRow;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;

/**
 * @author Alexander Ilyin
 *
 * Этот прокси понадобился для сокращения времени загрузки полного списка схем и таблиц клиенту
 * путём выполнения лишь одного запроса к БД на все записи.
 * Основная сложность при этом ограничивается работой с SqlRowSet.
 * Как итог — скорость загрузки таблиц на веб-страницу выросла примерно в 20 раз.
 */
@Component
public class TablingProxy implements Tabling{

    @Autowired
    private TablingJdbc jdbc;

    // Собственно объект, который хранит один большой запрос-порцию
    private SqlRowSet rowSet;

    public void updateCache() {
        rowSet = jdbc.getFullRowSet();
        rowSet.first();
    }

    @Override
    public LinkedHashSet<String> getSchemasSet() {
        LinkedHashSet <String> schemas = new LinkedHashSet<>();
        rowSet.first();
        do{
            schemas.add(rowSet.getString("table_schema"));
        }
        while (rowSet.next());
        return schemas;
    }

    @Override
    public LinkedHashSet<String> getTablesBySchemaSet(String schema) {
        LinkedHashSet<String> tables = new LinkedHashSet<>();
        moveCursorTo(schema);
        do {
            if (!Objects.equals(rowSet.getString("table_schema"), schema))
                break;
            tables.add(rowSet.getString("table_name"));
        }
        while (rowSet.next());
        return tables;
    }

    @Override
    public HashSet<String> getForeignTables(String schemaName, String tableName) {
        HashSet<String> foreignTables = new HashSet<>();
        moveCursorTo(schemaName, tableName);
        do {
            String foreignTable = rowSet.getString("foreign_table");
            if (foreignTable != null && !foreignTable.equals(schemaName + "." +tableName))
                foreignTables.add(foreignTable);
        }
        while (rowSet.next() && Objects.equals(rowSet.getString("table_name"), tableName));
        return foreignTables;
    }

    @Override
    public HashSet<String> getProcedureNames(String schemaName, String tableName) {
        HashSet<String> procedureNames = new HashSet<>();
        moveCursorTo(schemaName, tableName);
        do {
            String procedureName = rowSet.getString("procedure_name");
            if (procedureName != null) {
                procedureNames.add(procedureName);
            }
        }
        while (rowSet.next() && Objects.equals(rowSet.getString("table_name"), tableName));
        return procedureNames;
    }

    @Override
    public TableStructure getTableStructure(String schemaName, String tableName) {
        return jdbc.getTableStructure(schemaName, tableName);
    }

    private void moveCursorTo(String schema, String table){
        do {
            moveCursorTo(schema);
            if (Objects.equals(rowSet.getString("table_name"), table))
                break;
        }
        while (rowSet.next());
    }

    private void moveCursorTo(String schema){
        if (rowSet.getRow() == 0)
            rowSet.first();
        while (!Objects.equals(rowSet.getString("table_schema"), schema) && rowSet.next())
            if (rowSet.isLast()) // кольцо
                rowSet.first();
    }
}
