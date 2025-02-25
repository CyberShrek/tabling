package org.vniizt.tabling.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vniizt.tabling.entity.DocumentParams;
import org.vniizt.tabling.service.docx.DocumentCompiler;
import org.vniizt.tabling.service.docx.TablesInfo;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Alexander Ilyin
 */

@Service
public class DocumentService {

    @Autowired
    private DataSource dataSource;

    private TablesInfo tablesInfo;
    private DocumentCompiler documentCompiler;

    private final String templateLocation = "/opt/manual/catalog/template.docx";

    @Transactional
    public XWPFDocument createDocument(DocumentParams params) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            tablesInfo = new TablesInfo(connection);
            documentCompiler = new DocumentCompiler(templateLocation);
            System.out.println("НАЧИНАЮ ЗАПОЛНЯТЬ КАТАЛОГ");
            addTablesCatalog(params.getTables());
            return documentCompiler.completeAndGetDocument();
        }
    }

    private void addTablesCatalog(String[] tables) throws SQLException {
        String schemaName = "", tableName = "";
        int schemaNum = 0, tableNum = 1;
        for (String table : tables) {
            String[] schemaNtable = table.split("\\."); // schema.table -> [schema][table]
            // Построение описания схемы
            if (!schemaNtable[0].equals(schemaName)) {
                schemaNum++;
                schemaName = schemaNtable[0];
                addSchema(schemaNum, schemaName);
                tableNum = 1;
            }
            // Построение описания таблицы
            tableName = schemaNtable[1];
            addTable(schemaNum, tableNum, schemaName, tableName);
            compileSection();
            tableNum++;
        }
    }

    private void addSchema(int schemaNum, String schemaName) throws SQLException {
        String schemaHeading = tablesInfo.getSchemaHeading(schemaNum, schemaName);
        System.out.println("добавляю заголовок\t: " + schemaHeading);
        documentCompiler.setSchemaHeading(schemaHeading);
    }

    private void addTable(int schemaNum, int tableNum, String schemaName, String tableName) throws SQLException {
        String tableHeading = tablesInfo.getTableHeading(schemaNum, tableNum, schemaName, tableName);
        System.out.println("добавляю заголовок\t:\t" + tableHeading);
        documentCompiler.setTableHeading(tableHeading);
        System.out.println("\t\t\tзапись таблицы");
        documentCompiler.setColumnsDescription(tablesInfo.getColumnsDescription(schemaName, tableName));
        System.out.println("\t\t\tзапись ключей");
        documentCompiler.setPrimaryKey(tablesInfo.getPrimaryKey(schemaName, tableName));
        documentCompiler.setForeignKeys(tablesInfo.getForeignKeys(schemaName, tableName));
        documentCompiler.setUniqueKeys(tablesInfo.getUniqueKeys(schemaName, tableName));
    }

    private void compileSection(){
        System.out.println("\t\t\tкомпиляция секции");
        documentCompiler.compileSection();
        System.out.println("\t\t\tSUCCESS");
    }

    // Далее методы работы с шаблоном

    public void setTemplate(XWPFDocument template) throws IOException {
        template.write(new FileOutputStream(templateLocation));
    }
    public XWPFDocument getTemplate() throws IOException {
        return new XWPFDocument(new FileInputStream(templateLocation));
    }

//    @PostConstruct
//    private void actualizeTemplateName() {
//        try{
//            new FileInputStream(templateLocation);
//        }
//        catch (FileNotFoundException exception){
//            templateLocation = "template.docx";
//        }
//    }
}
