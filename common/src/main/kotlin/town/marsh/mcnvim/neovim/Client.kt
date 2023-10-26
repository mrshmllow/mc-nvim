package town.marsh.mcnvim.neovim

import org.msgpack.value.ArrayValue
import town.marsh.mcnvim.Neovim
import java.net.Socket
import java.util.concurrent.CompletableFuture

class Client(socket: Socket): Session(socket) {
    fun feedKeys(keys: String, mode: String, escapeKs: Boolean) {
        createCall("nvim_feedkeys") {
            packer.packArrayHeader(3)
            packer.packString(keys)
            packer.packString(mode)
            packer.packBoolean(escapeKs)
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

    fun getCurrentLine(): CompletableFuture<String> {
        return createCall("nvim_get_current_line").thenApply { result -> result.asStringValue().asString() };
    }

    fun setCurrentLine(string: String) {
        createCall("nvim_set_current_line") {
            packer.packArrayHeader(1)
            packer.packString(string)
        }
    }

    fun winGetCursor(window: Int = 0): CompletableFuture<ArrayValue> {
        return createCall("nvim_win_get_cursor") {
            packer.packArrayHeader(1)
            packer.packInt(window)
        }.thenApply { result -> result.asArrayValue() }
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

    fun bufGetMark(buf: Int, mark: String): CompletableFuture<ArrayValue> {
        return createCall("nvim_buf_get_mark") {
            packer.packArrayHeader(2)
            packer.packInt(buf)
            packer.packString(mark)
        }.thenApply { result ->
            if (result.isNilValue) return@thenApply null;

            result.asArrayValue()
        }
    }

    fun getMode(): CompletableFuture<String> {
        return createCall("nvim_get_mode").thenApply { result ->
            // this is jank. mode is the first key/value, so skip the key and get the mode value
            result.asMapValue().keyValueArray[1].asStringValue().toString()
        }
    }
}