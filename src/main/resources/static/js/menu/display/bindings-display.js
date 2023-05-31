let bindingsDisplayScroll = 0

function showBindingsDisplay() {

    if (!bindingsDisplayEl.classList.contains("filled")) {
        addArrows()
        bindingsDisplayEl.classList.add("filled")
    }
    bindingsDisplayEl.classList.add("visible")
    window.scrollTo(0,bindingsDisplayScroll)

    function addArrows() {
        for (const tableBlock of bindingsDisplayEl.getElementsByClassName("table-block"))
            for (const foreignTable of getForeignTables(tableBlock))
                addLeaderArrow(tableBlock, foreignTable, bindingsDisplayEl)
    }
}

function updateBindingsDisplay() {
    for (const tablesSet of bindingsDisplayEl.getElementsByClassName("tables-set")) {
        tablesSet.style.display = "none"
        for (const tableBlock of tablesSet.getElementsByClassName("table-block"))
            tableBlock.style.display = "none"
    }
    if (selectedTables.length > 0) {
        bindingsDisplayEl.getElementsByClassName("notation")[0].classList.remove("visible")

        for (const selectedTable of selectedTables) {
            const tableBlock = document.getElementById(selectedTable.id + "|-in-bindings")
            tableBlock.style.display = ""
            tableBlock.parentElement.style.display = "" // tablesSet
        }
    }
    else
        bindingsDisplayEl.getElementsByClassName("notation")[0].classList.add("visible")
    updateArrowsInDisplay(bindingsDisplayEl)
}
function hideBindingsDisplay() {
    bindingsDisplayScroll = window.scrollY
    bindingsDisplayEl.classList.remove("visible")
    removeArrowsFromDisplay(bindingsDisplayEl)
}