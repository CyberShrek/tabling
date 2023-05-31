function editDocTemplate() {
    const authUrl = "document/template/auth"
    let ajax = new XMLHttpRequest()

    ajax.open("GET", authUrl, true)
    ajax.send()
    ajax.onload = () => {
        if (ajax.status === 200){
            popUpEl.classList.add("visible")
            templateEditorEl.classList.add("visible")
            popUpEl.querySelector(".loading-indicator").style.display = "none"

            downloadTemplate()
        }
    }

    function downloadTemplate() {
        const downloader = document.createElement("a")
        downloader.href = "document/template/download"
        downloader.click()
        downloader.remove()
    }
}

window.onload = () => {

    // Добавление обработчиков для drag-n-drop редактора шаблона
    {
        ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
            templateEditorEl.addEventListener(eventName, function (event) {
                event.preventDefault()
                event.stopPropagation()
            })
        })

        templateEditorEl.addEventListener("dragenter", function () {
            templateEditorEl.classList.add("dragenter")
        })
        templateEditorEl.addEventListener("dragleave", function () {
            templateEditorEl.classList.remove("dragenter")
        })
        templateEditorEl.addEventListener("drop", function (event) {
            templateEditorEl.classList.remove("dragenter")
            const files = event.dataTransfer.files
            if (files.length !== 1){
                alert("Файл должен быть один")
                return
            }
            const file = files.item(0)
            if (file.name.substr(file.name.length - 5) !== ".docx"){
                alert("Файл должен иметь формат .docx")
                return
            }
            uploadFile(file)
        })

        function uploadFile(file) {
            let url = 'document/template/upload'
            let formData = new FormData()
            formData.append('file', file)
            fetch(url, {
                method: 'POST',
                body: formData,
            })
                .then(() => { alert("Шаблон документа успешно передан") })
                .catch(() => { alert("Ошибка передачи файла") })
        }
    }
}
