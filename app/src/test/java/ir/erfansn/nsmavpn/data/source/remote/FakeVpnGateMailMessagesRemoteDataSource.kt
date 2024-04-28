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
package ir.erfansn.nsmavpn.data.source.remote

import com.google.api.services.gmail.model.Message

class FakeVpnGateMailMessagesRemoteDataSource : VpnGateMailMessagesRemoteDataSource {

    lateinit var messages: Map<String, VpnGateMessage>

    override suspend fun fetchMessageIdList(emailAddress: String, lastDay: Int): List<String> {
        return messages.keys.toList()
    }

    override suspend fun fetchMessageBodyText(emailAddress: String, messageId: String): String {
        return messages[messageId]!!.content
    }

    override suspend fun fetchMessage(emailAddress: String, messageId: String): Message {
        return Message().setInternalDate(messages[messageId]!!.receiveTime)
    }
}

data class VpnGateMessage(
    val receiveTime: Long,
    val content: String,
)

