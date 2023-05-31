package org.vniizt.tabling.dao;

import org.vniizt.tabling.entity.TableStructure;

import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * @author Alexander Ilyin
 *
 * Здесь описаны основные методы при работе с данными из бд
 */
public interface Tabling {
    LinkedHashSet<String> getSchemasSet();
    LinkedHashSet<String> getTablesBySchemaSet(String schemaName);
    HashSet<String> getForeignTables(String schemaName, String tableName);
    HashSet<String> getProcedureNames(String schemaName, String tableName);
    TableStructure getTableStructure(String schemaName, String tableName);
}