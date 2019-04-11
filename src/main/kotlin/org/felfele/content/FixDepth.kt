package org.felfele.content

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File

class Blog(
    var url:String?=null,
    var title:String?=null,
    var description:String?=null,
    var tags:List<String>?=null
)

object UsenetFixDepth {
    val json = ObjectMapper()
    val prettyJson = json.writerWithDefaultPrettyPrinter()

    @JvmStatic
    fun main(args: Array<String>) {
        val inputTree = (json.readValue(ClassLoader.getSystemResourceAsStream("usenetTumblrTree.json"),Map::class.java) as Map<String,Any>)



       val outputTree = convertToFixedDepth(inputTree)

        prettyJson.writeValue(File("threeLevelTree.json"), outputTree)
    }

     fun convertToFixedDepth(inputTree: Map<String, Any>): Map<String, Map<String, List<Blog>>> {
        return  inputTree.map{sc->
            sc.key to if (sc.value is Map<*,*>){
                (sc.value as Map<String, Map<String,Any>>).map{ bl->
                    bl.key to if (bl.value is List<*>){
                        val bm = (bl.value as List<Map<String,*>>)
                        bm.map{ b->
                            Blog(b["url"] as String,b["title"] as String,b["description"] as String, (b["tags"] as List<String>).toSortedSet().take(10))
                        }
                    }else{
                        if (bl.value is Map<*,*>){
                            val b = (bl.value as Map<String,*>)
                            listOf(Blog(b["url"] as String,b["title"] as String,b["description"] as String, (b["tags"] as List<String>).toSortedSet().take(10)))
                        }else {
                            System.err.println("#########2 ${bl.value}")
                            null
                        }
                    }
                }.toMap().filterValues { it != null && it.isNotEmpty() }
            }else{
                System.err.println("######### ${sc.value}")
                null
            }

        }.toMap().filterValues { it != null && it.isNotEmpty() } as Map<String, Map<String, List<Blog>>>
    }
}
