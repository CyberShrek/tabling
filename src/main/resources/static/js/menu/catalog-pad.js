const markerClassName = "marker";              // чекбокс "выбрать всё"
const titleClassName  = "title";               // для заголовка меню

let menu,
    marker;
function initMenuParams(menuId) {
    menu = document.getElementById(menuId);
    marker = menu.getElementsByClassName(markerClassName)[0];
}

// Фильтрует строки в меню по введённому имени, сопоставляя как схемы, так и вложенные в них таблицы.
function filtrate(inputValue) {
    inputValue = inputValue.toLowerCase();
    const menus = document.getElementsByClassName("menu"),
        units = document.getElementsByClassName("unit");
    let currentSchemaName,
        schemaIsValid;
    for (let i = 0; i < units.length; i++) {
        const elementName = units.item(i).id.toLowerCase();
        const actualSchemaName = elementName.substring(0, elementName.indexOf("."));
        if (currentSchemaName == null) {
            currentSchemaName = actualSchemaName;
        }
        if (currentSchemaName !== actualSchemaName || i === units.length - 1) {
            menus.namedItem(currentSchemaName).style.display = (schemaIsValid) ? "" : "none"
            currentSchemaName = actualSchemaName;
            schemaIsValid = false;
        }
        if (elementName.indexOf(inputValue) > -1) {
            units.item(i).style.display = "";
            schemaIsValid = true;
        } else units.item(i).style.display = "none";
    }
    for (const menu of menus)
        handleCheckboxes(menu.id)
}

// Сбрасывает в меню все выбранные чекбоксы, цвета тайтлов и открытые секции
function resetMenu() {
    const menus = document.getElementsByClassName("menus scroller")[0];

    for (const menu of menus.getElementsByClassName("menu"))
        if (menu.classList.contains("open"))
            collapseMenu(menu.id);          // с анимацией схлопывает все меню

    for (const input of menus.getElementsByTagName("input"))
        if (input.type === "checkbox")
            input.checked = false;
}

// Отмечает либо убирает все видидимые чекбоксы в форме используя значение чекбокса "marker"
function markAll(menuId) {
        initMenuParams(menuId)
        markTitleLike(marker.checked);
        for (const unit of menu.getElementsByClassName("unit"))
            if (unit.style.display !== "none")
                unit.getElementsByTagName("input")[0].checked = marker.checked;
}

// Обрабатывает чекбоксы меню, изменяя цвет "title" и значение "markAll" в зависимости от выбранных значений в форме
function handleCheckboxes(menuId) {
    initMenuParams(menuId)
    let someUnitsIsTrue = false,
        allUnitsIsTrue = true;
    for (const unit of menu.getElementsByClassName("unit")) {
            const value = unit.getElementsByTagName("input").item(0).checked.valueOf();
            someUnitsIsTrue += value;
        if(unit.style.display !== "none")    // Помечает только те маркеры, которые видны пользователю
            allUnitsIsTrue *= value;
    }
    markTitleLike(someUnitsIsTrue);
    marker.checked = allUnitsIsTrue;
}

// Раскрывает меню с анимацией
function collapseMenu(menuId) {
    const menu = document.getElementById(menuId);
    if (menu.classList.contains("open")) {
        menu.classList.add("close");
        menu.addEventListener("animationend", function (event) {
            if (event.animationName === "animate-close-unit")
                menu.classList.remove("close","open");
        })
    } else menu.classList.add("open");
}

function markTitleLike(value) {
    if (value)
        menu.classList.add("selected");
    else menu.classList.remove("selected");
}