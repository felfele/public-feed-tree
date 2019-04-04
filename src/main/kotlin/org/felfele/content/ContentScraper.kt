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





object ContentScraper{
    val json = ObjectMapper()
    @JvmStatic
    fun main(args:Array<String>){
        val factory = DocumentBuilderFactory.newInstance()
        factory.isValidating = false
        //factory.isIgnoringElementContentWhitespace = true
        val builder = factory.newDocumentBuilder()


//        val content =ClassLoader.getSystemResourceAsStream("rss.xml")
//        val doc = builder.parse(content.toByteArray(Charsets.UTF_8).inputStream())
//        val feed = SyndFeedInput().build(doc)
//        System.out.println(feed.getTitle())
//
//        System.exit(0)



        //val blogs = (json.readValue(ClassLoader.getSystemResourceAsStream("tumblrs.json"),List::class.java) as List<Map<String,Any?>>)
        val blogs = (json.readValue(ClassLoader.getSystemResourceAsStream("loggedoutpicks.json"),List::class.java) as List<List<Map<String,Any?>>>).flatten()

        val taggedBlogs = blogs.map{ b->
         try {
             val url = b["url"].toString()
             val content = khttp.get("$url/rss", headers = mapOf("User-Agent" to "curl/7.37.0")).text
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
             b to tags
         }catch(e:Throwable){
             b to null
         }
        }

        json.writeValue(File("/Users/gmora/felfele/taggedloggedoutpicks.json"),taggedBlogs)

    }
}
