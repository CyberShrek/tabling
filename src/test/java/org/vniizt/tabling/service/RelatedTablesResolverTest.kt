package org.vniizt.tabling.service

import org.junit.jupiter.api.Test
import org.vniizt.tabling.entity.RelatedTables
import org.vniizt.tabling.entity.SqlProcedure
import java.io.File
import kotlin.test.DefaultAsserter.fail

class RelatedTablesResolverTest {

    private val resolver = RelatedTablesResolver()

//    @Test
//    fun findRelatedTablesTest(){
//        var caseNumber = 1
//        while (true){
//            try {
//                val caseName = "test-cases/case-$caseNumber"
//                val relatedTablesSet = resolver.findRelatedTables(File("$caseName.sql").readText())
//                val expectedRelatedTablesSet = File("$caseName.result").readLines().map {
//                    RelatedTables(
//                        it.substringBefore(" -> "),
//                        it.substringAfter(" -> "))
//                }.toHashSet()
//
//                if(relatedTablesSet != expectedRelatedTablesSet)
//                    fail("""
//                        |related tables:\n$relatedTablesSet
//                        |expected:\n$expectedRelatedTablesSet
//                    """.trimMargin())
//
//                caseNumber++
//            }
//            catch (_: Exception){
//                break
//            }
//        }
//    }

    @Test
    fun play(): Unit {
        val str = "some text -|- -|- -|- -|- -|- some text"
        var count = 0
        val result = str.replace(Regex("-\\|-") ) { it.value+(count++).toString() }
        println(result)

        val str1 = "inter''val '  1   da'' ''y   ''1 da   '   'ddcd' ' ''y   ' rfreferrerggrgrg"
        //unescaped quote
        Regex("'(?:''|[^'])*'").findAll(str1).map { it.value.replace(Regex("'(?!')"), "") }.forEach {
            println(it)
        }


    }

//    @Test
    fun testIFExpressions(){
        try{
            with(SqlProcedure.IFExpression("""
                |IF (a != b) THEN a := b; END IF
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