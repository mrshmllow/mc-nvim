package town.marsh.mcnvim

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import town.marsh.mcnvim.neovim.Client
import town.marsh.mcnvim.neovim.connection.ProcessConnection
import town.marsh.mcnvim.neovim.connection.SocketConnection

object Neovim {
    const val MOD_ID = "mcnvim"
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID);

//    private val connection = SocketConnection("127.0.0.1", 1234)
    private val connection = ProcessConnection("nvim", "--embed", "--headless")
    val client = Client(connection)

    fun init() {
    }

    fun close() {
        client.close()
        connection.close()
    }
}