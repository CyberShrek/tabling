package org.vniizt.tabling.entity

class SqlProcedure(private var procedureText: String){

    init {
        // Preparing for analysis
        procedureText = procedureText
            .erase(simpleComment)
            .erase("\n")
            .erase(bracketedComment)
            .replace(whitespace, " ")
            .replace(semicolonSeparator, ";")
            .trim()
    }

    val variables: Map<String, Variable> = mutableMapOf<String, Variable>().apply {
        DECLAREBody.find(procedureText)?.value
            ?.split(';')
            ?.forEach {
                with(Variable(it)){
                    this@apply[this.name] = this
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

        val DECLAREBody = Regex("(?<=^DECLARE\\s)[\\s\\S]*?(?=\\sBEGIN)")
        val BEGINBody   = Regex("(?<=BEGIN\\s)[\\s\\S]*?(?=\\sEND;$)")

        val blockStartOperators = Regex("(?i)")
        val blockEndOperators = Regex("(?i)")

        val tableName = Regex("[a-z_][a-z0-9_]*?\\.[a-z_][a-z0-9_]*?")

        object IFBlock {
            val IF = Regex("(?i)")
            val THEN = Regex("(?i)")
            val ELSEIF = Regex("(?i)")
            val ELSE = Regex("(?i)")
        }

        object LOOPBlock {

        }

        class Block(
            val begin: Regex,
            val end: Regex
        ){
            val body = Regex("(?<=BEGIN\\s)[\\s\\S]*?(?=\\sEND;)")
        }

        private val ignoreQuotesPattern
            get() = "(?=(?:[^']*'[^']*')*[^']*\$)"
    }


    private fun String.erase(value: String) = replace(value, "")
    private fun String.erase(regex: Regex) = replace(regex, "")

    class Variable(initText: String){
        val name = initText.substringBefore(" ")
        val type: String
        val value: String?
        init {
            with(initText.substringAfter(" ").split(assignmentOperator)){
                type = get(0)
                value = getOrNull(1)
            }
        }
    }

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