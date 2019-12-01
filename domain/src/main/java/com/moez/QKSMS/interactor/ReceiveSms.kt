/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moez.QKSMS.interactor

import android.os.Build
import android.telephony.SmsMessage
import android.util.Log
import androidx.annotation.RequiresApi
import com.moez.QKSMS.blocking.BlockingClient
import com.moez.QKSMS.extensions.mapNotNull
import com.moez.QKSMS.manager.NotificationManager
import com.moez.QKSMS.manager.ShortcutManager
import com.moez.QKSMS.repository.ConversationRepository
import com.moez.QKSMS.repository.ForwardDingDingRepository
import com.moez.QKSMS.repository.MessageRepository
import com.moez.QKSMS.util.Preferences
import io.reactivex.Flowable
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.stream.Collectors.toList
import javax.inject.Inject

class ReceiveSms @Inject constructor(
        private val conversationRepo: ConversationRepository,
        private val blockingClient: BlockingClient,
        private val prefs: Preferences,
        private val messageRepo: MessageRepository,
        private val notificationManager: NotificationManager,
        private val updateBadge: UpdateBadge,
        private val shortcutManager: ShortcutManager,
        private val forwardDingDingRepository: ForwardDingDingRepository
) : Interactor<ReceiveSms.Params>() {

    class Params(val subId: Int, val messages: Array<SmsMessage>)

    val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun buildObservable(params: Params): Flowable<*> {
        return Flowable.just(params)
                .filter { it.messages.isNotEmpty() }
                .mapNotNull {
                    // Don't continue if the sender is blocked
                    val messages = it.messages
                    val address = messages[0].displayOriginatingAddress
                    val action = blockingClient.getAction(address).blockingGet()
                    val shouldDrop = prefs.drop.get()
                    Timber.v("block=$action, drop=$shouldDrop")

                    // If we should drop the message, don't even save it
                    if (action is BlockingClient.Action.Block && shouldDrop) {
                        return@mapNotNull null
                    }

                    val time = messages[0].timestampMillis
                    val body: String = messages
                            .mapNotNull { message -> message.displayMessageBody }
                            .reduce { body, new -> body + new }
                    val builder = StringBuilder();
                    builder.append(it.subId).append(":").append(address).append(":").append(body).append(":").append(time)
                    Log.i("ReceiveSMS", builder.toString());
                    //这里可以做拦截转发
                    if (prefs.switch.get()) {
                        forward(address, body)
                    }
                    // Add the message to the db
                    val message = messageRepo.insertReceivedSms(it.subId, address, body, time)

                    when (action) {
                        is BlockingClient.Action.Block -> {
                            messageRepo.markRead(message.threadId)
                            conversationRepo.markBlocked(listOf(message.threadId), prefs.blockingManager.get(), action.reason)
                        }
                        is BlockingClient.Action.Unblock -> conversationRepo.markUnblocked(message.threadId)
                        else -> Unit
                    }

                    message
                }
                .doOnNext { message ->
                    conversationRepo.updateConversations(message.threadId) // Update the conversation
                }
                .mapNotNull { message ->
                    conversationRepo.getOrCreateConversation(message.threadId) // Map message to conversation
                }
                .filter { conversation -> !conversation.blocked } // Don't notify for blocked conversations
                .doOnNext { conversation ->
                    // Unarchive conversation if necessary
                    if (conversation.archived) conversationRepo.markUnarchived(conversation.id)
                }
                .map { conversation -> conversation.id } // Map to the id because [delay] will put us on the wrong thread
                .doOnNext { threadId -> notificationManager.update(threadId) } // Update the notification
                .doOnNext { shortcutManager.updateShortcuts() } // Update shortcuts
                .flatMap { updateBadge.buildObservable(Unit) } // Update the badge and widget
    }

    /**
     * 转发
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun forward(address: String, message: String) {
        val keyWords = forwardDingDingRepository.getKeyWords();
        keyWords.stream().map { k -> k.word }.collect(toList()).forEach {
            if (message.contains(it)) {
                Log.i("ReceiveSms", "拦截到了信息")
                requestDingDing(address, message)
            }
        }
    }

    /**
     * 转发到钉钉
     */
    private fun requestDingDing(address: String, message: String) {
        val body = JSONObject();
        body.put("msgtype", "text");
        val innerBody = JSONObject();
        innerBody.put("content", "发送人:" + address + "\r\n" + "内容:" + message)
        body.put("text", innerBody)
        val client = OkHttpClient()
        val requestBody = RequestBody.create(mediaType, body.toString());
        val url = "https://oapi.dingtalk.com/robot/send?access_token=" + prefs.token.get()
        val request = Request.Builder().url(url).post(requestBody).build();
        val response = client.newCall(request).execute();
        Log.i("ReciiveSms", response.body?.string())
    }
}
