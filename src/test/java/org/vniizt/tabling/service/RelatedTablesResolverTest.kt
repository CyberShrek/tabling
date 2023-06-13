package org.vniizt.tabling.service

import org.junit.jupiter.api.Test
import org.vniizt.tabling.entity.RelatedTables
import org.vniizt.tabling.entity.SqlProcedure
import java.io.File
import kotlin.test.DefaultAsserter.fail

class RelatedTablesResolverTest {

    private val resolver = RelatedTablesResolver()

    private val prepareForAnalysisCases = mapOf(
        "-- some commented text" to "",
        """
            -- some commented text
            some text
            -- some commented text
        """.trimIndent()
                to "some text",
        "/*some commented text*/" to "",
        """
            /*some commented text
            some commented text*/
        """.trimIndent()
                to "",
        """
            /*
            some commented text
            some commented text
            */
        """.trimIndent()
                to "",
        """
            some text/*
            some commented text
            some commented text
            */
            some text -- some commented text
        """.trimIndent()
                to "some text some text",
        """
            
            
          
        """.trimIndent() to "",
        """
            some text
        """.trimIndent() to "some text",
        """some text
            some  text
        """.trimIndent() to "some text some text",
        """some text;
            some  text
        """.trimIndent() to "some text\nsome text",
        """
        some text   ; -- some commented text
            some  text
        """.trimIndent() to "some text\nsome text"
    )

    @Test
    fun prepareForAnalysisTest(){
        prepareForAnalysisCases.forEach {
            println("""
                |
                |original text:
                |'${it.key}'
                |changed text:
                |'${it.value}'
            """.trimMargin())
            with(resolver.prepareForAnalysis(it.key)){
                if(it.value != this) {
                    fail("expected changed text:\n$this")
                }
            }
        }
    }

    @Test
    fun findRelatedTablesTest(){
        var caseNumber = 1
        while (true){
            try {
                val caseName = "test-cases/case-$caseNumber"
                val relatedTablesSet = resolver.findRelatedTables(File("$caseName.sql").readText())
                val expectedRelatedTablesSet = File("$caseName.result").readLines().map {
                    RelatedTables(
                        it.substringBefore(" -> "),
                        it.substringAfter(" -> "))
                }.toHashSet()

                if(relatedTablesSet != expectedRelatedTablesSet)
                    fail("""
                        |related tables:\n$relatedTablesSet
                        |expected:\n$expectedRelatedTablesSet
                    """.trimMargin())

                caseNumber++
            }
            catch (_: Exception){
                break
            }
        }
    }

    @Test
    private fun testIFExpressions(){
        try{
            with(SqlProcedure.IFExpression("""
                IF (a != b) THEN a := b; END IF
            """.trimIndent())){
                assert(conditionText == "(a != b)")
//                assert()
            }
        }
        catch (error: AssertionError){
            fail(error.message)
        }
    }

    private fun processTestCase(number: Byte){

    }

    fun readLinesFromFile(filePath: String): List<String> {
        val file = File(filePath)
        return file.readLines()
    }

    fun readTextFromFile(filePath: String): String {
        val file = File(filePath)
        return file.readText()
    }
}