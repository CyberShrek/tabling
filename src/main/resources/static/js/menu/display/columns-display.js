let columnsDisplayScroll = 0

function showColumnsDisplay() {
    if (!columnsDisplayEl.classList.contains("filled")) {
        fillColumnsDisplay()
        columnsDisplayEl.classList.add("filled")
    }
    columnsDisplayEl.classList.add("visible")
    window.scrollTo(0, columnsDisplayScroll)

    function fillColumnsDisplay() {
        const rows = columnsDisplayEl.getElementsByTagName("row");
        // adding outcome tables and assigning rows id
        for (const row of rows) {
            const tableBlock = row.getElementsByClassName("table-block")[0]
            // assigning rows id
            row.id = tableBlock.id + "-row"
            // adding tables
            for (const foreignTable of getForeignTables(tableBlock)) {
                const foreignTableClone = foreignTable.cloneNode(true)
                const outers = row.getElementsByTagName("outers")[0]
                foreignTableClone.style.background = "var(--second-table-block-color)"
                outers.append(foreignTableClone)
                if (outers.childNodes.length === 1)
                    outers.before(createRowArrow())
            }
        }
        // adding income tables
        for (const row of rows) {
            for (const outerTable of row.getElementsByTagName("outers")[0].getElementsByClassName("table-block")) {
                const innerTable = row.getElementsByTagName("selected")[0].getElementsByClassName("table-block")[0].cloneNode(true)
                const targetRow = document.getElementById(outerTable.id + "-row")
                const inners = targetRow.getElementsByTagName("inners")[0]
                innerTable.style.background = "var(--second-table-block-color)"
                inners.append(innerTable)
                if (inners.childNodes.length === 1)
                    inners.after(createRowArrow())
            }
        }
    }
}
function updateColumnsDisplay() {
    for (const row of columnsDisplayEl.getElementsByTagName("row"))
        row.style.display = "none"
    if (selectedTables.length > 0) {
        columnsDisplayEl.getElementsByClassName("notation")[0].classList.remove("visible")
        for (const selectedTable of selectedTables) {
            const row = document.getElementById(selectedTable.id + "|-in-columns-row")
            row.style.display = ""

            // Костыль, нужный для задания ширины стрелок относительно их высоты
            if (!row.classList.contains("arrowsWidthAssigned")) {
                let arrowHeight = null
                for (const arrow of row.getElementsByTagName("arrow")) {
                    if (arrowHeight === null)
                        arrowHeight = arrow.offsetHeight
                    arrow.style.width = String(arrowHeight/6) + "px"
                }
                row.classList.add("arrowsWidthAssigned")
            }
        }
    }
    else
        columnsDisplayEl.getElementsByClassName("notation")[0].classList.add("visible")
}
function hideColumnsDisplay() {
    columnsDisplayScroll = window.scrollY
    columnsDisplayEl.classList.remove("visible")
    columnsDisplayEl.getElementsByClassName("notation")[0].classList.add("visible")
}