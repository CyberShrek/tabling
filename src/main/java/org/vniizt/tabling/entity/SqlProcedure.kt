package org.vniizt.tabling.entity

data class SqlProcedure(val rawText: String){

//        val variables: Set<Variable>

    data class Variable(
        val type: String,
        val name: String
    )

    interface Expression {
        val text: String
    }

    data class QueryExpression(override val text: String): Expression{

    }

    data class IFExpression(override val text: String): Expression {
        val conditionText: String = ""
        val thenExpressions: List<Expression> = listOf()
        val elsifExpressions: List<ELSIFExpression> = listOf()
        val elseExpressions: List<Expression> = listOf()

        data class ELSIFExpression(override val text: String): Expression{
            val conditionText: String = ""
            val thenExpressions: List<Expression> = listOf()
        }
    }

    data class CASEExpression(override val text: String): Expression{
        val whenExpressions: List<WHENExpression> = listOf()
        val elseExpressions: List<Expression> = listOf()

        data class WHENExpression(override val text: String): Expression{
            val conditionText: String = ""
            val thenExpressions: List<Expression> = listOf()
        }
    }

    data class LOOPExpression(override val text: String): Expression{
        val conditionText: String = ""
        val thenExpressions: List<Expression> = listOf()
    }

    data class OtherExpression(override val text: String): Expression
}