package ir.erfansn.nsmavpn.feature.home.vpn.protocol.terminal

import android.os.ParcelFileDescriptor
import ir.erfansn.nsmavpn.feature.home.vpn.protocol._DNS_CUSTOM_ADDRESS
import ir.erfansn.nsmavpn.feature.home.vpn.protocol._ROUTE_ALLOWED_APPS
import ir.erfansn.nsmavpn.feature.home.vpn.protocol._ROUTE_CUSTOM_ROUTES
import ir.erfansn.nsmavpn.feature.home.vpn.protocol._ROUTE_DO_ADD_CUSTOM_ROUTES
import ir.erfansn.nsmavpn.feature.home.vpn.protocol._ROUTE_DO_ADD_DEFAULT_ROUTE
import ir.erfansn.nsmavpn.feature.home.vpn.protocol._ROUTE_DO_ENABLE_APP_BASED_RULE
import ir.erfansn.nsmavpn.feature.home.vpn.protocol._ROUTE_DO_ROUTE_PRIVATE_ADDRESSES
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ClientBridge
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ControlMessage
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.Result
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.Where
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension.isSame
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.extension.toHexByteArray
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetAddress
import java.nio.ByteBuffer

class IpTerminal(private val bridge: ClientBridge) {
    private var fd: ParcelFileDescriptor? = null

    private lateinit var inputStream: FileInputStream
    private lateinit var outputStream: FileOutputStream

    private val isAppBasedRuleEnabled = _ROUTE_DO_ENABLE_APP_BASED_RULE
    private val isDefaultRouteAdded = _ROUTE_DO_ADD_DEFAULT_ROUTE
    private val isPrivateAddressesRouted = _ROUTE_DO_ROUTE_PRIVATE_ADDRESSES
    private val isCustomRoutesAdded = _ROUTE_DO_ADD_CUSTOM_ROUTES

    suspend fun initializeTun(): Boolean {
        if (bridge.PPP_IPv4_ENABLED) {
            if (bridge.currentIPv4.isSame(ByteArray(4))) {
                bridge.controlMailbox.send(ControlMessage(Where.IPv4, Result.ERR_INVALID_ADDRESS))
                return false
            }

            InetAddress.getByAddress(bridge.currentIPv4).also {
                bridge.builder.addAddress(it, 32)
            }

            if (bridge.DNS_DO_USE_CUSTOM_SERVER) {
                bridge.builder.addDnsServer(_DNS_CUSTOM_ADDRESS)
            }

            if (!bridge.currentProposedDNS.isSame(ByteArray(4))) {
                InetAddress.getByAddress(bridge.currentProposedDNS).also {
                    bridge.builder.addDnsServer(it)
                }
            }

            setIPv4BasedRouteing()
        }

        if (bridge.PPP_IPv6_ENABLED) {
            if (bridge.currentIPv6.isSame(ByteArray(8))) {
                bridge.controlMailbox.send(ControlMessage(Where.IPv6, Result.ERR_INVALID_ADDRESS))
                return false
            }

            ByteArray(16).also { // for link local addresses
                "FE80".toHexByteArray().copyInto(it)
                ByteArray(6).copyInto(it, destinationOffset = 2)
                bridge.currentIPv6.copyInto(it, destinationOffset = 8)
                bridge.builder.addAddress(InetAddress.getByAddress(it), 64)
            }

            setIPv6BasedRouteing()
        }

        if (isCustomRoutesAdded) {
            addCustomRoutes()
        }

        if (isAppBasedRuleEnabled) {
            addAppBasedRules()
        }

        bridge.builder.setMtu(bridge.PPP_MTU)
        bridge.builder.setBlocking(true)

        fd = bridge.builder.establish()!!.also {
            inputStream = FileInputStream(it.fileDescriptor)
            outputStream = FileOutputStream(it.fileDescriptor)
        }

        return true
    }

    private fun setIPv4BasedRouteing() {
        if (isDefaultRouteAdded) {
            bridge.builder.addRoute("0.0.0.0", 0)
        }

        if (isPrivateAddressesRouted) {
            bridge.builder.addRoute("10.0.0.0", 8)
            bridge.builder.addRoute("172.16.0.0", 12)
            bridge.builder.addRoute("192.168.0.0", 16)
        }
    }

    private fun setIPv6BasedRouteing() {
        if (isDefaultRouteAdded) {
            bridge.builder.addRoute("::", 0)
        }

        if (isPrivateAddressesRouted) {
            bridge.builder.addRoute("fc00::", 7)
        }
    }

    private fun addAppBasedRules() {
        _ROUTE_ALLOWED_APPS.forEach {
            bridge.builder.addAllowedApplication(it)
        }
    }

    private suspend fun addCustomRoutes(): Boolean {
        _ROUTE_CUSTOM_ROUTES.split("\n").filter { it.isNotEmpty() }.forEach {
            val parsed = it.split("/")
            if (parsed.size != 2) {
                bridge.controlMailbox.send(ControlMessage(Where.ROUTE, Result.ERR_PARSING_FAILED))
                return false
            }

            val address = parsed[0]
            val prefix = parsed[1].toIntOrNull()
            if (prefix == null){
                bridge.controlMailbox.send(ControlMessage(Where.ROUTE, Result.ERR_PARSING_FAILED))
                return false
            }

            try {
                bridge.builder.addRoute(address, prefix)
            } catch (_: IllegalArgumentException) {
                bridge.controlMailbox.send(ControlMessage(Where.ROUTE, Result.ERR_PARSING_FAILED))
                return false
            }
        }

        return true
    }

    fun writePacket(start: Int, size: Int, buffer: ByteBuffer) {
        // the position won't be changed
        outputStream.write(buffer.array(), start, size)
    }

    fun readPacket(buffer: ByteBuffer) {
        buffer.clear()
        buffer.position(inputStream.read(buffer.array(), 0, bridge.PPP_MTU))
        buffer.flip()
    }

    fun close() {
        fd?.close()
    }
}
