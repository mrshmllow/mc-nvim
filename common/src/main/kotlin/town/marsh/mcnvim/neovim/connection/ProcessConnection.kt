package town.marsh.mcnvim.neovim.connection

import java.io.InputStream
import java.io.OutputStream
import java.lang.ProcessBuilder

class ProcessConnection(vararg command: String): Connection {
    private val pb = ProcessBuilder(command.asList())
    private val process: Process = pb.start();

    override fun getOutputStream(): OutputStream {
        return process.outputStream
    }

    override fun getInputStream(): InputStream {
        return process.inputStream
    }

    override fun close() {
        process.destroy()
    }
}