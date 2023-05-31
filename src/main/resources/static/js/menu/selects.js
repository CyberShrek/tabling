// Массив выбранных таблиц и их имём
let selectedTables = [];

// Обновляет массив выбранных таблиц и зависимые от них поля документа
function updateSelectedTables() {
    let tables = [], names = []
    for (const unit of document.getElementsByClassName("unit")) {
        if (unit.getElementsByTagName("input")[0].checked.valueOf()){
            tables.push(unit);
            names.push(unit.id);
        }
    }
    selectedTables = tables
    document.getElementById("selected-tables").value = names;
    updateDisplays();
    updateSqlCreator();
}

function getAllTables() {
    let tablesNames=[];
    for (const unit of document.getElementsByClassName("unit")) {
        tablesNames.push(unit);
    }
    return tablesNames;
}