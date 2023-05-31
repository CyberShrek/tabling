package org.vniizt.tabling.service.docx;

import lombok.Setter;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.vniizt.tabling.entity.TableStructureRow;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;

/**
 * @author Alexander Ilyin
 *
 * Объекты этого класса отвечают за создание элементов документа.
 * Фактически это слегка доработанное легаси взятое с первой версии проекта.
 */
public class DocumentCompiler {

    private final String contentTag = "<content will be created here>";
    private XWPFParagraph contentParagraph;

    @Setter
    private String schemaHeading;
    @Setter
    private String tableHeading;
    @Setter
    private HashSet<TableStructureRow> columnsDescription;
    @Setter
    private String[] primaryKey;
    @Setter
    private String[] uniqueKeys;
    @Setter
    private String[] foreignKeys;

    private final XWPFDocument document;

    public DocumentCompiler(String templateLocation) throws IOException {
        document = new XWPFDocument(new FileInputStream(templateLocation));

        document.getParagraphs().forEach(
                paragraph -> {
                    if (paragraph.getText().equals(contentTag))
                        contentParagraph = paragraph;
                });

//        System.out.println(documen);
    }

    public XWPFDocument completeAndGetDocument() {
        document.removeBodyElement(document.getPosOfParagraph(contentParagraph));
        return document;
    }

    // Собирает раздел-описание
    public void compileSection() {
        new WordSection();
    }

    //Класс предназначен для формирования разделов-описаний таблиц
    private final class WordSection {

        XWPFParagraph headSchParagraph;   //заголовок схемы
        XWPFParagraph headTabParagraph;   //заголовок описания таблицы
        XWPFParagraph labelParagraph;     //ярлык
        XWPFParagraph mainParagraph;      //главный параграф, состоящий из таблицы и описаний ключей

        WordSection() {
            if (schemaHeading != null) {
                headSchParagraph = insertNewParagraph();
                headSchParagraph.setStyle("1"); // Шаблонный стиль "Заголовок 1"
                XWPFRun headSchRun = headSchParagraph.createRun();
                headSchRun.addBreak(BreakType.PAGE);
                createLine(headSchRun, schemaHeading);
                schemaHeading = null;
            }
            //заголовок описания таблицы
            headTabParagraph = insertNewParagraph();
            headTabParagraph.setStyle("2");     // Шаблонный стиль "Заголовок 2"
            XWPFRun headTabRun = headTabParagraph.createRun();
            headTabRun.addBreak();
            createLine(headTabRun, tableHeading);

            //ярлык
//            createLabel();
            //таблица-описание колонок
            createTable();
            //описание ключей и их индексов

            if (primaryKey.length > 0)
                descriptKey("Первичный ключ:", primaryKey);
            if (uniqueKeys.length > 0)
                if (uniqueKeys.length > 1) //если ключей более одного
                    descriptKey("Уникальные ключи:", uniqueKeys);
                else
                    descriptKey("Уникальный ключ:", uniqueKeys);
            if (foreignKeys.length > 0)
                if (foreignKeys.length > 3) //здесь нужно 3, поскольку в foreignKeys 1 ключ == 3 элемента описания
                    descriptKey("Внешние ключи:", foreignKeys);
                else
                    descriptKey("Внешний ключ:", foreignKeys);
        }

        /**
         * Создание ярлыка
         */
        private void createLabel() {
            //создание таблицы размером в  1 ячейку
            XWPFTable table = insertNewTable(1);
            labelParagraph = insertNewParagraph();
            makePrettyTable(table);

            labelParagraph.setSpacingAfter(-1); //Устранение отступов между строками
            XWPFRun labelRun = labelParagraph.createRun();

            String name = tableHeading.substring(tableHeading.indexOf('«') + 1, tableHeading.indexOf('»'));
            String code = tableHeading.substring(tableHeading.indexOf('(') + 1, tableHeading.indexOf(')'));
            String label = name; //???

            labelRun.setText("Name    ");
            labelRun.addTab();
            labelRun.setText(":  " + name);
            labelRun.addBreak();
            labelRun.setText("Code    ");
            labelRun.addTab();
            labelRun.setText(":  " + code);
            labelRun.addBreak();
            labelRun.setText("Label   ");
            labelRun.addTab();
            labelRun.setText(":  " + label);

            XWPFTableRow tableRow = table.getRow(0);
            tableRow.getCell(0).setParagraph(labelParagraph);
            labelParagraph.removeRun(0);
        }

        /**
         * Метод создаёт таблицу с описанием колонок таблицы из бд
         */
        private void createTable() {
            //создание таблицы размером в 3 колонки
            XWPFTable table = insertNewTable(3);
            makePrettyTable(table);
            //инициализация главного параграфа
            mainParagraph = insertNewParagraph();
            mainParagraph.setSpacingAfter(-1); //Устранение отступов между строками для обеспечения компактности таблицы
            //заполнение таблицы данными
            fillRow(table.getRow(0), new TableStructureRow("COLUMN NAME", "CODE", "TYPE"));
            columnsDescription.forEach(row -> {
                fillRow(table.createRow(), row);
            });
        }
        private void makePrettyTable(XWPFTable table) {
            CTTblPr tblPr = table.getCTTbl().getTblPr();
            CTJc jc = (tblPr.isSetJc() ? tblPr.getJc() : tblPr.addNewJc());
            jc.setVal(STJc.CENTER);
            table.getCTTbl().addNewTblPr().addNewTblW().setW(BigInteger.valueOf(10500));
        }

        /**
         * Заполнение строки в таблице
         *
         * @param row целевая строка таблицы
         * @param tableStructureRow TableStructureRow
         * последовательно в строку таблицы
         */
        private void fillRow(XWPFTableRow row, TableStructureRow tableStructureRow) {

            String columnName = tableStructureRow.getComment(),
                         code = tableStructureRow.getName(),
                         type = tableStructureRow.getDataType();

            boolean isKey = (isPrimaryKey(code) || isUniqueKey(code) || isForeignKey(code));

            // "COLUMN NAME"
            createLine(mainParagraph.createRun(), columnName, isKey);
            row.getCell(0).setParagraph(mainParagraph);
            mainParagraph.removeRun(0);

            // "CODE"
            createLine(mainParagraph.createRun(), code, isKey);
            row.getCell(1).setParagraph(mainParagraph);
            mainParagraph.removeRun(0);

            // "TYPE"
            createLine(mainParagraph.createRun(), type, isKey);
            row.getCell(2).setParagraph(mainParagraph);
            mainParagraph.removeRun(0);
        }

        /**
         * Метод формирует описание ключей и их индексов
         *
         * @param keysHead заголовок описания
         * @param keysArray массив строк описаний ключа
         */
        private void descriptKey(String keysHead, String[] keysArray) {
            // Создание заголовка
            createLine(mainParagraph.createRun(), keysHead);
            // Заполнение ключей
            for (String key : keysArray) {
                XWPFRun keyRun = mainParagraph.createRun();
                keyRun.addBreak();
                createLine(keyRun, key);
                keyRun.addBreak();
            }
        }

        private XWPFParagraph insertNewParagraph() {
            if (contentParagraph == null)
                return document.createParagraph();

            return document.insertNewParagraph(contentParagraph.getCTP().newCursor());
        }

        private XWPFTable insertNewTable(int size) {
            XWPFTable table;
            if (contentParagraph == null) {
                table = document.createTable(1, size);
            } else {
                table = document.insertNewTbl(contentParagraph.getCTP().newCursor());
                for (int i = 1; i < size; i++) {
                    table.getRow(0).createCell();
                }
            }
            return table;
        }

        // Создание новой строки в объекте Run
        private void createLine(XWPFRun run, String line, boolean isBold) {
            run.setText(line);
            run.setBold(isBold);
        }
        private void createLine(XWPFRun run, String line) {
            run.setText(line);
        }

        //Методы-проверки
        private boolean isPrimaryKey(String code) {
            boolean value = false;
            if (primaryKey.length > 0) {
                String pkey = " " + primaryKey[0].substring(0, primaryKey[0].indexOf("("));
                if (pkey.contains(" " + code + " ") || pkey.contains(" " + code + ", "))
                    value = true;
            }
            return value;
        }

        private boolean isForeignKey(String code) {
            boolean value = false;
            for (int i = 0; i < foreignKeys.length; i += 3) {   //здесь нужно проверять каждый третий элемент
                String fkey = " " + foreignKeys[i].substring(0, foreignKeys[i].indexOf("("));
                if (fkey.contains(" " + code + " ") || fkey.contains(" " + code + ", ")) {
                    value = true;
                    break;
                }
            }
            return value;
        }

        private boolean isUniqueKey(String code) {
            boolean value = false;
            for (String uniqueKey : uniqueKeys) {
                String ukey = " " + uniqueKey.substring(0, uniqueKey.indexOf("("));
                if (ukey.contains(" " + code + " ") || ukey.contains(" " + code + ", ")) {
                    value = true;
                    break;
                }
            }
            return value;
        }
    }
}
