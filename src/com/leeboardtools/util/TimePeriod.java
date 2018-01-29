/*
 * Copyright 2018 Albert Santos.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.leeboardtools.util;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.zone.ZoneRulesException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Immutable class that represents a period in time, with a begin time and an end time, which may be the same.
 * @author Albert Santos
 */
public class TimePeriod implements Comparable <TimePeriod> {
    final private Instant startInstant;
    final private Instant endInstant;
    
    /**
     * Constructor. The start instant must be before or equal to the end instant.
     * @param startInstant  The start instant, must not be <code>null</code>.
     * @param endInstant The end instant, must not be <code>null</code>
     */
    protected TimePeriod(Instant startInstant, Instant endInstant) {
        this.startInstant = startInstant;
        this.endInstant = endInstant;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.startInstant);
        hash = 53 * hash + Objects.hashCode(this.endInstant);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TimePeriod other = (TimePeriod) obj;
        if (!Objects.equals(this.startInstant, other.startInstant)) {
            return false;
        }
        if (!Objects.equals(this.endInstant, other.endInstant)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return toString(null);
    }
    
    
    /**
     * Returns a string representation of the time period relative to a given time zone id.
     * For zero duration time periods the date/time is returned.
     * For time periods that start and end at midnight (zone id time):
     * <ul>
     *      <li>If the time period is a single day the date is returned</li>
     *      <li>Otherwise a string in the form (start date) -> (end date - 1).
     * </ul>
     * For other time periods the full date/time is returned in the form:
     * (start date/time) -> (end date/time).
     * @param zoneId    The zone id of the date-times, if <code>null</code> {@link ZoneId#systemDefault() } is
     * called to obtain the zone id.
     * @return The string representation.
     */
    public String toString(ZoneId zoneId) {
        if (zoneId == null) {
            zoneId = ZoneId.systemDefault();
        }
        
        LocalDateTime startDateTime = LocalDateTime.ofInstant(this.startInstant, zoneId);
        LocalDateTime endDateTime = LocalDateTime.ofInstant(this.endInstant, zoneId);
        
        if (startDateTime.equals(endDateTime)) {
            return startDateTime.toString();
        }
        
        LocalTime startTime = startDateTime.toLocalTime();
        LocalTime endTime = endDateTime.toLocalTime();
        if (LocalTime.MIDNIGHT.equals(startTime) && LocalTime.MIDNIGHT.equals(endTime)) {
            LocalDate startDate = startDateTime.toLocalDate();
            LocalDate endDate = endDateTime.toLocalDate().minusDays(1);
            if (startDate.equals(endDate)) {
                return startDate.toString();
            }
            return startDate.toString() + " -> " + endDate.toString();
        }
        
        return startDateTime.toString() + " -> " + endDateTime.toString();
    }
    
    
    /**
     * Retrieves the instant defining the start of the time period.
     * @return The instant.
     */
    public final Instant getStartInstant() {
        return this.startInstant;
    }
    
    /**
     * Retrieves the instant defining the end of the time period. This is always
     * the same as or after the start instant.
     * @return The instant.
     */
    public final Instant getEndInstant() {
        return this.endInstant;
    }
    
    /**
     * Retrieves the duration of the time period.
     * @return The duration, this always >= 0.
     */
    public final Duration getDuration() {
        return Duration.between(this.startInstant, endInstant);
    }

    /**
     * Compares this time period to another time period. The ordering is first by the
     * start instants, and if those are the same then by the end instants.
     * @param o The time period to compare to.
     * @return &lt; 0 if the start instant of this time period is before the start instant
     * of the other time period, or if the start instants are the same but the end instant
     * is before the end instant of the other time period, 0 if the start instants are
     * equal and the end instants are equal, &gt; 0 otherwise.
     */
    @Override
    public int compareTo(TimePeriod o) {
        int result = this.startInstant.compareTo(o.startInstant);
        if (result != 0) {
            return result;
        }
        return this.endInstant.compareTo(o.endInstant);
    }
    
    
    /**
     * Defines the relationship of a point in time to a time period.
     */
    public static enum Relation {
        BEFORE,
        ON_START_EDGE,
        INSIDE,
        ON_END_EDGE,
        AFTER
    }
    
    /**
     * Determines when an instant in time is relative to the time period.
     * @param instant   The instant of interest.
     * @return The relationship of instant to the time period.
     */
    public final Relation getTimeRelation(Instant instant) {
        int result = instant.compareTo(this.startInstant);
        if (result <= 0) {
            return (result < 0) ? Relation.BEFORE : Relation.ON_START_EDGE;
        }
        
        result = instant.compareTo(this.endInstant);
        if (result >= 0) {
            return (result > 0) ? Relation.AFTER : Relation.ON_END_EDGE;
        }
        return Relation.INSIDE;
    }
    
    /**
     * Determines when a {@link LocalDateTime} is relative to the time period.
     * @param dateTime  The date-time of interest.
     * @param zoneId    The zone id of the date-times, if <code>null</code> {@link ZoneId#systemDefault() } is
     * called to obtain the zone id.
     * @return The relationship of the date-time to the time period.
     */
    public final Relation getTimeRelation(LocalDateTime dateTime, ZoneId zoneId) {
        if (zoneId == null) {
            zoneId = ZoneId.systemDefault();
        }
        return getTimeRelation(Instant.from(dateTime.atZone(zoneId)));
    }
    
    /**
     * Defines the overlap relationship between two time periods.
     * TODO Need to refine this a bit more when the edges land on each other.
     * If an end is equal to a start, then its a TOUCH_END or TOUCH_START.
     */
    public static enum Overlap {
        /**
         * No overlap.
         * <pre>
         * a)        S----E
         *    s---e
         * 
         * b)        S----E
         *                   s---e
         * </pre>
         * If a.getOverlap(b) == NONE then b.getOverlap(a) == NONE.
         */
        NONE,
        
        /**
         * This start instant is before the start of the other, and the end instant
         * is the same as the start of the other.
         * <pre>
         * a)        S----E
         *                s----e
         * </pre>
         * If a.getOverplap(b) == TOUCH_OTHER_START then b.getOverlap(a) == TOUCH_OTHER_END.
         */
        TOUCH_OTHER_START,
        
        /**
         * This start instant is before the start of the other, but this end instant
         * is within the other.
         * <pre>
         * a)        S----E
         *             s----e
         * 
         * </pre>
         * If a.getOverlap(b) == OTHER_START then b.getOverlap(a) == OTHER_END
         */
        OTHER_START,
        
        /**
         * This fully encloses the other, but does not represent the same period as the other..
         * <pre>
         * a)        S----E
         *            s--e
         * 
         * b)        S----E
         *             s--e
         * 
         * c)        S----E
         *           s--e
         * </pre>
         * If a.getOverlap(b) == ENCLOSES_OTHER then b.getOverlap(a) == INSIDE_OTHER.
         */
        ENCLOSES_OTHER,
        
        /**
         * This represents the same time period as the other.
         * <pre>
         * a)        S----E
         *           s----e
         * </pre>
         * If a.getOverlap(b) == SAME then b.getOverlap(a) == SAME.
         */
        SAME,
        
        /**
         * This start instant is within the other, but the end instant is after the end of the other.
         * <pre>
         * a)        S----E
         *         s---e
         * </pre>
         * If a.getOverlap(b) == OTHER_END then b.getOverlap(a) == OTHER_START
         */
        OTHER_END,
        
        /**
         * This start is the same as the other end, and this end is after the other end.
         * <pre>
         * a)        S----E
         *       s---e
         * </pre>
         * If a.getOverlap(b) == TOUCH_OTHER_END then b.getOverlap(a) == TOUCH_OTHER_START.
         */
        TOUCH_OTHER_END,
        
        /**
         * This is wholly enclosed by the other, but does not represent the same period as the other.
         * <pre>
         * a)        S----E
         *         s--------e
         * 
         * b)        S----E
         *         s------e
         * 
         * c)        S----E
         *           s-------e
         * </pre>
         * If a.getOverlap(b) == INSIDE_OTHER then b.getOverlap(a) == ENCLOSES_OTHER.
         */
        INSIDE_OTHER,
    }
    
    /**
     * Determines the overlap between this time period and another time period.
     * @param other The time period to compare against.
     * @return The type of overlap.
     */
    public final Overlap getOverlap(TimePeriod other) {
        if (this.endInstant.isBefore(other.startInstant) || this.startInstant.isAfter(other.endInstant)) {
            return Overlap.NONE;
        }
        
        int compareStarts = this.startInstant.compareTo(other.startInstant);
        int compareEnds = this.endInstant.compareTo(other.endInstant);
        if (compareStarts < 0) {
            // S----E
            //   s-e        ENCLOSES_OTHER
            //   s--e       ENCLOSES_OTHER
            //   s-----e    OTHER_START
            //      s---e   TOUCH_OTHER_START
            if (compareEnds >= 0) {
                return Overlap.ENCLOSES_OTHER;
            }
            else if (this.endInstant.equals(other.startInstant)) {
                return Overlap.TOUCH_OTHER_START;
            }
            return Overlap.OTHER_START;
        }
        else if (compareStarts == 0) {
            // S----E
            // s--e         ENCLOSES_OTHER
            // s----e       SAME
            // s------e     INSIDE_OTHER
            if (compareEnds == 0) {
                return Overlap.SAME;
            }
            else if (compareEnds > 0) {
                return Overlap.ENCLOSES_OTHER;
            }
            return Overlap.INSIDE_OTHER;
        }
        
        //    S----E
        // s--e             TOUCH_OTHER_END
        // s-----e          OTHER_END
        // s-------e        INSIDE_OTHER
        // s---------e      INSIDE_OTHER
        
        if (compareEnds > 0) {
            if (this.startInstant.equals(other.endInstant)) {
                return Overlap.TOUCH_OTHER_END;
            }
            return Overlap.OTHER_END;
        }
        return Overlap.INSIDE_OTHER;
    }
    
    /**
     * Determines if any portion of the time period falls within the 24 hours of a local date.
     * @param date  The date of interest.
     * @param zoneId    The zone id of the date-times, if <code>null</code> {@link ZoneId#systemDefault() } is
     * called to obtain the zone id.
     * @return <code>true</code> if any portion of the time period falls within date.
     */
    public final boolean containsDate(LocalDate date, ZoneId zoneId) {
        TimePeriod datePeriod = fromEdgeDates(date, date, zoneId);
        Overlap overlap = this.getOverlap(datePeriod);
        switch (overlap) {
            case NONE :
            case TOUCH_OTHER_START :
            case TOUCH_OTHER_END :
                return false;
        }
        return true;
    }
    
    
    /**
     * Fills a collection with the local dates spanned by the time period.
     * @param dates The collection to add the dates to, if <code>null</code> then an {@link ArrayList} will
     * be created.
     * @param zoneId    The zone id of the date-times, if <code>null</code> {@link ZoneId#systemDefault() } is
     * called to obtain the zone id.
     * @return The collection containing the local dates.
     */
    public final Collection<LocalDate> getDates(Collection<LocalDate> dates, ZoneId zoneId) {
        if (dates == null) {
            dates = new ArrayList<>();
        }
        if (zoneId == null) {
            zoneId = ZoneId.systemDefault();
        }
        
        LocalDate date = this.startInstant.atZone(zoneId).toLocalDate();
        LocalDateTime endDateTime = LocalDateTime.ofInstant(this.endInstant, zoneId);
        LocalDate endDate = endDateTime.toLocalDate();
        if (!endDate.atStartOfDay().equals(endDateTime)) {
            endDate = endDate.plusDays(1);
        }
        
        while (true) {
            dates.add(date);
            date = date.plusDays(1);
            if (!date.isBefore(endDate)) {
                break;
            }
        }
        return dates;
    }
    
    
    /**
     * Obtains an instance of TimePeriod whose start instant is now and with a duration of 1 hour.
     * @return The time period.
     */
    public static TimePeriod now() {
        return fromEdgeAndDuration(Instant.now(), Duration.ofHours(1));
    }
    
    
    /**
     * Obtains an instance of TimePeriod using two {@link Instant}s defining the edges of the period.
     * @param instantA  The instant defining one edge of the time period.
     * @param instantB  The instant defining the other edge of the time period.
     * @return The time period.
     */
    public static TimePeriod fromEdgeTimes(Instant instantA, Instant instantB) {
        if (instantA.isAfter(instantB)) {
            return new TimePeriod(instantB, instantA);
        }
        return new TimePeriod(instantA, instantB);
    }
    
    /**
     * Obtains an instance of TimePeriod using an {@link Instant} defining one edge and
     * the duration of the time period.
     * @param instant   The instant defining one edge of the time period.
     * @param duration  The duration of the time period. If less than zero then instant is
     * the end instant.
     * @return The time period.
     */
    public static TimePeriod fromEdgeAndDuration(Instant instant, Duration duration) {
        if (duration.isNegative()) {
            return new TimePeriod(instant.plus(duration), instant);
        }
        return new TimePeriod(instant, instant.plus(duration));
    }
    
    /**
     * Obtains an instance of TimePeriod using two {@link LocalDateTime}s defining the edges of the period.
     * @param dateTimeA The date-time defining one edge of the time period.
     * @param dateTimeB The date-time defining the other edge of the time period.
     * @param zoneId    The zone id of the date-times, if <code>null</code> {@link ZoneId#systemDefault() } is
     * called to obtain the zone id.
     * @return The time period.
     */
    public static TimePeriod fromEdgeTimes(LocalDateTime dateTimeA, LocalDateTime dateTimeB, ZoneId zoneId) {
        if (zoneId == null) {
            zoneId = ZoneId.systemDefault();
        }
        
        Instant instantA = Instant.from(dateTimeA.atZone(zoneId));
        Instant instantB = Instant.from(dateTimeB.atZone(zoneId));
        return fromEdgeTimes(instantA, instantB);
    }
    
    /**
     * Obtains an instance of TimePeriod using a {@link LocalDateTime} defining one edge and the
     * duration of the time period.
     * @param dateTime  The date-time defining one edge of the time period.
     * @param duration  The duration of the time period. If less than zero then dateTime defines
     * the end of the time period.
     * @param zoneId    The zone id of the date-times, if <code>null</code> {@link ZoneId#systemDefault() } is
     * called to obtain the zone id.
     * @return The time period.
     */
    public static TimePeriod fromEdgeAndDuration(LocalDateTime dateTime, Duration duration, ZoneId zoneId) {
        if (zoneId == null) {
            zoneId = ZoneId.systemDefault();
        }
        Instant instant = Instant.from(dateTime.atZone(zoneId));
        return fromEdgeAndDuration(instant, duration);
    }
    
    /**
     * Obtains an instance of TimePeriod using a {@link LocalDate} defining one edge and the
     * duration of the time period.
     * @param date  The date, {@link LocalDate#atStartOfDay() } is called to obtain the actual date-time.
     * @param duration  The duration of the time period. If less than zero then date defines
     * the end of the time period.
     * @param zoneId    The zone id of the date-times, if <code>null</code> {@link ZoneId#systemDefault() } is
     * called to obtain the zone id.
     * @return The time period.
     */
    public static TimePeriod fromEdgeAndDuration(LocalDate date, Duration duration, ZoneId zoneId) {
        return fromEdgeAndDuration(date.atStartOfDay(), duration, zoneId);
    }
    
    /**
     * Obtains an instance of TimePeriod using two {@link LocalDate}s defining the edge days of the
     * time period. Note that the entire day is used, so if dateA.equals(dateB), then the period's
     * duration will be 24 hours, if dateB is the following day, then the period will be 48 hours, starting
     * from the start of day of dateA and ending with the end of day of dateB.
     * @param dateA One of the edge days.
     * @param dateB The other edge day.
     * @param zoneId    The zone id of the dates, if <code>null</code> {@link ZoneId#systemDefault() } is
     * called to obtain the zone id.
     * @return The time period.
     */
    public static TimePeriod fromEdgeDates(LocalDate dateA, LocalDate dateB, ZoneId zoneId) {
        if (dateA.isAfter(dateB)) {
            LocalDate tmpDate = dateA;
            dateA = dateB;
            dateB = tmpDate;
        }
        
        Instant startInstant = localDateToStartInstant(dateA, zoneId);
        Instant endInstant = localDateToEndInstant(dateB, zoneId);
        
        return fromEdgeTimes(startInstant, endInstant);
    }
    
    
    /**
     * Obtains an instance of TimePeriod that starts with the earliest time covered
     * by two time periods and the latest time covered by the two time periods.
     * @param periodA   The first time period.
     * @param periodB   The second time period.
     * @return The new time period.
     */
    public static TimePeriod joinPeriods(TimePeriod periodA, TimePeriod periodB) {
        Instant startInstant = (periodA.startInstant.isBefore(periodB.startInstant))
                ? periodA.startInstant : periodB.startInstant;
        Instant endInstant = (periodA.endInstant.isAfter(periodB.endInstant))
                ? periodA.endInstant : periodB.endInstant;
        return new TimePeriod(startInstant, endInstant);
    }
    
    
    /**
     * Converts a {@link LocalDate} to an {@link Instant} that's compatible with the
     * start instant used by {@link TimePeriod#fromEdgeDates(java.time.LocalDate, java.time.LocalDate, java.time.ZoneId) }.
     * @param date  The date to convert.
     * @param zoneId    The zone id of the dates, if <code>null</code> {@link ZoneId#systemDefault() } is
     * called to obtain the zone id.
     * @return The instant.
     */
    public static Instant localDateToStartInstant(LocalDate date, ZoneId zoneId) {
        if (zoneId == null) {
            zoneId = ZoneId.systemDefault();
        }
        return date.atStartOfDay(zoneId).toInstant();
    }
    
    
    /**
     * Converts a {@link LocalDate} to an {@link Instant} that's compatible with the
     * end instant used by {@link TimePeriod#fromEdgeDates(java.time.LocalDate, java.time.LocalDate, java.time.ZoneId) }.
     * @param date  The date to convert.
     * @param zoneId    The zone id of the dates, if <code>null</code> {@link ZoneId#systemDefault() } is
     * called to obtain the zone id.
     * @return The instant.
     */
    public static Instant localDateToEndInstant(LocalDate date, ZoneId zoneId) {
        if (zoneId == null) {
            zoneId = ZoneId.systemDefault();
        }
        return date.atStartOfDay(zoneId).plusDays(1).toInstant();
    }
    
    
    /**
     * Helper that writes an {@link Instant}'s state to a JSON object that can be read
     * back in with {@link TimePeriod#instantFromJSON(org.json.JSONObject, java.lang.String) }
     * @param jsonObject    The JSON object.
     * @param key   The key for the instant's state within the JSON object.
     * @param instant The instant to be written, may be <code>null</code>.
     */
    public static void instantToJSON(JSONObject jsonObject, String key, Instant instant) {
        jsonObject.put(key, (instant == null) ? JSONObject.NULL : instant.toString());
    }
    
    /**
     * Helper that retrieves an {@link Instant} based upon the state stored in a JSON object.
     * @param jsonObject    The JSON object.
     * @param key   The key for the instant's state within the JSON object.
     * @return The instant, <code>null</code> if key's value is <code>null</code>
     * @throws JSONException    if there is no string value for the key.
     * @throws DateTimeParseException   if the key's value could not be parsed into an instant.
     */
    public static Instant instantFromJSON(JSONObject jsonObject, String key) throws JSONException, DateTimeParseException {
        String text = jsonObject.getString(key);
        if (text == JSONObject.NULL) {
            return null;
        }
        return Instant.parse(text);
    }
    
    /**
     * Helper that writes a {@link ZoneId}'s id to a JSON object that can be read back in
     * with {@link TimePeriod#optZoneIdFromJSON(org.json.JSONObject, java.lang.String, java.time.ZoneId) }.
     * @param jsonObject    The JSON object.
     * @param key   The key for the zone id.
     * @param zoneId The zone id to be written, may be <code>null</code>.
     */
    public static void zoneIdToJSON(JSONObject jsonObject, String key, ZoneId zoneId) {
        jsonObject.put(key, (zoneId == null) ? JSONObject.NULL : zoneId.toString());
    }
    
    /**
     * Helper that retrieves a {@link ZoneId} based upon the id stored in a JSON object.
     * @param jsonObject    The JSON object.
     * @param key   The key for the zone id's value within the JSON object.
     * @param defZoneId The zone id to return if the key is either not present or is null.
     * @return  The zone id.
     * @throws DateTimeException    if the zone id's value has an invalid format.
     * @throws ZoneRulesException if the zone id is a region ID that cannot be found.
     */
    public static ZoneId optZoneIdFromJSON(JSONObject jsonObject, String key, ZoneId defZoneId) throws DateTimeException, ZoneRulesException {
        String text = jsonObject.optString(key);
        if ((text == null) || text.isEmpty()) {
            return defZoneId;
        }
        return ZoneId.of(text);
    }
    
    
    /**
     * Adds the time period's state to a JSON object that can be read back with
     * {@link TimePeriod#fromJSON(org.json.JSONObject) }.
     * Note that {@link TimePeriod#toJSON(org.json.JSONObject, java.lang.String) } is
     * the more common method to use, this should only be used for special situations.
     * @param jsonObject The JSON object.
     */
    public final void toJSON(JSONObject jsonObject) {
        instantToJSON(jsonObject, "startInstant", startInstant);
        instantToJSON(jsonObject, "endInstant", endInstant);
    }
    
    /**
     * Obtains an instance of TimePeriod from a JSON object. Note that {@link TimePeriod#fromJSON(org.json.JSONObject, java.lang.String) }
     * is the more common method to use, this should only be used for special situations.
     * @param jsonObject    The JSON object representing the time period.
     * @return The time period.
     * @throws JSONException    if there is no string value for the key.
     * @throws DateTimeParseException   if the key's value could not be parsed into an instant.
     */
    public static TimePeriod fromJSON(JSONObject jsonObject) throws JSONException, DateTimeParseException {
        Instant startInstant = instantFromJSON(jsonObject, "startInstant");
        Instant endInstant = instantFromJSON(jsonObject, "endInstant");
        return new TimePeriod(startInstant, endInstant);
    }
    
    
    /**
     * Creates a {@link JSONObject} and writes the state of the time period to it.
     * @return The JSON object.
     */
    public final JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        toJSON(jsonObject);
        return jsonObject;
    }
    
    
    /**
     * Adds the time period as a JSON object for a key. The time period can be read
     * back with {@link TimePeriod#fromJSON(org.json.JSONObject, java.lang.String) },
     * This differs from {@link TimePeriod#toJSON(org.json.JSONObject) } in that this one
     * creates the JSON object to which the time period is written as an object within the
     * JSON object passed to it. This is the more common method to use.
     * @param jsonObject    The JSON object to contain the time period's JSON object.
     * @param key The key for the time period's JSON object.
     */
    public final void toJSON(JSONObject jsonObject, String key) {
        jsonObject.put(key, toJSONObject());
    }
    
    /**
     * Obtains an instance of TimePeriod from the value of a key within a JSON object.
     * @param jsonObject    The JSON object.
     * @param key   The key for the time period's JSON object within jsonObject.
     * @return  The time period.
     * @throws JSONException    if there is no JSON value for the key.
     * @throws DateTimeParseException   if the key's value could not be parsed into an instant.
     */
    public static TimePeriod fromJSON(JSONObject jsonObject, String key) throws JSONException, DateTimeParseException {
        JSONObject periodObject = jsonObject.getJSONObject(key);
        if (periodObject == null) {
            return null;
        }
        return fromJSON(periodObject);
    }
    
    
    
    
    /**
     * Comparator that orders first by startInstant, and if those are the same then
     * by the end instant. The ordering is < 0 if the o1 instant is before the o2 instant.
     */
    public static class StartComparator implements Comparator <TimePeriod> {
        @Override
        public int compare(TimePeriod o1, TimePeriod o2) {
            int result = o1.startInstant.compareTo(o2.startInstant);
            if (result != 0) {
                return result;
            }
            return o1.endInstant.compareTo(o2.endInstant);
        }
    }
    
    
    /**
     * Comparator that orders first by endInstant, and if those are the same then
     * by start instant. The ordering is < 0 if the o1 instant is before the o2 instant.
     */
    public static class EndComparator implements Comparator <TimePeriod> {
        @Override
        public int compare(TimePeriod o1, TimePeriod o2) {
            int result = o1.endInstant.compareTo(o2.endInstant);
            if (result != 0) {
                return result;
            }
            return o1.startInstant.compareTo(o2.startInstant);
        }
    }
}
