package org.vniizt.tabling.entity;

import lombok.Data;

/**
 * @author Alexander Ilyin
 */

// Сущность описывает параметры, которые используются при генерации документа
@Data
public class DocumentParams {
    private String fileName;
    // Список таблиц вида schema.table
    private String[] tables;
}
