package com.finorix.signals.data.local

import androidx.room.TypeConverter
import com.finorix.signals.data.local.entity.SignalOutcome
import com.finorix.signals.domain.model.SignalDirection

class Converters {
    @TypeConverter
    fun fromDirection(value: SignalDirection): String = value.name
    
    @TypeConverter
    fun toDirection(value: String): SignalDirection = SignalDirection.valueOf(value)

    @TypeConverter
    fun fromOutcome(value: SignalOutcome): String = value.name
    
    @TypeConverter
    fun toOutcome(value: String): SignalOutcome = SignalOutcome.valueOf(value)
}
