package kiryantsev.ftpindexer

import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPReply

class FTPController(var IP: String, var PORT: String) {


    var ftp = FTPClient()

    init {
        ftp.connect(IP, PORT.toInt())
        if (!FTPReply.isPositiveCompletion(ftp.replyCode)) {
            ftp.disconnect()
            throw Exception("Exception in connecting to FTP Server")
        }
        ftp.login("anonymous", "anonymous")
        ftp.controlEncoding = "UTF-8"
    }

    fun scan(): MutableList<FTPFile> {
        var result = SearchUtils(ftp, ftp.listFiles().toMutableList()).searchRecursive()
        ftp.disconnect()
        return result
    }


}