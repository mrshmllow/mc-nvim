package town.marsh.mcnvim.neovim

import org.msgpack.value.ArrayValue
import town.marsh.mcnvim.Neovim
import java.net.Socket

class Client(socket: Socket): Session(socket) {
    fun feedKeys(keys: String, mode: String, escape_ks: Boolean) {
        createCall("nvim_feedkeys") {
            packer.packArrayHeader(3)
            packer.packString(keys)
            packer.packString(mode)
            packer.packBoolean(escape_ks)
        }
    }

    fun input(keys: String) {
        createCall("nvim_input") {
            packer.packArrayHeader(1)
            packer.packString(keys)
        };
    }

    fun feedIntChar(i: Int) {
        Neovim.LOGGER.info(i.toString());
        val str: String = when (i) {
            256 -> "<Escape>"
            259 -> "<BS>"
            else -> ""
        }

        input(str)
    }

    fun getCurrentLine(): String? {
        return createCall("nvim_get_current_line").asStringValue().asString()
    }

    fun setCurrentLine(string: String) {
        createCall("nvim_set_current_line") {
            packer.packArrayHeader(1)
            packer.packString(string)
        }
    }

    fun winGetCursor(window: Int): ArrayValue? {
        return createCall("nvim_win_get_cursor") {
            packer.packArrayHeader(1)
            packer.packInt(window)
        }.asArrayValue()
    }

    fun winSetCursor(window: Int, column: Int) {
        createCall("nvim_win_set_cursor") {
            packer.packArrayHeader(2)
            packer.packInt(window)

            packer.packArrayHeader(2)
            packer.packInt(1)
            packer.packInt(column)
        }
    }

    fun bufGetMark(buf: Int, mark: String): ArrayValue? {
        val result = createCall("nvim_buf_get_mark") {
            packer.packArrayHeader(2)
            packer.packInt(buf)
            packer.packString(mark)
        }

        if (result.isNilValue) {
            return null
        }

        return result.asArrayValue()
    }

    fun getMode(): String {
        // this is jank. mode is the first key/value, so skip the key and get the mode value
        return createCall("nvim_get_mode").asMapValue().keyValueArray[1].asStringValue().toString()
    }
}