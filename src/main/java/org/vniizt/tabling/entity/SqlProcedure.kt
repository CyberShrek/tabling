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
            .replace(semicolonSeparator, ";")
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

    val expressions = CompositeExpression(procedureText).expressions

    private companion object Regexes {

        private const val ignoreQuotesPattern = "(?=(?:[^']*'[^']*')*[^']*\$)"
        private const val basicRowPattern = "(?<=;|^).*?(?=;)"
        private const val loopBlockPattern = "(?i)(?<=;|^|)(?:|[^;]*? )LOOP (?:(?R)|.)*?(?<=;)END LOOP(?=(?: [^;]*|);)"

        val whitespace = Regex("\\s+")
        val simpleComment = Regex("--.*")
        val bracketedComment = Regex("/\\*.*\\*/")
        val semicolonSeparator = Regex("\\s*;\\s*$ignoreQuotesPattern")
        val assignmentOperator = Regex("\\s?(=|:=)\\s?")

        val BEGINBody   = Regex("(?i)(?<=BEGIN\\s)[\\s\\S]*?(?=END(?: [^;]*|);\$)")

        val quotedText = Regex("'(?:''|[^'])*'")
        val unescapedQuote = Regex("'(?!')")

        val tableName = Regex("(?i)[a-z_][a-z0-9_]*?\\.[a-z_][a-z0-9_]*?")

        val expression = Regex("$loopBlockPattern|$basicRowPattern")

        val loopExpressionEnd = Regex("(?i)(?<=;| )END LOOP(?=;\$|\$)")
        val loopExpressionCondition = Regex("(?i)(?i)(?<=^)[^;]*?(?= LOOP )")
    }

    interface Expression

    open inner class CompositeExpression(text: String): Expression{
        val expressions: List<Expression> = text
            .find(BEGINBody)?.value
            ?.findAll(expression)?.toList()
            ?.map { createExpression(it.value) }
            ?: emptyList()
    }

    inner class BasicRowExpression(val text: String): Expression

    inner class LoopBlockExpression(text: String): CompositeExpression(text){
        val condition: String = text.find(loopExpressionCondition)?.value ?: ""
    }

    private fun createExpression(expressionText: String) =
        if(expressionText.contains(loopExpressionEnd))
            LoopBlockExpression(expressionText)
        else
            BasicRowExpression(expressionText)

    private fun String.erase(value: String) = replace(value, "")
    private fun String.erase(regex: Regex) = replace(regex, "")
    private fun String.find(regex: Regex) = regex.find(this)
    private fun String.findAll(regex: Regex) = regex.findAll(this)
}