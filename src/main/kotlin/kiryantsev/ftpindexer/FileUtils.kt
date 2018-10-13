package kiryantsev.ftpindexer

import javafx.stage.FileChooser
import javafx.stage.Stage
import org.apache.commons.net.ftp.FTPFile
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat

class FileUtils(var fileName: String, var filetype: String = FILE_TYPE.TEXT_FILE ){


    object FILE_TYPE {
        val TEXT_FILE = "txt"
        val TABLE_FILE = "csv"
    }

    fun writeString(str: String){
        val out = PrintWriter(BufferedWriter(FileWriter("$fileName.$filetype", true)))
        out.println(str)
        out.close()
    }

    fun writeFileInfo(fullPath: String,file: FTPFile?){
        if (file != null) {
            if(filetype.equals(FILE_TYPE.TEXT_FILE)){
                writeString( "\n" + fullPath + "/" + file.name  +
                        " \n # size:" + file.size + " ; created: " +
                        SimpleDateFormat("dd.MM.yyyy hh:mm").format(file.timestamp.time) )
            }else if(filetype.equals(FILE_TYPE.TABLE_FILE)){
                writeString("\n" + file.name + ";" + file.size + ";" + SimpleDateFormat("dd.MM.yyyy hh:mm").format(file.timestamp.time) + ";" + fullPath)
            }
        }
    }


    fun writeListToFile(listOfFiles: MutableList<FTPFile>){
        listOfFiles.forEach {
            writeFileInfo(it.link,it)
        }
    }


    fun loadListIPFromFile():List<String>{
        var chooser = FileChooser()
        chooser.title = "Choose file with ip's"
        var selectedFile = chooser.showOpenDialog(Stage())
        return selectedFile.readText().split(";")
    }

}


