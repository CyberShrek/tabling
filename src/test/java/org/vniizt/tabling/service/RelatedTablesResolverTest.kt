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
    fun play() {
        val case = readCase(1)

        println(case)

        println(SqlProcedure(case))

    }

    private fun processTestCase(number: Byte){

    }

    fun readLinesFromFile(filePath: String): List<String> {
        val file = File(filePath)
        return file.readLines()
    }

    fun readCase(caseNumber: Int): String {
        val file = File("test-cases/case-$caseNumber.sql")
        return file.readText()
    }
}