package dev.guilhermealves.assets.portfolio.api.app.domain.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class LocalDateTimeConverter {

    public static LocalDateTime convertTimeZone(ZoneId fromZoneID, ZoneId toZoneID, LocalDateTime localDateTime){

        ZonedDateTime fromZoned = localDateTime.atZone(fromZoneID);

        ZonedDateTime toZoned = fromZoned.withZoneSameInstant(toZoneID);

        return toZoned.toLocalDateTime();
    }
}
