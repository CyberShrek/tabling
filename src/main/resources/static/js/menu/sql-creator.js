let procedureNames = []

function updateSqlCreator() {
    const sqlCreator = document.getElementById("sql-creator"),
        notFoundNota = sqlCreator.querySelector(".notation")

    flushProcedures()
    updateProceduresNames()

    if (procedureNames.length > 0) {
        notFoundNota.style.display = "none"
        appendProcedures()
    }
    else {
        notFoundNota.style.display = ""
    }

    function appendProcedures() {
        for (const procedureName of procedureNames) {
            sqlCreator.querySelector(".procedures").insertAdjacentHTML("beforeend",
                "<label class=\"procedure\"><input type=\"checkbox\">"+ procedureName +"</label>"
            )
        }
    }

    function flushProcedures() {
        sqlCreator.querySelectorAll(".procedure").forEach(
            procedure => procedure.remove())
    }

    function updateProceduresNames() {
        let queriesNames = []
        for (const selectedTable of selectedTables) {
            // Костыль, переделать
            let queriesString = selectedTable.querySelector("procedure-tables").textContent
            if (queriesString !== "[]") {
                queriesString.replace("[", "")
                    .replace("]", "")
                    .split(", ")
                    .forEach(queriesName => queriesNames.push(queriesName))
            }
        }
        procedureNames = queriesNames
    }
}

function generateSqlUsingForm(form) {
    const
        procedures = form.querySelectorAll(".procedure"),
        dateFrom   = new Date(form.elements["from"].value),
        dateTo     = new Date(form.elements["to"].value)

    let sql = ""

    for (const procedure of procedures) {
        if (procedure.querySelector("input").checked.valueOf()) {
            const date = new Date(dateFrom)
            while (date <= dateTo) {
                sql += "SELECT " + procedure.textContent + "('" + formatDate(date) + "');\n"
                date.setDate(date.getDate() + 1)
            }
        }
    }

    writeSQL()

    function formatDate(date) {
        let day   = date.getDate(),
            month = date.getMonth() + 1,
            year  = date.getFullYear()

        return year + "-" +
            ((month < 10) ? "0" + month : month) + "-" +
            ((day < 10) ? "0" + day : day)
    }

    // Клиент получит готовый файл так, как будто он его скачал с сервера
    function writeSQL() {
        const downloader = document.createElement("a")
        downloader.href = window.URL.createObjectURL(
            new Blob([sql], {type : "application/sql"}))
        downloader.download = "script.sql"
        downloader.click()
        downloader.remove()
    }
}