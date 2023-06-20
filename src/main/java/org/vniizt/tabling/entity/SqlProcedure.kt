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

    val expressions: List<Expression> = mutableListOf<Expression>().apply {
        BEGINBody.find(
            procedureText

        )
    }

    private companion object Regexes {

        val whitespace = Regex("\\s+")
        val simpleComment = Regex("--.*")
        val bracketedComment = Regex("/\\*.*\\*/")
        val semicolonSeparator = Regex("\\s*;\\s*$ignoreQuotesPattern")
        val assignmentOperator = Regex("\\s?(=|:=)\\s?")

        val BEGINBody   = Regex("(?<=BEGIN\\s)[\\s\\S]*?(?=\\sEND;$)")

        val quotedText = Regex("'(?:''|[^'])*'")
        val unescapedQuote = Regex("'(?!')")

        val tableName = Regex("[a-z_][a-z0-9_]*?\\.[a-z_][a-z0-9_]*?")

        val basicRow = Regex("(?<=;|^).*?(?=;)")
        val loopBlock = Regex("(?<=;|^)(?:[^;]* |)LOOP (?:(?R)|.)*?(?<=;)END LOOP(?: [^;]*|);")

        private val ignoreQuotesPattern
            get() = "(?=(?:[^']*'[^']*')*[^']*\$)"

    }


    private fun String.erase(value: String) = replace(value, "")
    private fun String.erase(regex: Regex) = replace(regex, "")

    open class Expression(
//        val
    )

    class BEGINExpression(expressionText: String){

    }

    open class ConditionalExpression(expressionText: String,
                                     conditionRegex: Regex): Expression()
    {
        val conditionText: String = conditionRegex.find(expressionText)?.value ?: ""
    }

    class IFExpression(expressionText: String): ConditionalExpression(
        expressionText,
        IFBlock.IF
    ) {
        val thenExpressions: List<Expression> = listOf()
        val elsifExpressions: List<ELSIFExpression> = listOf()
        val elseExpressions: List<Expression> = listOf()

        class ELSIFExpression(expressionText: String): ConditionalExpression(
            expressionText,
            IFBlock.ELSEIF
        ){
            val thenExpressions: List<Expression> = listOf()
        }
    }

//    class CASEExpression(text: String): Expression{
//        val whenExpressions: List<WHENExpression> = listOf()
//        val elseExpressions: List<Expression> = listOf()
//
//        class WHENExpression(text: String): ConditionalExpression(){
//
//        }
//    }
//
//    open class LOOPExpression(text: String): ConditionalExpression(){
//
//    }
//
//    open class LOOPQUERYExpression(text: String): LOOPExpression(text){
//        val query: QueryExpression? = null
//    }
//
//    class RECORDLOOPExpression(text: String): LOOPQUERYExpression(text){
//        val recordVariable: String = ""
//    }

//    open class OtherExpression(val text: String): Expression
//
//    class QueryExpression(text: String): OtherExpression(text){
//
//    }
}