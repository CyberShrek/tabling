package org.vniizt.tabling.entity

import org.vniizt.tabling.util.Regexes

data class SqlProcedure(val rawText: String){

//        val variables: Set<Variable>

    interface Expression

    open class ConditionalExpression(text: String,
                                     conditionRegex: Regex,
                                     startRegex: Regex): Expression
    {
        val conditionText: String = conditionRegex.find(text)?.value ?: ""
        val thenExpressions: List<Expression> = listOf()
    }

    class IFExpression(text: String): ConditionalExpression(
        text,
        Regexes.Sql.Procedure.IFExpression.IF,
        Regexes.Sql.Procedure.IFExpression.THEN
    ) {
        val elsifExpressions: List<ELSIFExpression> = listOf()
        val elseExpressions: List<Expression> = listOf()

        class ELSIFExpression(text: String): ConditionalExpression(
            text,
            Regexes.Sql.Procedure.IFExpression.ELSEIF,
            Regexes.Sql.Procedure.IFExpression.THEN
        ){

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