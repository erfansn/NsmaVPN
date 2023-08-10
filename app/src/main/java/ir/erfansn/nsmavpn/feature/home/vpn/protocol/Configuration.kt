package ir.erfansn.nsmavpn.feature.home.vpn.protocol

import android.net.Uri

const val MAX_MRU = 2000
const val MAX_MTU = 2000
const val MIN_MRU = 68
const val MIN_MTU = 68
const val DEFAULT_MRU = 1500
const val DEFAULT_MTU = 1500
const val BUFFER_INCOMING = 16384
const val PPP_AUTH_TIMEOUT = 3
const val _HOME_HOSTNAME = "public-vpn.opengw.net"
const val _HOME_USERNAME = "vpn"
const val _HOME_PASSWORD = "vpn"
const val _PPP_MRU = 1500
const val _PPP_MTU = 1500
const val _PPP_PAP_ENABLED = true
const val _PPP_MSCHAPv2_ENABLED = true
const val _PPP_IPv4_ENABLED = true
const val _PPP_IPv6_ENABLED = false
const val _DNS_DO_REQUEST_ADDRESS = true
const val _DNS_DO_USE_CUSTOM_SERVER = false
const val _ROUTE_DO_ENABLE_APP_BASED_RULE = false
const val BUFFER_OUTGOING = 16384
const val _ROUTE_DO_ADD_DEFAULT_ROUTE = true
const val _ROUTE_DO_ROUTE_PRIVATE_ADDRESSES = false
const val _ROUTE_DO_ADD_CUSTOM_ROUTES = false
const val _DNS_CUSTOM_ADDRESS = ""
val _ROUTE_ALLOWED_APPS = emptySet<String>()
const val _ROUTE_CUSTOM_ROUTES = ""
const val _SSL_VERSION = "DEFAULT"
val _SSL_SUITES = emptySet<String>()
val _SSL_CERT_DIR: Uri? = null
const val _SSL_DO_ADD_CERT = false
const val _SSL_PORT = 443
const val _SSL_DO_SELECT_SUITES = false
const val _SSL_DO_VERIFY = false

const val _ROOT_STATE = false
const val _LOG_DO_SAVE_LOG = false
val _LOG_DIR: Uri? = null
const val _RECONNECTION_LIFE = 1
const val _RECONNECTION_INTERVAL = 10
