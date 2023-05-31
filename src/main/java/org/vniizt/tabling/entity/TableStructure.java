package org.vniizt.tabling.entity;

import lombok.Builder;
import lombok.Data;

import java.util.LinkedHashSet;

@Data
@Builder
public class TableStructure {
    private String header;
    private LinkedHashSet<TableStructureRow> rows;

    public TableStructure(String header, LinkedHashSet<TableStructureRow> rows){
        this.header = header;
        this.rows = rows;
    }
}
