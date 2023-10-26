package town.marsh.mcnvim.neovim

import org.msgpack.core.MessagePack
import org.msgpack.core.MessagePacker
import org.msgpack.core.MessageUnpacker
import org.msgpack.value.ArrayValue
import org.msgpack.value.Value
import town.marsh.mcnvim.Neovim
import java.net.Socket
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread


open class Session(socket: Socket) {
    val packer: MessagePacker = MessagePack.newDefaultPacker(socket.getOutputStream())
    private val unpacker: MessageUnpacker = MessagePack.newDefaultUnpacker(socket.getInputStream())

    private val msgid = AtomicInteger(0)

    private val responseMap = ConcurrentHashMap<Int, BlockingQueue<Value>>()

    fun setupCall(name: String): Int {
        val msgid = msgid.incrementAndGet()
        packer.packArrayHeader(4)

        packer.packInt(0) // 0 = Request
            .packInt(msgid) // msgid
            .packString(name) // method

        return msgid
    }

    fun call(name: String, vararg params: Any): Value {
        val msgid = setupCall(name)

        packer.packArrayHeader(params.size)

        for (param in params) {
            packer.packValue(param as Value?)
        }

        return flushBlockAndTake(msgid)
    }

    fun createCall(method: String, createParams: () -> MessagePacker): Value {
        val msgid = setupCall(method)

        createParams();

        return flushBlockAndTake(msgid)
    }

    fun createCall(method: String): Value {
        val msgid = setupCall(method)
        packer.packArrayHeader(0)
        return flushBlockAndTake(msgid)
    }

    private fun flushBlockAndTake(msgid: Int): Value {
        val responseQueue = LinkedBlockingQueue<Value>();
        responseMap[msgid] = responseQueue;

        packer.flush();

        val response = responseQueue.take();

        return response;
    }

    private fun handleResponse(msgid: Int, error: Value, result: Value) {
        responseMap[msgid]?.put(result);
    }

    private fun handleNotification(method: String, asArrayValue: ArrayValue) {
        Neovim.LOGGER.info("Received notification {}: {}", method, asArrayValue)
    }

    init {
        thread(start = true) {
            Neovim.LOGGER.info("Entering Neovim Thread")

            while (unpacker.hasNext()) {
                val value = unpacker.unpackValue().asArrayValue();
                val type = value[0].asIntegerValue().asInt()

                if (type == 1) {
                    handleResponse(value[1].asIntegerValue().asInt(), value[2], value[3])
                } else if (type == 2) {
                    handleNotification(value[1].asStringValue().toString(), value[2].asArrayValue())
                }

//                Thread.sleep(1000)
            }

            Neovim.LOGGER.info("Leaving thread");
        }
    }
}