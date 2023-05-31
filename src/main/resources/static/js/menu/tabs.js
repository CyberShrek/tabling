function selectDisplayViaTab(tab) {
    tab.classList.toggle("selected")
    for (const anyTab of document.getElementsByClassName("tab"))
        if(anyTab !== tab)
            anyTab.classList.remove("selected")

    hideDisplays()
    if (tab.classList.contains("selected")){
        if (tab.id === "tab-for-columns-display")
            showColumnsDisplay()
        else if (tab.id === "tab-for-binding-display")
            showBindingsDisplay()
    }
    updateSelectedTables()
}

function collapseTabsPad(tabsPad) {
    if (tabsPad.classList.contains("collapse")) {
        tabsPad.classList.remove("collapse")
        displaysEl.classList.remove("expand")
    } else {
        tabsPad.classList.add("collapse")
        displaysEl.classList.add("expand")
    }
    if (bindingsDisplayEl.classList.contains("visible")){
        hideArrowsInDisplay(bindingsDisplayEl)
        document.getAnimations()[0].onfinish = () => {
            updateArrowsInDisplay(bindingsDisplayEl)
        }
    }
}
