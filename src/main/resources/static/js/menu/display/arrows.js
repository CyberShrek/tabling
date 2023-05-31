let updateArrowsEvent = new Event("updateArrows");
let hideArrowsEvent = new Event("hideArrows");
let removeArrowsEvent = new Event("removeArrows");

const basicArrowColor = "slategrey",
    innerArrowColor = "indianred",
    outerArrowColor = "deepskyblue"

// Добавляет стрелки-соединители связанных таблиц
function addLeaderArrow(table, foreignTable, display) {
    let arrow;
    display.addEventListener("updateArrows", function () {
        if (table.style.display !== "none" && foreignTable.style.display !== "none") {
            if (arrow == null) {
                arrow = createArrow(table, foreignTable)
            } else {
                arrow.position()
                arrow.show("none")
            }
        } else if (arrow != null) {
            arrow.hide("none")
        }
    }, false);
    display.addEventListener("hideArrows", function () {
        if (arrow != null) {
            arrow.hide("none")
        }
    }, false);
    display.addEventListener("removeArrows", function () {
        if (arrow != null) {
            arrow.remove()
            arrow = null
        }
    }, false);

    table.addEventListener("mouseenter", function () {addOuterHighlights()}, false);
    table.addEventListener("mouseleave", function () {removeHighlights()}, false);
    foreignTable.addEventListener("mouseenter", function () {addInnerHighlights()}, false);
    foreignTable.addEventListener("mouseleave", function () {removeHighlights()}, false);
    document.addEventListener("scroll", function () {removeHighlights()}, false);

    function addInnerHighlights() {
        table.classList.add("highlight")
        if (arrow != null)
            arrow.setOptions({
                color: innerArrowColor,
                outline: false,
                startPlugColor: innerArrowColor,
                endPlugColor: innerArrowColor,
                dash: {animation: true}})
    }
    function addOuterHighlights() {
        foreignTable.classList.add("highlight")
        if (arrow != null)
            arrow.setOptions({
                color: outerArrowColor,
                outline: false,
                startPlugColor: outerArrowColor,
                endPlugColor: outerArrowColor,
                dash: {animation: true}})
    }
    function removeHighlights() {
        table.classList.remove("highlight")
        foreignTable.classList.remove("highlight")
        if (arrow != null)
            arrow.setOptions({
                color: 'initial',
                outline: true,
                outlineColor: basicArrowColor,
                startPlugColor: basicArrowColor,
                endPlugColor: basicArrowColor,
                dash: false,})
    }
    function createArrow(start, end) {
        return new LeaderLine(start, end,
            {
                size: 3,
                color: 'initial',
                outline: true,
                outlineColor: basicArrowColor,
                startPlug: 'square',
                endPlug: 'arrow2',
                startPlugColor: basicArrowColor,
                endPlugColor: basicArrowColor,
                startSocket: 'bottom',
                endSocket: 'top',
            })
    }
}

function updateArrowsInDisplay(display) {
    display.dispatchEvent(updateArrowsEvent)
    crapMe()
}
function hideArrowsInDisplay(display) {
    display.dispatchEvent(hideArrowsEvent)
}
function removeArrowsFromDisplay(display) {
    display.dispatchEvent(removeArrowsEvent)
}

// Создаёт CSS стрелку для Row
function createRowArrow() {
    let arrow = document.createElement('arrow')
    arrow.innerHTML = '<arrow-top></arrow-top><arrow-bottom></arrow-bottom>'
    return arrow;
}

// Говнокостыль. Вызов метода hide у стрелок порождает пустоты в документе. Event resize убирает эти пустоты.
function crapMe() {
    if (window.scrollY > displaysEl.offsetHeight) {
        window.scrollTo(0, displaysEl.offsetHeight)
    }
    window.dispatchEvent(new Event("resize"))
}