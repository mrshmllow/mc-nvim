package town.marsh.mcnvim.neovim

import org.msgpack.core.MessagePack
import org.msgpack.core.MessagePacker
import org.msgpack.core.MessageUnpacker
import org.msgpack.value.ArrayValue
import org.msgpack.value.Value
import town.marsh.mcnvim.Neovim
import town.marsh.mcnvim.neovim.connection.Connection
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread


open class Session(connection: Connection) {
    val packer: MessagePacker = MessagePack.newDefaultPacker(connection.getOutputStream())
    private val unpacker: MessageUnpacker = MessagePack.newDefaultUnpacker(connection.getInputStream())
    private val msgId = AtomicInteger(0)

    private val concurrentHashMap = ConcurrentHashMap<Int, CompletableFuture<Value>>()

//    private val executor = Executors.newCachedThreadPool();

    private val workerThread = thread(start = true) {
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

    private fun setupCall(name: String): Int {
        val msgId = msgId.incrementAndGet()
        packer.packArrayHeader(4)

        packer.packInt(0) // 0 = Request
            .packInt(msgId) // msgid
            .packString(name) // method

        return msgId
    }

//    fun call(name: String, vararg params: Any): CompletableFuture<Value> {
//        val msgId = setupCall(name)
//
//        packer.packArrayHeader(params.size)
//
//        for (param in params) {
//            packer.packValue(param as Value?)
//        }
//
//        return flushBlockAndTake(msgId)
//    }

    fun createCall(method: String, packParams: () -> MessagePacker): CompletableFuture<Value> {
        val msgId = setupCall(method)

        packParams();

        return createFuture(msgId)
    }

    fun createCall(method: String): CompletableFuture<Value> {
        val msgId = setupCall(method)
        packer.packArrayHeader(0)
        return createFuture(msgId)
    }

    private fun createFuture(msgId: Int): CompletableFuture<Value> {
        val futureResult = CompletableFuture<Value>()
        concurrentHashMap[msgId] = futureResult;

        packer.flush();

        return futureResult;
    }

    private fun handleResponse(msgId: Int, error: Value, result: Value) {
        concurrentHashMap[msgId]?.complete(result)
    }

    private fun handleNotification(method: String, asArrayValue: ArrayValue) {
        Neovim.LOGGER.info("Received notification {}: {}", method, asArrayValue)
    }

    fun close() {
        packer.close();
        unpacker.close();
    }
}