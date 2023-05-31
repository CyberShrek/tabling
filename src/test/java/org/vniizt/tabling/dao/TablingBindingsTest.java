package org.vniizt.tabling.dao;

import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.fail;

// Тест pgplsql функции поиска связей таблиц
@SpringBootTest
public class TablingBindingsTest {

    @Autowired
    private JdbcTemplate template;

    private final ArrayList<Case> cases = new ArrayList<>();
    TablingBindingsTest() throws IOException {
        // Тестовые случаи находятся в файле test-cases.sql
        try (Scanner casesScanner = new Scanner(new File("test-cases.sql"))){
            StringBuilder caseBuilder = null;
            while (casesScanner.hasNextLine()){
                String line = casesScanner.nextLine();
                if (line.contains("@")){
                    if (caseBuilder != null){
                        cases.add(new Case(caseBuilder.toString()));
                    }
                    caseBuilder = new StringBuilder();
                }
                if (caseBuilder != null){
                    caseBuilder.append(line).append("\n");
                    if (!casesScanner.hasNextLine()){
                        cases.add(new Case(caseBuilder.toString()));
                    }
                }
            }
        }
    }

//    @Test
    void testAllCases() {
        cases.forEach(aCase -> {
            final SqlRowSet testingRowSet = getTestingRowSet(aCase.caseProcedure);
            final HashSet<BindingPair> testingPairs = new HashSet<>();
            while (testingRowSet.next()){
                testingPairs.add(new BindingPair(
                        testingRowSet.getString("main_table"),
                        testingRowSet.getString("inlet_table")));
            }
            System.out.println(aCase.caseName + " testing" +
                    "\nprocedure: \n" +
                    aCase.caseProcedure );
            if (!testingPairs.equals(aCase.bindingPairs)){
                fail(aCase.caseName + ":" +
                        "\ncorrect\t: " + aCase.bindingPairs.toString() +
                        "\ngotten\t: " + testingPairs.toString()
                );
            }
            else System.out.println(aCase.caseName + " successfully passed");
        });
    }

    private class Case {
        private final String caseName;
        private final String caseProcedure;
        private final HashSet<BindingPair> bindingPairs = new HashSet<>();
        Case(String sqlCase) {
            caseName = sqlCase.substring(sqlCase.indexOf("@") + 1, sqlCase.indexOf('{')).trim();
            caseProcedure = sqlCase.substring(sqlCase.indexOf('}') + 1);
            for (String pair : sqlCase.substring(sqlCase.indexOf('{') + 1, sqlCase.indexOf('}')).split(";")) {
                bindingPairs.add(new BindingPair(pair.split("<-")));
            }
        }
    }
    private class BindingPair {
        private final String mainTable;
        private final String inletTable;
        BindingPair(String [] pair){
            this.mainTable = pair[0].trim();
            this.inletTable = pair[1].trim();
        }
        BindingPair(String mainTable, String inletTable){
            this.mainTable = mainTable.trim();
            this.inletTable = inletTable.trim();
        }

        @Override
        public String toString() {
            return mainTable + " <- " + inletTable;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BindingPair that = (BindingPair) o;
            return Objects.equals(mainTable, that.mainTable) && Objects.equals(inletTable, that.inletTable);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mainTable, inletTable);
        }
    }

    private SqlRowSet getTestingRowSet(String procedureText){
        procedureText = procedureText.replaceAll("'", "''");
        return template.queryForRowSet("SELECT main_table, inlet_table FROM magic.get_binding_tables('"+procedureText+"')");
    }
}
