package org.vniizt.tabling.service

import org.junit.jupiter.api.Test
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

    private val complexTestCases = mapOf("" to "")

    @Test
    fun prepareForAnalysisTest(){
        prepareForAnalysisCases.forEach {
            checkChanges(it.key, it.value, resolver.prepareForAnalysis(it.key).joinToString("\n"))
        }
    }

    private fun checkChanges(origText: String, expectedChangedText: String, changedText: String){
        println("""
                |
                |original text:
                |'$origText'
                |changed text:
                |'$changedText'
            """.trimMargin())
        if(changedText != expectedChangedText) {
            fail("expected changed text:\n$expectedChangedText")
        }
    }
}