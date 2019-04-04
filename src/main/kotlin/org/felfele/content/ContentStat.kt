package org.felfele.content

import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.syndication.feed.synd.SyndCategory
import com.sun.syndication.feed.synd.SyndEntry
import com.sun.syndication.io.SyndFeedInput
import org.xml.sax.InputSource
import java.io.File
import java.net.URL
import javax.sql.rowset.spi.XmlReader
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory





object ContentStat{
    val json = ObjectMapper()
    @JvmStatic
    fun main(args:Array<String>){

val taggedBlogs = (json.readValue(ClassLoader.getSystemResourceAsStream("taggedloggedoutpicks.json"),List::class.java) as List<Map<String,Any?>>)

        val perS = "\\s".toRegex()
val words = taggedBlogs.flatMap{ b->
    val blog = b["first"] as Map<String, Any?>
    val tags = b["second"] as List<String>?
    if (tags!=null){
        val name = blog["name"]?.toString()?.split(perS) ?: listOf()
        val description = blog["description_sanitized"]?.toString()?.split(perS)?: listOf()
        val title = blog["title"]?.toString()?.split(perS)?: listOf()
        val flatTags = tags.flatMap {
            it.split(perS)
        }
        flatTags + name + description + title
    }else{
        listOf()
    }



}
        val counts  = count(words).toList().sortedByDescending { it.second }

        json.writeValue(File("/Users/gmora/felfele/tumblrssCnt.json"),counts)
        //json.writeValue(File("/Users/gmora/felfele/tumblrss.json"),tumbrss)

    }

    private fun count(words: List<String>): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        words.forEach { w->
            val t = w.trim().toLowerCase()
            val cnt = map[t] ?: 0
            map[t] = (cnt+1)
        }
        return map
    }
}