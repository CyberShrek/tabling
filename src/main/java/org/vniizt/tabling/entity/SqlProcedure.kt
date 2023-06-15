package org.vniizt.tabling.entity

import org.vniizt.tabling.util.Regexes

class SqlProcedure(procedureText: String){

    val variables: Map<String, Variable> = mutableMapOf()

    val expressions: List<Expression> = mutableListOf()

    init {
        val preparedProcedureText = procedureText // Preparing for analysis
            .erase(Regexes.Sql.simpleComment)
            .erase("\n")
            .erase(Regexes.Sql.bracketedComment)
            .replace(Regexes.whitespace, " ")
            .replace(Regexes.Sql.semicolonSeparator, ";")
            .trim()

        // Writing variables
        Regexes.Sql.Procedure.DECLAREBlock.content.find(preparedProcedureText)?.value
            ?.split(';')?.forEach {
                with(Variable(it)){
                    (variables as MutableMap)[this.name] = this
                }
            }

        Regexes.Sql.Procedure.BEGINBlock.content.find(preparedProcedureText)?.value
    }

    private fun String.erase(value: String) = replace(value, "")
    private fun String.erase(regex: Regex) = replace(regex, "")

    class Variable(initText: String){
        val name = initText.substringBefore(" ")
        val type: String
        val value: String?
        init {
            with(initText.substringAfter(" ").split(Regexes.Sql.Procedure.assignmentOperator)){
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
        Regexes.Sql.Procedure.IFBlock.IF
    ) {
        val thenExpressions: List<Expression> = listOf()
        val elsifExpressions: List<ELSIFExpression> = listOf()
        val elseExpressions: List<Expression> = listOf()

        class ELSIFExpression(expressionText: String): ConditionalExpression(
            expressionText,
            Regexes.Sql.Procedure.IFBlock.ELSEIF
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