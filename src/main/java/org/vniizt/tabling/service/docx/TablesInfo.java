package org.vniizt.tabling.service.docx;

import org.vniizt.tabling.entity.TableStructureRow;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * @author Alexander Ilyin
 *
 *
 * Формирует медаданные таблиц в читабельный вид для последующей вставки в документ
 */
public final class TablesInfo {

    private final Connection connection;
    private final DatabaseMetaData metaData;

    /**
     * @param connection Соединение с БД
     */
    public TablesInfo(Connection connection) throws SQLException {
        this.connection = connection;
        metaData = connection.getMetaData();
    }

    /**
     * Получение заголовка схемы вместе с комментарием
     *
     * @param schemaNum порядковый номер схемы
     * @param schemaName имя схемы
     * @return строка в формате "[schemaNum]. Схема «" + schemaRemark + "» (" + schemaName + ")"
     */
    public String getSchemaHeading(int schemaNum, String schemaName) throws SQLException {
        String schemaRemark = ""; //Имя схемы из комментария
        try (ResultSet resultSet = connection.createStatement().executeQuery(
                "SELECT obj_description('" + schemaName + "'::regnamespace, 'pg_namespace')")){
            if (resultSet.next()) {
                String remark = resultSet.getString("obj_description");
                schemaRemark = (remark == null) ? schemaRemark : remark;
            }
        }
        return schemaNum + ". Схема «" + schemaRemark + "» (" + schemaName + ")";
    }

    /**
     * Получение заголовка таблицы вместе с комментарием
     *
     * @param schemaNum порядковый номер схемы
     * @param tableNum порядковый номер таблицы
     * @param schemaName имя схемы
     * @param tableName имя таблицы
     * @return строка в формате "[schemaNum].[tableNum]. Таблица «" + tableRemark + "» (" + tableName + ")"
     */
    public String getTableHeading(int schemaNum, int tableNum, String schemaName, String tableName) throws SQLException {
        return schemaNum + "." + tableNum + ". " + getTableHeading(schemaName, tableName);
    }
    /**
     * Получение заголовка таблицы вместе с комментарием
     *
     * @param schemaName имя схемы
     * @param tableName имя таблицы
     * @return строка в формате "[schemaNum].[tableNum]. Таблица «" + tableRemark + "» (" + tableName + ")"
     */
    public String getTableHeading(String schemaName, String tableName) throws SQLException {
        String tableRemark = ""; //Имя таблицы из комментария
        try (ResultSet resultSet = metaData.getTables(null, schemaName, tableName, null)){
            if (resultSet.next()){
                String remark = resultSet.getString("REMARKS");
                tableRemark = (remark == null) ? tableRemark : remark;
            }
        }
        return "Таблица «" + tableRemark.trim() + "» (" + tableName + ")";
    }

    /**
     * Получение HashSet с массивами описаний колонок таблицы
     * 0 элемент массива - комментарий
     * 1 элемент массива - имя колонки
     * 2 элемент массива - тип данных
     */
    public LinkedHashSet<TableStructureRow> getColumnsDescription(String tableName, String schemaName) throws SQLException {
        LinkedHashSet<TableStructureRow> tableStructure = new LinkedHashSet<>();
        try (ResultSet rs = metaData.getColumns(null, tableName, schemaName, "%")){
            while (rs.next()) {
                String  columnName = rs.getString("COLUMN_NAME"),
                        comment    = rs.getString("REMARKS"),
                        typeName   = rs.getString("TYPE_NAME");
                int     dataType   = rs.getInt("DATA_TYPE"),
                        colSize    = rs.getInt("COLUMN_SIZE"),
                        decDigits  = rs.getInt("DECIMAL_DIGITS");

                if (comment==null) comment="";
                switch (dataType) {
                    case Types.NUMERIC: typeName = "dec("+colSize+","+decDigits+")"; break;
                    case Types.CHAR: typeName = "char("+colSize+")"; break;
                    case Types.VARCHAR: if (!typeName.equals("text"))  typeName = typeName+"("+colSize+")"; break;
                }
                tableStructure.add(new TableStructureRow(comment, columnName, modifyTypeName(typeName)));
            }
        }
        return tableStructure;
    }
    //изменение имени типа в таблице
    private String modifyTypeName(String typeName) {
        switch (typeName) {
            case "int2": typeName = "smallint";
                break;
            case "int4": typeName = "int";
                break;
            case "int8": typeName = "bigint";
                break;
            case "float8": typeName = "float";
                break;
        }
        if (typeName.indexOf("_") == 0) {//если это массив
            typeName = typeName.replaceFirst("_", "");
            typeName += "[]";
        }
        return typeName.toUpperCase();
    }

    ///////////////       ПОЛУЧЕНИЕ КЛЮЧЕЙ       ///////////////
    /**
     * Получение описания первичного ключа таблицы
     *
     * @return массив строк с описанием ключа в формате "Первичный ключ:
     * [столбцы (индекс)]"
     */
    public String[] getPrimaryKey(String schemaName, String tableName) throws SQLException {
        ArrayList<String> primaryKey = new ArrayList<>();
        try (ResultSet resultSet = metaData.getPrimaryKeys(null, schemaName, tableName)){
            if (resultSet.next())
                primaryKey.add(compileKey(resultSet, "PK_NAME", "COLUMN_NAME"));

        }
        return primaryKey.toArray(new String[0]);
    }

    /**
     * Получение описания уникальных ключей таблицы без учёта первичных ключей
     *
     * @return массив строк с описанием ключей в формате "Уникальные ключи:
     * [столбцы (индекс)]"
     */
    public String[] getUniqueKeys(String schemaName, String tableName) throws SQLException {
        ArrayList<String> uniqueKeys = new ArrayList<>();
        try (ResultSet resultSet = metaData.getIndexInfo(null, schemaName, tableName, false, false);
             ResultSet pkeyRset = metaData.getPrimaryKeys(null, schemaName, tableName)){
            String primaryKeyIndex = null;
            String uniqueKeyIndex;
            if (pkeyRset.next())
                primaryKeyIndex = pkeyRset.getString("PK_NAME");
            while (resultSet.next()) {
                uniqueKeyIndex = resultSet.getString("INDEX_NAME");
                //Если индекс не является индексом первичного ключа
                if (!uniqueKeyIndex.equals(primaryKeyIndex))
                    uniqueKeys.add(compileKey(resultSet, "INDEX_NAME", "COLUMN_NAME"));
            }
        }
        return uniqueKeys.toArray(new String[0]);
    }

    /**
     * Получение описания внешних ключей таблицы
     *
     * @return массив строк с описанием ключей в формате "Внешние ключи:
     * [столбцы (индекс)]"
     */
    public String[] getForeignKeys(String schemaName, String tableName) throws SQLException {
        ArrayList<String> foreignKeys = new ArrayList<>();
        try (ResultSet resultSet = metaData.getImportedKeys(null, schemaName, tableName)){
            while (resultSet.next()) {
                int startRow = resultSet.getRow();  // буфер для записи стартовой позиции курсора, с которой будет составляться описание ключа
                foreignKeys.add(compileKey(resultSet, "FK_NAME", "FKCOLUMN_NAME"));
                int currentRow = resultSet.getRow(); //текущая позиция курсора
                //возвращение курсора в изначальную позицию для компиляции ключа, на которые ссылается полученый в пределах текущей итерации ключ
                resultSet.absolute(startRow);
                foreignKeys.add("       связан с таблицей " + resultSet.getString("PKTABLE_NAME"));
                foreignKeys.add("               по ключу " + compileKey(resultSet, "PK_NAME", "PKCOLUMN_NAME"));
                //необходимо вернуть курсор в случае если на одну внешнюю таблицу ссылаются несколько ключей
                if (resultSet.getRow() != currentRow)
                    resultSet.absolute(currentRow);
            }
        }
        return foreignKeys.toArray(new String[0]);
    }


    /**
     * Заполнение описания составного ключа
     *
     * @param resultSet текущий объект ResultSet
     * @param INDEX_NAME аргумент в resultSet.toString(INDEX_NAME), по которому
     * ожидается возвращение индекса-имени ключа
     * @param COLUMN_NAME аргумент в resultSet.toString(COLUMN_NAME), по
     * которому ожидается возвращение названия столбца
     * @return строка с описанием заданного ключа
     */
    private String compileKey(ResultSet resultSet, String INDEX_NAME, String COLUMN_NAME) throws SQLException {
        String origKeyIndex = resultSet.getString(INDEX_NAME);    //индекс ключа, к которому будет составляться описание
        LinkedHashSet<String> columnsSet = new LinkedHashSet<>(); //для хранения имён столбцов необходимо использовать множество, поскольку индексы внешних таблиц в ResultSet могут иметь несколько одинаковых COLUMN_NAME
        columnsSet.add(resultSet.getString(COLUMN_NAME));

        int nextIndexRow = 0;       //номер строки со следующим индексом, к которой вернётся resulSet после итераций
        while (resultSet.next())    //итерирует resultSet до последнего элемента и записывает имена столбцов в columnsLine, чей индекс соответсвует индексу origKeyIndex
            if (resultSet.getString(INDEX_NAME).equals(origKeyIndex))
                columnsSet.add(resultSet.getString(COLUMN_NAME));
            else if (nextIndexRow == 0)
                nextIndexRow = resultSet.getRow() - 1;
        //заполнение строки-описания ключа
        String[] columnsArr = columnsSet.toArray(new String[0]);
        StringBuilder keyLine = new StringBuilder(columnsArr[0]);
        for (int i = 1; i < columnsArr.length; i++)
            keyLine.append(", ").append(columnsArr[i]);
        keyLine.append(" (").append(origKeyIndex).append(") ");

        if (nextIndexRow != 0) //перемещение курсора к строке со следующим индексом (если он есть)
            resultSet.absolute(nextIndexRow);

        return keyLine.toString();
    }
}
