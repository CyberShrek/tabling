const updateEvent = new Event("update")

function updateDisplays() {
    if (columnsDisplayEl.classList.contains('visible'))
        updateColumnsDisplay()
    else if (bindingsDisplayEl.classList.contains('visible'))
        updateBindingsDisplay()
}
function hideDisplays() {
    if (columnsDisplayEl.classList.contains("visible"))
        hideColumnsDisplay()
    else if (bindingsDisplayEl.classList.contains("visible"))
        hideBindingsDisplay()
}

function getForeignTables(tableBlock) {
    // Костыль, переделать
    let foreignTables = [];
    let foreignTablesString = tableBlock.getElementsByTagName("foreign-tables")[0].textContent;
    if (foreignTablesString !== "[]") {
        foreignTablesString = foreignTablesString.replace("[", "")
            .replace("]", "").split(", ");
        let idPostfix = tableBlock.id.toString().split('|')[1]
        for (const foreignTablesName of foreignTablesString) {
            const foreignTableId = foreignTablesName + "|" + idPostfix
            const foreignTable = document.getElementById(foreignTableId)
            if (foreignTable !== null)
                foreignTables.push(foreignTable)
            else
                console.error("Foreign table with id '" + foreignTableId + "' is not found")
        }
    }
    return foreignTables;
}

function showTableStructure(schemaName, tableName) {
    const url = "menu/table-structure"
    let tableStructure = {}
    try{
        popUpEl.classList.add("visible")
        popUpEl.querySelector(".loading-indicator").style.display = ""
        let ajax = createTableStructureAjax(url, true)
        ajax.send()
        ajax.onload = () => {
            if (ajax.status === 200) {
                tableStructure = JSON.parse(ajax.responseText)
                popUpEl.querySelector(".loading-indicator").style.display = "none"
                tableStructureEl.querySelector("tr td")
                    .textContent = tableStructure.header
                fillTableStructure()
                tableStructureEl.classList.add("visible")
            }
        }
        function createTableStructureAjax(url, async) {
            let ajax = new XMLHttpRequest();
            ajax.open("GET", url, async);
            ajax.setRequestHeader("schemaName", schemaName);
            ajax.setRequestHeader("tableName", tableName);
            return ajax
        }

        function fillTableStructure() {
            let changeColor = false
            for (const tableStructureRow of tableStructure.rows) {
                    const row = document.createElement("tr"),
                    comment = document.createElement("td"),
                    name = document.createElement("td"),
                    dataType = document.createElement("td");
                if (changeColor)
                    row.style.backgroundColor = "var(--second-color)"
                changeColor = !changeColor

                comment.className = "comment"
                name.className = "name"
                dataType.className = "dataType"

                if(tableStructureRow.comment != null)
                    comment.append(tableStructureRow.comment)
                name.append(tableStructureRow.name)
                dataType.append(tableStructureRow.dataType)

                row.append(comment)
                row.append(name)
                row.append(dataType)
                tableStructureEl.append(row);
            }
        }
    }
    catch (e) {
        alert(e)
    }
}

function closePopUp() {
    popUpEl.classList.remove("visible")
    if (tableStructureEl.classList.contains("visible"))
        flushTableStructure()
    templateEditorEl.classList.remove("visible")

    function flushTableStructure() {
        tableStructureEl.classList.remove("visible")
        let rows = tableStructureEl.getElementsByTagName("tr")
        while(rows.length > 2){
            rows[2].remove()
            console.log("removed")
        }
    }
}