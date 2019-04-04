package org.felfele.content

import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.syndication.feed.synd.SyndCategory
import com.sun.syndication.feed.synd.SyndEntry
import com.sun.syndication.io.SyndFeedInput
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory


object ContentScraperSimple {

    val json = ObjectMapper()
    val factory = DocumentBuilderFactory.newInstance().apply {
        this.isValidating = false
    }

    //factory.isIgnoringElementContentWhitespace = true
    val builder = factory.newDocumentBuilder()

    @JvmStatic
    fun main(args: Array<String>) {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isValidating = false
        //factory.isIgnoringElementContentWhitespace = true
        val builder = factory.newDocumentBuilder()

        val usenet = (json.readValue(ClassLoader.getSystemResourceAsStream("usenetMap.json"), Map::class.java) as Map<String, Any>)

        val usenetTumblr = usenet.map { m ->
            m.key to replaceLeafs(m.toPair())
        }.toMap().filterValues { it != null }

        json.writeValue(File("/Users/gmora/felfele/usenetTumblrTree.json"), usenetTumblr)

//
////        val content =ClassLoader.getSystemResourceAsStream("rss.xml")
////        val doc = builder.parse(content.toByteArray(Charsets.UTF_8).inputStream())
////        val feed = SyndFeedInput().build(doc)
////        System.out.println(feed.getTitle())
////
////        System.exit(0)
//
//
//
//        //val blogs = (json.readValue(ClassLoader.getSystemResourceAsStream("tumblrs.json"),List::class.java) as List<Map<String,Any?>>)
//        val blogs = (json.readValue(ClassLoader.getSystemResourceAsStream("loggedoutpicks.json"),List::class.java) as List<List<Map<String,Any?>>>).flatten()
//
//        val taggedBlogs = blogs.map{ b->
//         try {
//             val url = b["url"].toString()
//             val content = khttp.get("$url/rss", headers = mapOf("User-Agent" to "curl/7.37.0")).text
//             val doc = builder.parse(content.toByteArray(Charsets.UTF_8).inputStream())
//             val feed = SyndFeedInput().build(doc)
//             System.out.println(feed.title)
//             val tags = feed.entries.flatMap { e ->
//                 if (e is SyndEntry) {
//                     e.categories.map { c ->
//                         if (c is SyndCategory) {
//                             c.name
//                         } else {
//                             null
//                         }
//                     }.filterNotNull()
//                 } else {
//                     listOf()
//                 }
//             }
//             b to tags
//         }catch(e:Throwable){
//             b to null
//         }
//        }
//
//        json.writeValue(File("/Users/gmora/felfele/taggedloggedoutpicks.json"),taggedBlogs)
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
