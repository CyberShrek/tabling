package org.vniizt.tabling.entity

import org.vniizt.tabling.util.Regexes

data class SqlProcedure(val procedureText: String){

    val variables: Map<String, Variable> = mutableMapOf()

    val expressions: List<Expression> = mutableListOf()

    init {
        val lines = procedureText
            .erase(Regexes.Sql.simpleComment)
            .replace(Regexes.whitespace, " ")
            .erase(Regexes.Sql.bracketedComment)
            .trim()
            .split(Regexes.Sql.semicolonSeparator)

        var lineId = 0

        // Moving to BEGIN
        while (Regexes.Sql.Procedure.BEGIN.find(lines[lineId]) == null){
            // Looking for variables
            with(Variable(
                if(variables.isNotEmpty()) lines[lineId]
                else lines[lineId].erase(Regexes.Sql.Procedure.DECLARE))){
                (variables as MutableMap)[this.name] = this
            }
            lineId++
        }
    }

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

    interface Expression

    open class ConditionalExpression(expressionText: String,
                                     conditionRegex: Regex): Expression
    {
        val conditionText: String = conditionRegex.find(expressionText)?.value ?: ""
    }

    class IFExpression(expressionText: String): ConditionalExpression(
        expressionText,
        Regexes.Sql.Procedure.IFExpression.IF
    ) {
        val thenExpressions: List<Expression> = listOf()
        val elsifExpressions: List<ELSIFExpression> = listOf()
        val elseExpressions: List<Expression> = listOf()

        class ELSIFExpression(expressionText: String): ConditionalExpression(
            expressionText,
            Regexes.Sql.Procedure.IFExpression.ELSEIF
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

    open class OtherExpression(val text: String): Expression

    class QueryExpression(text: String): OtherExpression(text){

    }
}