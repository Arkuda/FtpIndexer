import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.PrintWriter

/*
Created: Andrey Kiryantsev - reyzor2142@gmail.com, Jun 2018
 */


fun main(args: Array<String>) {

    var ftp = FTPClient()

    if(args.isEmpty()){
        println("Need ip in args")
        return
    }
    var ipAddr : String = args[0]

    ftp.connect(ipAddr, 21)
    System.out.println("FTP URL is:" + ftp.defaultPort)
    if (!FTPReply.isPositiveCompletion(ftp.replyCode)) {
        ftp.disconnect()
        throw Exception("Exception in connecting to FTP Server")
    }
    ftp.login("anonymous", "anonymous")
    ftp.controlEncoding = "UTF-8"
    index(ftp, "/", ipAddr)
}


fun index(ftp: FTPClient, dir : String, ipAddr: String){
    var listDirAndFiles = ftp.listFiles(dir)
    for (element in listDirAndFiles){
        if(element.isDirectory){
            index(ftp, dir + "/" + element.name, ipAddr)
        }else if(element.isFile){
            println("Founded: " + dir + element.name)
            write(ipAddr,dir + element.name)
        }
    }
}


fun write(ipAddr: String, str : String){
    val out = PrintWriter(BufferedWriter(FileWriter("$ipAddr.txt", true)))
    out.println( "\n" + str)
    out.close()
}