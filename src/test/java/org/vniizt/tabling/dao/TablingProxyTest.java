package org.vniizt.tabling.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;

import static com.jayway.jsonpath.internal.path.PathCompiler.fail;

/**
 * @author Alexander Ilyin
 */

@SpringBootTest
class TablingProxyTest {

    @Autowired
    TablingJdbc jdbc;
    @Autowired
    TablingProxy proxy;

    @Test
    void compareProxyToJdbc() {
        proxy.updateCache();
//        compareProxyToJdbcSchemas();compareProxyToJdbcSchemas();
//        compareProxyToJdbcTables();compareProxyToJdbcTables();
//        compareProxyToJdbcSchemas();compareProxyToJdbcTables();compareProxyToJdbcSchemas();
//        compareProxyToJdbcForeignTables();
    }

    void compareProxyToJdbcSchemas(){
        Set <String> schemasListFromProxy = proxy.getSchemasSet();
        Set<String> schemasListFromJdbc = jdbc.getSchemasSet();
        if (!schemasListFromProxy.equals(schemasListFromJdbc))
            fail("\n" + schemasListFromProxy.toString() + "\nnot equals\n" +
                    schemasListFromJdbc.toString());
    }

    void compareProxyToJdbcTables(){
        for (String schema : jdbc.getSchemasSet()) {
            Set<String> tablesListFromProxy = proxy.getTablesBySchemaSet(schema);
            Set <String> tablesListFromJdbc = jdbc.getTablesBySchemaSet(schema);
            if (!tablesListFromProxy.equals(tablesListFromJdbc))
                fail("\n" + schema + "." + tablesListFromProxy.toString() + "\nnot equals\n" +
                        schema + "." + tablesListFromJdbc.toString());
        }
    }

    void compareProxyToJdbcForeignTables(){
        for (String schema : jdbc.getSchemasSet()) {
            for (String table : jdbc.getTablesBySchemaSet(schema)){
                HashSet<String> jdbcForeignTables = jdbc.getForeignTables(schema, table);
                HashSet <String> proxyForeignTables = proxy.getForeignTables(schema, table);
                if (!proxyForeignTables.equals(jdbcForeignTables))
                    fail("\n" + schema+"."+table + proxyForeignTables.toString() + "\nnot equals\n" +
                            schema+"."+table + jdbcForeignTables.toString());
            }
        }
    }
}