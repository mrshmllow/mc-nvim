package town.marsh.mcnvim.neovim.connection

import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

class SocketConnection(s: String, i: Int) : Socket(s, i), Connection

interface Connection {
    fun getOutputStream(): OutputStream;
    fun getInputStream(): InputStream;

    fun close()
}