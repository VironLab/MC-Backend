package eu.vironlab.mc.feature.moderation.event

import eu.thesimplecloud.api.eventapi.ISynchronizedEvent
import eu.vironlab.mc.feature.moderation.chatlog.Chatlog
import eu.vironlab.mc.feature.moderation.Punishment
import java.util.*

class PunishmentUpdateEvent(val punishment: List<Punishment>, val target: UUID) : ISynchronizedEvent

class PunishmentAddEvent(val punishment: Punishment, val target: UUID) : ISynchronizedEvent

class ChatlogCreatedEvent(val id: String, val chatlog: Chatlog) : ISynchronizedEvent
