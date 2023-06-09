package org.vniizt.tabling.entity

import javax.management.Query

data class SqlProcedure(val rawText: String){

//        val variables: Set<Variable>

    interface Expression {
        val text: String
    }

    class QueryExpression(override val text: String): Expression{

    }

    class IFExpression(override val text: String): Expression {
        val conditionText: String = ""
        val thenExpressions: List<Expression> = listOf()
        val elsifExpressions: List<ELSIFExpression> = listOf()
        val elseExpressions: List<Expression> = listOf()

        class ELSIFExpression(override val text: String): Expression{
            val conditionText: String = ""
            val thenExpressions: List<Expression> = listOf()
        }
    }

    class CASEExpression(override val text: String): Expression{
        val whenExpressions: List<WHENExpression> = listOf()
        val elseExpressions: List<Expression> = listOf()

        class WHENExpression(override val text: String): Expression{
            val conditionText: String = ""
            val thenExpressions: List<Expression> = listOf()
        }
    }

    open class LOOPExpression(override val text: String): Expression{
        val conditionText: String = ""
        val thenExpressions: List<Expression> = listOf()
    }

    open class LOOPQUERYExpression(override val text: String): LOOPExpression(text){
        val query: QueryExpression? = null
    }

    class RECORDLOOPExpression(override val text: String): LOOPQUERYExpression(text){
        val recordVariable: String = ""
    }

    class OtherExpression(override val text: String): Expression
}