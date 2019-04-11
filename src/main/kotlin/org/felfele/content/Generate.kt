package org.felfele.content

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File

object Generate{
    val json = ObjectMapper()
    val prettyJson = json.writerWithDefaultPrettyPrinter()

    @JvmStatic
    fun main(args:Array<String>){
        val usenetMap = (json.readValue(File("usenetMap.json"),Map::class.java) as Map<String,Map<String,List<String>>>)
        val tumblrTree= TumblrScraperSimple.addTumblr(usenetMap)
        val tumblrTreeFixedDepth = UsenetFixDepth.convertToFixedDepth(tumblrTree)
        val tumblrRedditTree = AddRreddit.addReddit(usenetMap, tumblrTreeFixedDepth)
        prettyJson.writeValue(File("usenetTumblrReddit.json"), tumblrRedditTree)
    }
}
