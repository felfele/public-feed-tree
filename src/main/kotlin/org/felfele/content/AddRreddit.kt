package org.felfele.content

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File


object AddRreddit {
    val json = ObjectMapper()
    val prettyJson = json.writerWithDefaultPrettyPrinter()


    @JvmStatic
    fun main(args: Array<String>) {

        val usenetMap = (json.readValue(ClassLoader.getSystemResourceAsStream("usenetMap.json"),Map::class.java) as Map<String,Map<String,List<String>>>)
        val inputTree = (json.readValue(ClassLoader.getSystemResourceAsStream("threeLevelTree.json"),Map::class.java) as Map<String,Map<String,List<Blog>>>)

        val outputTree = addReddit(usenetMap, inputTree)


        prettyJson.writeValue(File("usenetTumblrReddit.json"), outputTree)
    }

     fun addReddit(usenetMap: Map<String, Map<String, List<String>>>, inputTree: Map<String, Map<String, List<Blog>>>): Map<String, Map<String, List<Blog>>> {
        val reddits = listOf(
            "popular0-100.json",
            "popular100-200.json",
            "popular200-300.json"

        )

        val redditBlogs = reddits.flatMap{ f->
            val reddit = (json.readValue(File("reddit/popular/$f"),Map::class.java) as Map<String,Any>)
            ((reddit["data"] as Map<String,Any>)["children"] as List<Map<String,Any>>).map{ b->
                val blog = b["data"] as Map<String, Any>
                val subreddit = blog["url"].toString().trim('/')
                Blog(
                    url = "https:///www.reddit.com/$subreddit.rss",
                    description = blog["public_description"].toString(),
                    title = blog["title"].toString(),
                    tags = setOf(blog["advertiser_category"].toString().trim().toLowerCase(), subreddit.removePrefix("r/").toLowerCase()).toList()
                )
            }

        }

        return usenetMap.map { c->
            if (c.value is Map<String, Any>){
                var hasSameSub = false
                val ret = c.value.map { s ->
                    if (s.key == c.key){
                        hasSameSub = true
                    }
                    if (s.value is List<Any>) {
                        val topics = if (s.value.isEmpty()) {
                            listOf(s.key)
                        } else {

                            s.value.map { it.toString() }
                        }

                        s.key to (findReddits(topics, redditBlogs) +  (inputTree[c.key]?.get(s.key)?: listOf()))
                    } else {
                        System.err.println("#2 ${c.value}")
                        null
                    }
                }.filterNotNull().toMap()


                c.key to if (!hasSameSub){
                    // add main category tumblrs and subreddits as subcategory (fixed depth)
                    ret + (c.key to (findReddits(listOf(c.key), redditBlogs) +  (inputTree[c.key]?.get(c.key)?: listOf())))

                }else{
                    ret
                }.filterValues { it.isNotEmpty() }
            }else{
                System.err.println(c.value)
                null
            }

        }.filterNotNull().filter { it.second !=null && (it.second?.isEmpty() == false)  }.toMap()

    }

    private fun findReddits(topics: List<String>, redditBlogs: List<Blog>): List<Blog> {
        val lowerTopics = topics.map{ it.toLowerCase().trim()}
        return redditBlogs.filter {r->
            r.tags?.filter { it !=null && it!="" }?.map { it.toString().toLowerCase().trim() }
                ?.toSet()?.intersect(lowerTopics)?.isEmpty() == false
        }
    }

}
