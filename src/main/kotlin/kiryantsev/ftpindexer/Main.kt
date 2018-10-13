package kiryantsev.ftpindexer

import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply


/*
Created: Andrey Kiryantsev - reyzor2142@gmail.com, Jun 2018
 */

class Main{
    companion object {

        var IP = ""
        var PORT = ""
        var MODE = ""
        var FILEPATH = ""
        var isFullLog = false


        @JvmStatic
        fun main(args: Array<String>) {
            if(args.isEmpty()){
                println("Need arguments, help:")
                println("-ip=X     To set ip of server to index")
                println("-p=X      To set port of server")
                println("-o=tbl  Exit file to be csv table")
                println("-o=txt  Exit file to be txt, default method")
                println("-l=X    File with ip's")
                println("-d      Full log")
            }else {

                //Parse parameters
                args.forEach {
                    if(it.contains("-ip")){
                        IP = it.split("=")[1]
                    }
                    if(it.contains("-l")){
                        FILEPATH = it.split("=")[1]
                    }
                    if(it.contains("-p")){
                        PORT = it.split("=")[1]
                    }
                    if(it.contains("-o")){
                        if(it.split("=")[1].contains("tbl")){
                            MODE = "csv"
                        }else{
                            MODE = "txt"
                        }
                    }
                    if (it.contains("-d")){
                        isFullLog = true
                    }
                }

                //Check def params
                if (IP == "" && FILEPATH == "")
                    throw Exception("Ip and File are not specified")


                //Set defaults
                if (PORT == "")
                    PORT = "21"

                if (MODE == "")
                    MODE = "txt"

                if (IP != ""){
                    // start load
                    println("Start index $IP")
                    var ftp = FTPController(IP, PORT)
                    var res = ftp.scan()
                    var searcher = SearchUtils(ftp.ftp,res)
                    searcher.loadDefaultFilter()
                    var intresStuff = searcher.getIntrestStuff()

                    var fu = FileUtils(IP,MODE)
                    fu.writeString("INTREST STUFF")
                    fu.writeListToFile(intresStuff.toMutableList())
                    fu.writeString("\nOTHER")
                    fu.writeListToFile(res)
                    println("Done for $IP")


                }else{
                    // load ips from file
                    var listOfIPS = FileUtils(FILEPATH,FileUtils.FILE_TYPE.TEXT_FILE).loadListIPFromFile()

                    listOfIPS.forEach {
                        println("Start index $it")
                        val ftp = FTPController(it, PORT)
                        val res = ftp.scan()
                        val searcher = SearchUtils(ftp.ftp,res)
                        searcher.loadDefaultFilter()
                        val intresStuff = searcher.getIntrestStuff()

                        val fu = FileUtils(it,MODE)
                        fu.writeString("INTREST STUFF")
                        fu.writeListToFile(intresStuff.toMutableList())
                        fu.writeString("\nOTHER")
                        fu.writeListToFile(res)
                        println("Done for $it")
                    }


                }

            }
        }
    }

}





/*
*
* package kiryantsev.FtpIndexer

import javafx.fxml.FXML
import javafx.scene.control.*
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.PrintWriter
import javafx.scene.control.TreeItem
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.apache.commons.net.ftp.FTPFile
import java.text.SimpleDateFormat


class MainGuiController{

    @FXML
    lateinit  var ipaddrInput : TextField

    @FXML
    lateinit  var ipPort : TextField

    @FXML
    lateinit  var outputTextArea : TextArea

    @FXML
    lateinit var treeView: TreeView<String>

    @FXML
    lateinit var progressBar : ProgressBar




    fun runIndexWithDefault(){  //because JavaFX stupid and cant call fun with default arguments, and to run in background thread
//        Thread(Runnable {
            runIndex( needDrawTree = false)
            progressBar.progress = 100.0
//        })
    }

    fun runIndex(ipAddr : String = ipaddrInput.text, ipPortint : Int = ipPort.text.toInt(), needDrawTree : Boolean = true){

        progressBar.progress = 0.0
        var ftp = FTPClient()

        if(ipAddr.isEmpty()){
            Alert(Alert.AlertType.ERROR,"Need to set ip first", ButtonType.OK).show()
            return
        }

        ftp.connect(ipAddr, ipPortint)
        if (!FTPReply.isPositiveCompletion(ftp.replyCode)) {
            ftp.disconnect()
            progressBar.progress = 0.0
            Alert(Alert.AlertType.ERROR, "Exception in connecting to FTP Server $ipAddr:$ipPortint", ButtonType.OK).show()
            throw Exception("Exception in connecting to FTP Server")
        }
        ftp.login("anonymous", "anonymous")
        ftp.controlEncoding = "UTF-8"

        if(needDrawTree) treeView.root = TreeItem("/")

        var intrestedStuff = index(ftp, "/", ipAddr,needDrawTree)

        if(intrestedStuff.isNotEmpty()){
            write(ipAddr,"\n \n INTRESTED STUFF \n \n", null)
            intrestedStuff.forEach {
                write(ipAddr,it.link,it)
            }
        }
    }



    fun index(ftp : FTPClient, dir : String, ipAddr : String, needDrawTree : Boolean): List<FTPFile> {
        var listOfFiles : MutableList<FTPFile> = mutableListOf()

        var listDirAndFiles = ftp.listFiles(dir)
        for (element in listDirAndFiles) {
            if (element.isDirectory) {
                listOfFiles.addAll( index(ftp, dir + "/" + element.name, ipAddr,needDrawTree))
            } else if (element.isFile) {
                outputTextArea.appendText("\n" + "Founded: " + dir + "/" + element.name)
                write(ipAddr,  dir , element)
                element.link = dir
                listOfFiles.add(element)
                if(needDrawTree) pathFinder(dir,element.name)
            }
        }
        return filterIntrestedStuff(listOfFiles)
    }


    fun write(ipAddr: String, str: String, element: FTPFile?){
        val out = PrintWriter(BufferedWriter(FileWriter("$ipAddr.txt", true)))
        if(element == null){
            out.println(str)
        }else{
            out.println( "\n" + str + "/" + element.name  +
                    " \n ###### size:" + element.size + " ; created: " +
                    SimpleDateFormat("dd.MM.yyyy hh:mm").format(element.timestamp.time) )
        }

        out.close()
    }


    fun pathFinder(path: String, item: String){
        var pathArr  = path.split("/").filterIndexed { _, s -> s != "" }
        var rootEml: TreeItem<String> = treeView.root
        var elmFinded : TreeItem<String>? = null
        for (pathElm in pathArr){
            rootEml.children.forEach { treeItem ->
                    if (treeItem.value == pathElm) {
                        elmFinded = treeItem
                    }
            }
            rootEml = if(elmFinded == null){
                val elm  = TreeItem(pathElm)
                rootEml.children.add(elm)
                elm
            }else{
                elmFinded!!
            }

        }
        rootEml.children.add(TreeItem(item))
    }

    fun indexArrayFromFile(){
        progressBar.progress = 0.0

        var chooser = FileChooser()
        chooser.title = "Choose file with ip's"
        var selectedFile = chooser.showOpenDialog(Stage())

            var arrayOfIps = selectedFile.readText().split(";")
            var indexed = 0
            for (addr : String in arrayOfIps){
                var fulladr = addr.split(":")
                if(fulladr.size == 1){
                    //run with default port
                    runIndex(fulladr[0],needDrawTree = false)
                }else{
                    //run with custom port
                    runIndex(fulladr[0],fulladr[1].toInt(),false)
                }
                indexed++
                progressBar.progress = ((indexed/arrayOfIps.size) * 100.0)
            }
    }


    fun filterIntrestedStuff(elms : List<FTPFile>): List<FTPFile> {
        var filter : MutableList<String> = mutableListOf()
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
        return simpleExtFilter(elms, filter)
    }

    fun simpleExtFilter(elms: List<FTPFile>, needExtension : List<String>) : List<FTPFile>{
        var result : MutableList<FTPFile> = mutableListOf()
        elms.forEach {
            var currPath = it
            var extension = it.name.split(".").last().toLowerCase()
            needExtension.forEach {
                if(extension.toLowerCase().toString().equals(it.toString())){
                    result.add(currPath)
                }
            }
        }
        return result
    }
}

*
* */