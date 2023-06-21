package org.vniizt.tabling.entity

class SqlProcedure(private var procedureText: String){

    init {
        // Preparing for analysis
        procedureText = procedureText
            .erase(simpleComment)
            .erase("\n")
            .erase(bracketedComment)
            .trim()
            .replace(whitespace, " ")
            .replace(semicolonSeparatorQuotesIgnored, ";")
    }

    // Collecting quoted strings and replacing them with their own indices in the text
    val quotedStrings: List<String> = run {
        var quotedStringIndex = 0
        mutableListOf<String>().apply {
            procedureText = procedureText.replace(quotedText){
                this.add(it.value.erase(unescapedQuote))
                "'${quotedStringIndex++}'"
            }
        }
    }

    private companion object Regexes {

        private const val ignoreQuotesPattern = "(?=(?:[^']*'[^']*')*[^']*\$)"
        private const val basicRowPattern = "(?<=;|^).*?(?=;)"
        private const val loopBlockPattern = "(?i)(?<=;|^|)(?:|[^;]*? )LOOP (?:(?R)|.)*?(?<=;)END LOOP(?=(?: [^;]*|);)"

        val whitespace = Regex("\\s+")
        val simpleComment = Regex("--.*")
        val bracketedComment = Regex("/\\*.*\\*/")
        val semicolonSeparatorQuotesIgnored = Regex("\\s*;\\s*(?=(?:[^']*'[^']*')*[^']*\$)")
        val assignmentOperator = Regex("\\s?(=|:=)\\s?")

        val BEGINBody   = Regex("(?i)(?<=BEGIN\\s)[\\s\\S]*?(?=END(?: [^;]*|);\$)")

        val quotedText = Regex("'(?:''|[^'])*'")
        val unescapedQuote = Regex("'(?!')")

        val name = Regex("(?i)[a-z_][a-z0-9_]*")
        val entityName = Regex("$name\\.$name")
    }


    private fun String.erase(value: String) = replace(value, "")
    private fun String.erase(regex: Regex) = replace(regex, "")
    private fun String.find(regex: Regex) = regex.find(this)
    private fun String.findAll(regex: Regex) = regex.findAll(this)
}