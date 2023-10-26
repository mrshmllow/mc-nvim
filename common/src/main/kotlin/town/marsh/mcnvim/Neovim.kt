package town.marsh.mcnvim

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import town.marsh.mcnvim.neovim.Client
import java.net.Socket

object Neovim {
    const val MOD_ID = "mcnvim"
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID);

    private val socket = Socket("127.0.0.1", 1234)
    val client = Client(socket)

    fun init() {}
}