package com.angcyo.library.component.xml

import android.util.Xml.newPullParser
import org.xmlpull.v1.XmlPullParser
import java.io.File

/**
 * https://developer.android.com/training/basics/network-ops/xml?hl=zh-cn
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/07/19
 */
object Xml {

    fun read(file: File, action: (parser: XmlPullParser, eventType: Int) -> Unit) {
        file.inputStream().use { inputStream ->
            val parser: XmlPullParser = newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            //parser.require(XmlPullParser.START_TAG, null, "rss")
            //parser.eventType
            //parser.next()
            //parser.nextTag()

            //parser.getAttributeValue()
            //parser.attributeCount
            //parser.getAttributeValue(null, "name")

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                action(parser, eventType)
                eventType = parser.next()
            }
        }
    }

}