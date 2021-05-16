package eu.vironlab.mc.feature.punishment.event

import eu.thesimplecloud.api.eventapi.ISynchronizedEvent
import eu.vironlab.mc.feature.punishment.Punishment
import java.util.*

class PunishmentUpdateEvent(val punishment: List<Punishment>, val target: UUID) : ISynchronizedEvent

class PunishmentAddEvent(val punishment: Punishment, val target: UUID) : ISynchronizedEvent
