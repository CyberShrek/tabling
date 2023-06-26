let updateArrowsEvent = new Event("updateArrows");
let hideArrowsEvent = new Event("hideArrows");
let removeArrowsEvent = new Event("removeArrows");

const basicArrowColor = "slategrey",
    innerArrowColor = "indianred",
    outerArrowColor = "deepskyblue"

// Добавляет стрелки-соединители связанных таблиц
function addLeaderArrow(startTable, endTable, display) {
    if (!startTable || !endTable || !display) return

    let arrow = createArrow(startTable, endTable);
    display.addEventListener("updateArrows", function () {
        if (startTable.style.display !== "none" && endTable.style.display !== "none") {
            if (arrow == null) {
                arrow = createArrow(startTable, endTable)
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

    startTable.addEventListener("mouseenter", function () {addOuterHighlights()}, false);
    startTable.addEventListener("mouseleave", function () {removeHighlights()}, false);
    endTable.addEventListener("mouseenter", function () {addInnerHighlights()}, false);
    endTable.addEventListener("mouseleave", function () {removeHighlights()}, false);
    document.addEventListener("scroll", function () {removeHighlights()}, false);

    function addInnerHighlights() {
        startTable.classList.add("highlight")
        if (arrow != null)
            arrow.setOptions({
                color: innerArrowColor,
                outline: false,
                startPlugColor: innerArrowColor,
                endPlugColor: innerArrowColor,
                dash: {animation: true}})
    }
    function addOuterHighlights() {
        endTable.classList.add("highlight")
        if (arrow != null)
            arrow.setOptions({
                color: outerArrowColor,
                outline: false,
                startPlugColor: outerArrowColor,
                endPlugColor: outerArrowColor,
                dash: {animation: true}})
    }
    function removeHighlights() {
        startTable.classList.remove("highlight")
        endTable.classList.remove("highlight")
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