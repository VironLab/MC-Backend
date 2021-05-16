package eu.vironlab.mc.util

import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.api.eventapi.ISynchronizedEvent

object EventUtil {

    fun callGlobal(event: ISynchronizedEvent) =
        CloudAPI.instance.getEventManager().call(event, false)

}