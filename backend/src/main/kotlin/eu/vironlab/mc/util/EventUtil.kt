package eu.vironlab.mc.util

import com.google.gson.Gson
import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.api.eventapi.ISynchronizedEvent
import eu.thesimplecloud.api.message.IMessageChannel
import eu.thesimplecloud.api.message.IMessageListener
import eu.thesimplecloud.api.network.component.INetworkComponent

object EventUtil {

    private var eventChannel: IMessageChannel<EventChannel>? = null

    @JvmStatic
    lateinit var instance: GlobalEventProvider

    fun callGlobal(event: ISynchronizedEvent) = instance.call(event)

}

interface GlobalEventProvider {
    fun call(event: ISynchronizedEvent)
}

class ManagerGlobalEventProvider() : GlobalEventProvider {

    val channel: IMessageChannel<EventChannel> = CloudAPI.instance.getMessageChannelManager()
        .registerMessageChannel(
            CloudAPI.instance.getThisSidesCloudModule(),
            "backendEventChannel",
            EventChannel::class.java
        )


    init {
        channel.registerListener(object: IMessageListener<EventChannel> {
            override fun messageReceived(msg: EventChannel, sender: INetworkComponent) {
                try {
                    val clazz = Class.forName(msg.eventClass)
                    val event = Gson().fromJson(msg.data, clazz) as ISynchronizedEvent
                    call(event)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    override fun call(event: ISynchronizedEvent) {
        CloudAPI.instance.getEventManager().call(event, false)
    }
}

class ServiceGlobalEventProvider : GlobalEventProvider {

    val channel: IMessageChannel<EventChannel> = CloudAPI.instance.getMessageChannelManager()
        .registerMessageChannel(
            CloudAPI.instance.getThisSidesCloudModule(),
            "backendEventChannel",
            EventChannel::class.java
        )

    override fun call(event: ISynchronizedEvent) {
        channel.sendMessage(EventChannel(event.javaClass.canonicalName, Gson().toJson(event)), INetworkComponent.MANAGER_COMPONENT)
    }

}

class EventChannel(val eventClass: String, val data: String)
