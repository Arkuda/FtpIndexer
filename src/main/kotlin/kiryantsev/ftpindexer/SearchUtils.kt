package kiryantsev.ftpindexer

import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile

class SearchUtils(var ftp: FTPClient, var listOfFiles: MutableList<FTPFile>) {

    var filter : MutableList<String> = mutableListOf()
    var indexOfFiles: MutableList<FTPFile> = mutableListOf()


    fun getIntrestStuff():List<FTPFile>{
        var result : MutableList<FTPFile> = mutableListOf()
        listOfFiles.forEach {
            var currPath = it
            var extension = it.name.split(".").last().toLowerCase()
            filter.forEach {
                if(extension.toLowerCase() == it){
                    result.add(currPath)
                }
            }
        }
        return result
    }

    fun searchRecursive(dir: String = "", foldersToScan: MutableList<FTPFile> = listOfFiles): MutableList<FTPFile> {
        foldersToScan.forEach {
            it.link = dir
            if (it.isDirectory){
                println("$dir/"+it.name)
                searchRecursive(dir+"/"+it.name,ftp.listFiles(dir+"/"+it.name).toMutableList())
            }else{
                if(Main.isFullLog)
                    println("$dir/"+it.name)
                indexOfFiles.add(it)
            }
        }
        return indexOfFiles
    }

    fun loadDefaultFilter(){
        filter.add("jpg") //img
        filter.add("jpeg") //img
        filter.add("cpp") //src
        filter.add("java") //src
        filter.add("js") //src
        filter.add("png") //img
        filter.add("exe") //exec
        filter.add("zip") //arc
        filter.add("rar") //arc
        filter.add("avi") //video
        filter.add("wmv") //video
    }
}