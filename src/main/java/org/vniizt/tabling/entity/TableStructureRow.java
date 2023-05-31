package org.vniizt.tabling.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @author Alexander Ilyin
 */

@Data
@Builder
public class TableStructureRow {
    private String comment;
    private String name;
    private String dataType;

    public TableStructureRow(String comment, String name, String dataType){
        this.comment = comment;
        this.name = name;
        this.dataType = dataType;
    }
}
