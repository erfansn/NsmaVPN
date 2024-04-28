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
package ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.incoming

class EchoTimer(private val interval: Long, private val echoFunction: suspend () -> Unit) {
    private var lastTicked = 0L
    private var deadline = 0L

    private var isEchoWaited = false

    private val isOutOfTime: Boolean
        get() = System.currentTimeMillis() - lastTicked > interval

    private val isDead: Boolean
        get() = System.currentTimeMillis() > deadline

    suspend fun checkAlive(): Boolean {
        if (isOutOfTime) {
            if (isEchoWaited) {
                if (isDead) {
                    return false
                }
            } else {
                echoFunction()
                isEchoWaited = true
                deadline = System.currentTimeMillis() + interval
            }
        }

        return true
    }

    fun tick() {
        lastTicked = System.currentTimeMillis()
        isEchoWaited = false
    }
}
