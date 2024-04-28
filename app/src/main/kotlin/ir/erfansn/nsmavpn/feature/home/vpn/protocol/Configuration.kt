/*
 * Copyright 2024 Erfan Sn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ir.erfansn.nsmavpn.feature.home.vpn.protocol

import android.net.Uri

const val MAX_MRU = 2000
const val MIN_MRU = 68
const val DEFAULT_MRU = 1500
const val DEFAULT_MTU = 1500

private const val EMPTY_TEXT = ""

object OscPrefKey {
    const val SSL_DO_VERIFY = true
    const val SSL_DO_ADD_CERT = false
    const val SSL_DO_SELECT_SUITES = false
    const val PROXY_DO_USE_PROXY = false
    const val PPP_PAP_ENABLED = true
    const val PPP_MSCHAPv2_ENABLED = true
    const val PPP_IPv4_ENABLED = true
    const val PPP_DO_REQUEST_STATIC_IPv4_ADDRESS = false
    const val PPP_IPv6_ENABLED = false
    const val DNS_DO_REQUEST_ADDRESS = true
    const val DNS_DO_USE_CUSTOM_SERVER = false
    const val ROUTE_DO_ADD_DEFAULT_ROUTE = true
    const val ROUTE_DO_ROUTE_PRIVATE_ADDRESSES = false
    const val ROUTE_DO_ADD_CUSTOM_ROUTES = false
    var SSL_PORT = 443
    const val PROXY_PORT = 8080
    const val PPP_MRU = DEFAULT_MRU
    const val PPP_MTU = DEFAULT_MTU
    const val PPP_AUTH_TIMEOUT = 3
    var HOME_HOSTNAME = EMPTY_TEXT
    const val HOME_USERNAME = "vpn"
    const val HOME_PASSWORD = "vpn"
    const val PROXY_HOSTNAME = EMPTY_TEXT
    const val PROXY_USERNAME = EMPTY_TEXT
    const val PROXY_PASSWORD = EMPTY_TEXT
    const val PPP_STATIC_IPv4_ADDRESS = EMPTY_TEXT
    const val DNS_CUSTOM_ADDRESS = EMPTY_TEXT
    const val ROUTE_CUSTOM_ROUTES = EMPTY_TEXT
    const val SSL_VERSION = "DEFAULT"
    val SSL_SUITES = emptySet<String>()
    val SSL_CERT_DIR: Uri? = null
    var DISALLOWED_APPS = emptyList<String>()
}
