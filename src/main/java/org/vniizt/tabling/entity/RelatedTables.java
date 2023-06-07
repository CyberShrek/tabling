package org.vniizt.tabling.entity;

import lombok.Data;

@Data
public class RelatedTables {
    public String startTable;
    public String endTable;

    public RelatedTables(String startTable, String endTable){
        this.startTable = startTable;
        this.endTable = endTable;
    }
}
