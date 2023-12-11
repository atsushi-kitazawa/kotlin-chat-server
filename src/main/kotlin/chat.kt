import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.Scanner
import kotlin.concurrent.thread

val server = ChatServer()
fun main() {
    server.init()
}

class ChatServer {
    private val PORT: Int = 8000
    private var roomList = mutableListOf<Room>()
    private var clientList = mutableListOf<Client>()

    fun init() {
        val server = ServerSocket(PORT)
        initRoom()
        while (true) {
            val clientSocket = server.accept()
            thread { ClientHandler(clientSocket).run() }
        }
    }

    fun login(client: Client) {
        if (isLogin(client)) {
            println("client is already logged in")
            return
        }
        clientList.add(client)
        println("login client=${client}")
        println("DEBUG: clientList=${clientList}")
    }

    fun logout(client: Client) {
        if (!isLogin(client)) {
            println("client is not logged in")
            return
        }
        clientList.remove(client)
        println("logout client=${client}")
        println("DEBUG: clientList=${clientList}")
    }

    fun isLogin(client: Client): Boolean {
        return clientList.contains(client)
    }

    fun addRoom(room: Room) {
        roomList.add(room)
    }

    fun removeRoom(room: Room) {}

    fun joinedRoom(client: Client, roomName: String) {
        var room = isExistedRoom(roomName)
        if (room != null) {
            room.join(client)
        }
    }

    fun leavedRoom(client: Client, roomName: String) {}

    fun isExistedRoom(name: String): Room? {
        for (n in roomList) {
            if (n.name == name) {
                return n
            }
        }
        return null
    }

    private fun initRoom() {
        addRoom(Room("room1"))
        addRoom(Room("room2"))
    }
}

data class Room(var name: String) {
    var joinedClientList = mutableListOf<Client>()

    fun rename(_name: String) {
        name = _name
    }

    fun join(client: Client) {
        joinedClientList.add(client)
        println("joined room=${name} client=${client}")
    }

    fun leave(client: Client) {
        joinedClientList.remove(client)
        println("leaved room=${name} client=${client}")
    }
}

data class Client(var name: String, val connection: Socket) {
    var joinedRoomList = mutableListOf<Room>()

    fun remave(_name: String) {
        name = _name
    }

    fun joinRoom(room: Room) {
        joinedRoomList.add(room)
    }
}

enum class Command {
    login,
    logout,
    join,
    leave
}

class ClientHandler(val cSocket: Socket) {
    fun run() {
        println("accept ${cSocket}")
        thread { MessageHandler(cSocket).run() }
    }
}

class MessageHandler(val cSocket: Socket) {
    private val reader: Scanner = Scanner(cSocket.getInputStream())
    private val writer: OutputStream = cSocket.getOutputStream()
    private var running = true
    private var client: Client = Client("", cSocket)
    fun run() {
        while (running) {
            val msg = reader.nextLine()
            if (msg.startsWith(Command.login.name, true)) {
                val name = msg.substring(Command.join.name.length + 1)
                client.remave(name)
                server.login(client)
                continue
            }
            if (msg == Command.logout.name) {
                server.logout(client)
                continue
            }
            if (msg == Command.leave.name) {
                leave()
                continue
            }
            if (msg.startsWith(Command.join.name, true)) {
                val roomName = msg.substring(Command.join.name.length + 1)
                server.joinedRoom(client, roomName)
                continue
            }

            println(msg)
        }
    }

    private fun leave() {
        running = false
        cSocket.close()
    }
}