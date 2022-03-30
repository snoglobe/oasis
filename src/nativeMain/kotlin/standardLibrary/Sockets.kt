package standardLibrary

import KotlinFunction0
import KotlinFunction1
import OasisPrototype
import kotlinx.cinterop.*
import platform.linux.inet_ntoa
import platform.posix.*

fun constructSocket(port: Double): OasisPrototype {
    val socket = OasisPrototype(base, -1)
    socket.set("port", port)
    socket.set("accepting", true)
    socket.set("__socketFd", socket(AF_INET, SOCK_STREAM, 0).also {
        if(it == 0) throw Exception("Failed to open socket: $errno")
    })
    val sockaddrIn: CPointer<sockaddr_in> = malloc(sizeOf<sockaddr_in>().convert())!!.reinterpret()
    sockaddrIn.pointed.apply {
        sin_family = AF_INET.convert()
        sin_port = htons(port.toInt().convert())
        sin_addr.s_addr = INADDR_ANY
    }
    socket.set("__socketAddr", sockaddrIn)
    bind(socket.get("__socketFd") as Int, sockaddrIn.reinterpret(), sockaddr_in.size.convert()).also {
        if(it == -1) {
            println("addr: ${inet_ntoa(sockaddrIn.pointed.sin_addr.readValue())!!.toKString()} family: ${sockaddrIn.pointed.sin_family} port: ${sockaddrIn.pointed.sin_port}")
            throw Exception("Failed to bind socket: $errno")
        }
    }
    listen(socket.get("__socketFd") as Int, 50).also {
        if(it == -1) throw Exception("Failed to accept on socket: $errno")
    }
    socket.set("waitForConnection", KotlinFunction0 {
        val connection = OasisPrototype(base, -1)
        connection.set("__fd", accept(socket.get("__socketFd") as Int, null, null))
        connection.set("address", inet_ntoa(sockaddrIn.pointed.sin_addr.readValue())!!.toKString())
        connection.set("send", KotlinFunction1<Unit, String> { x -> send(connection.get("__fd") as Int, x.cstr, x.cstr.size.toULong().convert(), 0)})
        connection.set("bsend", KotlinFunction1<Unit, ByteArray> { x -> send(connection.get("__fd") as Int, x.refTo(0), x.size.toULong().convert(), 0)})
        connection.set("read", KotlinFunction0 {
            val buf = ByteArray(1024)
            read(connection.get("__fd") as Int, buf.refTo(0), 1024)
            buf
        })
        connection.set("sread", KotlinFunction0 {
            val buf = ByteArray(1024)
            read(connection.get("__fd") as Int, buf.refTo(0), 1024)
            buf.toKString()
        })
        connection.set("close", KotlinFunction0 { close(connection.get("__fd") as Int) })
        connection
    })
    socket.set("close", KotlinFunction0 {
        close(socket.get("__socketFd") as Int)
        free(sockaddrIn)
    })
    return socket
}