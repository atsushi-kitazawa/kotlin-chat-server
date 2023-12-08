import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.Scanner
import kotlin.concurrent.thread

fun main() {
    ChatServer().init()
}

class ChatServer {
    val PORT: Int = 8000
    var clientList = mutableListOf<Socket>()
    var roomList = mutableListOf<Room>()

    fun init() {
        val server = ServerSocket(PORT)
        while (true) {
            val client = server.accept()
            addClient(client)
            thread { ClientHandler(client).run() }
        }
    }

    private fun addClient(client: Socket) {
        clientList.add(client)
    }

    private fun initRoom() {
        roomList.add(Room("room1", mutableListOf<Member>()))
        roomList.add(Room("room2", mutableListOf<Member>()))
    }

    fun addRoom() {}
}

class Room(val name: String, var members: MutableList<Member>) {}

class Member(val name: String, val connection: Socket) {}

enum class Command {
    login,
    logout,
    join,
    leave
}

class ClientHandler(val client: Socket) {
    fun run() {
        println("accept ${client}")
        thread { MessageHandler(client).run() }
    }
}

class MessageHandler(val client: Socket) {
    private val reader: Scanner = Scanner(client.getInputStream())
    private val writer: OutputStream = client.getOutputStream()
    private var running = true
    fun run() {
        while (running) {
            val msg = reader.nextLine()
            if (msg == Command.leave.name) {
                leave()
                continue
            }
            if (msg.startsWith(Command.join.name, true)) {
                val roomName = msg.substring(Command.join.name.length + 1)

            }

            println(msg)
        }
    }

    private fun leave() {
        running = false
        client.close()
        println("leave ${client}")
    }
}