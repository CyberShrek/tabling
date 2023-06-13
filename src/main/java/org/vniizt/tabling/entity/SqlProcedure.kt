package org.vniizt.tabling.entity

data class SqlProcedure(val rawText: String){

//        val variables: Set<Variable>

    interface Expression{
        val regex: Regex
    }

    open class ConditionalExpression(
        override val regex: Regex,
        conditionRegex: Regex

    ): Expression{
        val conditionText: String = ""
        val thenExpressions: List<Expression> = listOf()
    }

    class IFExpression(text: String): ConditionalExpression() {
        val elsifExpressions: List<ELSIFExpression> = listOf()
        val elseExpressions: List<Expression> = listOf()

        class ELSIFExpression(text: String): ConditionalExpression(){

        }
    }

    class CASEExpression(text: String, override val regex: Regex): Expression{
        val whenExpressions: List<WHENExpression> = listOf()
        val elseExpressions: List<Expression> = listOf()

        class WHENExpression(text: String): ConditionalExpression(){

        }
    }

    open class LOOPExpression(text: String): ConditionalExpression(){

    }

    open class LOOPQUERYExpression(text: String): LOOPExpression(text){
        val query: QueryExpression? = null
    }

    class RECORDLOOPExpression(text: String): LOOPQUERYExpression(text){
        val recordVariable: String = ""
    }

    open class OtherExpression(val text: String, override val regex: Regex): Expression

    class QueryExpression(text: String): OtherExpression(text){

    }
}