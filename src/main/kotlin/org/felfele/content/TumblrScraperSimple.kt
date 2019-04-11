package org.felfele.content

import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.syndication.feed.synd.SyndCategory
import com.sun.syndication.feed.synd.SyndEntry
import com.sun.syndication.io.SyndFeedInput
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory


object TumblrScraperSimple {

    val json = ObjectMapper()
    val factory = DocumentBuilderFactory.newInstance().apply {
        this.isValidating = false
    }

    //factory.isIgnoringElementContentWhitespace = true
    val builder = factory.newDocumentBuilder()

    @JvmStatic
    fun main(args: Array<String>) {

        val usenet = (json.readValue(File("usenetMap.json"), Map::class.java) as Map<String, Any>)
        val usenetTumblr = addTumblr(usenet)
        json.writeValue(File("usenetTumblrTree.json"), usenetTumblr)


    }

    fun addTumblr(usenetTree: Map<String, Any>): Map<String, Any> {
        return usenetTree.map { m ->
            m.key to replaceLeafs(m.toPair())
        }.toMap().filterValues { it != null } as Map<String, Any>

    }

    private fun replaceLeafs(m: Pair<String, Any?>): Any? {
        //string only
        return if (m.second == null) {
            getBlog(m.first)
        } else if (m.second is List<*>) {
            val catList = (m.second as List<String>)
            if (catList.isEmpty()) {
                replaceLeafs(m.first to null)
            } else {
                catList.map { s ->
                    replaceLeafs(s to null)
                }.filterNotNull().let{
                    if (it.isEmpty()){
                        null
                    }else{
                        it
                    }
                }
            }
        } else if (m.second is Map<*, *>) {
            (m.second as Map<String, Any?>).mapValues { m ->
                replaceLeafs(m.toPair())
            }.filterValues { it != null }
        } else {
            null
        }
    }

    private fun getBlog(name: String): Map<String, Any>? {
        return try {
            val url = "https://$name.tumblr.com/rss"
            val content = khttp.get(url, headers = mapOf("User-Agent" to "curl/7.37.0")).text
            val doc = builder.parse(content.toByteArray(Charsets.UTF_8).inputStream())
            val feed = SyndFeedInput().build(doc)
            System.out.println(feed.title)
             val tags = feed.entries.flatMap { e ->
                 if (e is SyndEntry) {
                     e.categories.map { c ->
                         if (c is SyndCategory) {
                             c.name
                         } else {
                             null
                         }
                     }.filterNotNull()
                 } else {
                     listOf()
                 }
             }
            mapOf(
                "title" to feed.title,
                "description" to feed.description,
                "url" to url,
                "tags" to tags
            )
        } catch (e: Throwable) {
            null
        }
    }
}
