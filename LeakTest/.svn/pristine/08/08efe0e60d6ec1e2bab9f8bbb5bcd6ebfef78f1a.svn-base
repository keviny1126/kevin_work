package com.power.baseproject.utils.log

import android.util.Log
import com.power.baseproject.utils.log.Util.isEmpty
import com.power.baseproject.utils.log.Util.printLine
import java.io.StringReader
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

/**
 * autour: tanwu
 * date: 2017/8/15 12:11
 * description:
 * version:
 * modify by:
 * update: 2017/8/15
 */
object XmlLog {
    fun printXml(tag: String?, xml: String?, headString: String) {
        var xml = xml
        if (xml != null) {
            xml = formatXML(xml)
            xml = """
                $headString
                $xml
                """.trimIndent()
        } else {
            xml = headString + LogUtil.NULL_TIPS
        }
        printLine(tag, true)
        val lines = xml.split(LogUtil.LINE_SEPARATOR).toTypedArray()
        for (line in lines) {
            if (!isEmpty(line)) {
                Log.d(tag, "║ $line")
            }
        }
        printLine(tag, false)
    }

    fun formatXML(inputXML: String): String {
        return try {
            val xmlInput: Source = StreamSource(StringReader(inputXML))
            val xmlOutput = StreamResult(StringWriter())
            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            transformer.transform(xmlInput, xmlOutput)
            xmlOutput.writer.toString().replaceFirst(">".toRegex(), ">\n")
        } catch (e: Exception) {
            e.printStackTrace()
            inputXML
        }
    }
}