package org.vniizt.tabling.service

import org.junit.jupiter.api.Test
import org.vniizt.tabling.entity.RelatedTables
import java.io.File
import kotlin.test.fail

class
RelatedTablesResolverTest {

    private val resolver = RelatedTablesResolver()

    @Test
    fun findRelatedTablesTest(){
        var caseNumber = 1
        while (true){
            println(caseNumber)
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
                        |related tables: 
                        |   $relatedTablesSet
                        |expected: 
                        |   $expectedRelatedTablesSet
                    """.trimMargin())

                else println("!!! PASSED !!!")

                caseNumber++
            }
            catch (_: Exception){
                break
            }
        }
    }
}