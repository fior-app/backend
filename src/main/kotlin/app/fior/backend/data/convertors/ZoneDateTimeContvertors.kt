package app.fior.backend.data.convertors

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import java.time.ZoneOffset

import java.time.ZonedDateTime
import java.util.*

@ReadingConverter
class ZonedDateTimeReadConverter : Converter<Date?, ZonedDateTime?> {
    override fun convert(date: Date): ZonedDateTime {
        return date.toInstant().atZone(ZoneOffset.UTC)
    }
}

@WritingConverter
class ZonedDateTimeWriteConverter : Converter<ZonedDateTime, Date> {
    override fun convert(zonedDateTime: ZonedDateTime): Date {
        return Date.from(zonedDateTime.toInstant())
    }
}