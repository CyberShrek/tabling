package org.vniizt.tabling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.vniizt.tabling.service.Tabling;
import org.vniizt.tabling.entity.RelatedTables;
import org.vniizt.tabling.entity.TableStructure;
import org.vniizt.tabling.entity.DocumentParams;

import java.util.Set;

/**
 * @author Alexander Ilyin
 */

@Controller
@RequestMapping("/menu")
public class MenuController {

    @Autowired
    private Tabling tabling;

    @GetMapping
    @Transactional
    public String getMenu(Model model){
        model.addAttribute("schemasTables", tabling.getSchemasTables());
        model.addAttribute("params", new DocumentParams());
        return "menu";
    }

    // Получение описания структуры выбранной таблицы
    @GetMapping(value = "/table-structure", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public TableStructure getTableStructure(@RequestHeader String schemaName,
                                            @RequestHeader String tableName){
        return tabling.getTableStructure(schemaName, tableName);
    }

    // Получение связей таблиц
    @GetMapping(value = "/related-tables", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Set<RelatedTables> getTableBindings(){
        return tabling.getRelatedTables();
    }
}


