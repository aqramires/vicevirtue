package com.aliceqr.vicevirtue.data

import com.aliceqr.vicevirtue.data.db.entity.EventEntity
import com.aliceqr.vicevirtue.data.db.entity.TrackableEntity
import com.aliceqr.vicevirtue.domain.model.Trackable
import com.aliceqr.vicevirtue.domain.model.TrackableEvent
import com.aliceqr.vicevirtue.domain.model.TrackableType

fun TrackableEntity.toDomain(): Trackable {
    return Trackable(
        id = id,
        name = name,
        type = TrackableType.valueOf(type),
        createdAt = createdAt
    )
}

fun Trackable.toEntity(): TrackableEntity {
    return TrackableEntity(
        id = id,
        name = name,
        type = type.name,
        createdAt = createdAt
    )
}

fun EventEntity.toDomain(): TrackableEvent {
    return TrackableEvent(
        id = id,
        trackableId = trackableId,
        description = description,
        timestamp = timestamp,
        count = count
    )
}

fun TrackableEvent.toEntity(): EventEntity {
    return EventEntity(
        id = id,
        trackableId = trackableId,
        description = description,
        timestamp = timestamp,
        count = count
    )
}
