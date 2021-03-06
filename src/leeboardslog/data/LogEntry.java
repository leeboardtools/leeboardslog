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
package leeboardslog.data;

import com.leeboardtools.text.StyledText;
import com.leeboardtools.text.TextUtil;
import com.leeboardtools.util.JSONUtil;
import com.leeboardtools.util.TimePeriod;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.UUID;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.property.ReadOnlySetWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.util.Callback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an entry in the log book. All entries have a GUID stored as a string
 * and a start {@link Instant} and an {@link Duration} that define the time period
 * the log entry represents.
 * <p>
 * To figure out:
 * How to handle the contents of the log entry. What do we want to be able to support?
 * Basic formatting/markup, such as emphasis, bold, etc. Really just want styles!
 * But should be a text-based markup language ala XML/HTML.
 * Maybe just use HTML with classes defined for the styling, then we could just implement
 * the styling via CSS.
 * 
 * 
 * @author Albert Santos
 */
public class LogEntry {
    public static final String GUID_KEY = "guid";
    public static final String VERSION_KEY = "version";
    public static final String TIME_PERIOD_PROP = "timePeriod";
    public static final String ZONE_ID_PROP = "zoneId";
    public static final String TITLE_PROP = "title";
    public static final String LATEST_AUTHOR_PROP = "latestAuthor";
    public static final String TAGS_PROP = "tags";
    public static final String DETAIL_LEVEL_PROP = "detailLevel";
    public static final String BODY_PROP = "body";
    public static final String BODY_FORMAT_PROP = "bodyFormat";
    
    private final String guid;
    
    private final List<Listener> listeners = new ArrayList<>();
    
    /**
     * Counter that if greater than 0 disables the firing of the listeners.
     */
    protected int listenerDisableCount;
    
    
    
    /**
     * Defines the {@link TimePeriod} of the log entry.
     */
    private final ObjectProperty<TimePeriod> timePeriod = new SimpleObjectProperty<>(this, TIME_PERIOD_PROP, TimePeriod.now());

    public final ObjectProperty<TimePeriod> timePeriodProperty() {
        return this.timePeriod;
    }
    public final TimePeriod getTimePeriod() {
        return this.timePeriod.get();
    }
    public final void setTimePeriod(TimePeriod value) {
        this.timePeriod.set(value);
    }
    
    
    
    /**
     * Defines the zoneId associated with the log entry. The zone id is typically the
     * zone id associated with the location of where the log entry started. Changing the
     * zone id does not change the start instant of the log entry.
     */
    private final ObjectProperty<ZoneId> zoneId = new SimpleObjectProperty<>(this, ZONE_ID_PROP, null);

    public final ObjectProperty<ZoneId> zoneIdProperty() {
        return this.zoneId;
    }
    public final ZoneId getZoneId() {
        return this.zoneId.get();
    }
    public final void setZoneId(ZoneId value) {
        this.zoneId.set(value);
    }
    


    /**
     * Defines the title of the log entry.
     */
    private final StringProperty title = new SimpleStringProperty(this, TITLE_PROP, "");

    public final StringProperty titleProperty() {
        return title;
    }
    public final String getTitle() {
        return title.get();
    }
    public final void setTitle(String value) {
        title.set(value);
    }
    
    

    /**
     * Defines the latest author of the log entry.
     */
    private final StringProperty latestAuthor = new SimpleStringProperty(this, LATEST_AUTHOR_PROP);

    public final StringProperty latestAuthorProperty() {
        return latestAuthor;
    }
    public final String getLatestAuthor() {
        return latestAuthor.get();
    }
    public final void setLatestAuthor(String value) {
        latestAuthor.set(value);
    }

    
    /**
     * Defines the set of tags associated with the log entry.
     */
    private final ReadOnlySetWrapper<String> tags = new ReadOnlySetWrapper<>(this, TAGS_PROP, FXCollections.observableSet(new TreeSet<>()));
    
    public final ReadOnlySetProperty<String> tagsProperty() {
        return tags.getReadOnlyProperty();
    }
    public final ObservableSet<String> getTags() {
        return tags.get();
    }
    
    
    public static enum DetailLevel {
        BIG_PICTURE,
        HIGHLIGHT,
        DETAIL,
    }
    
    /**
     * Defines the level of detail the log entry represents.
     */
    private final ObjectProperty<DetailLevel> detailLevel = new SimpleObjectProperty<>(this, DETAIL_LEVEL_PROP, DetailLevel.HIGHLIGHT);
    
    public final ObjectProperty<DetailLevel> detailLevelProperty() {
        return detailLevel;
    }
    public final DetailLevel getDetailLevel() {
        return detailLevel.get();
    }
    public final void setDetailLevel(DetailLevel value) {
        detailLevel.set(value);
    }
    
    
    /**
     * Defines the body content of the log entry. The format is determined by the bodyFormat
     * property.
     */
    private final ReadOnlyStringWrapper body = new ReadOnlyStringWrapper(this, BODY_PROP);

    public final ReadOnlyStringProperty bodyProperty() {
        return body.getReadOnlyProperty();
    }
    public final String getBody() {
        return body.get();
    }


    /**
     * The text formatting we support.
     */
    public static enum Format {
        /**
         * The fail-safe format, text is presented as-is.
         */
        PLAIN_TEXT,
        
        /**
         * The text is styled with our pseudo-HTML tags. This is the default.
         */
        STYLED_TEXT,
    }
    
    
    /**
     * Defines the format type of the body content of the log entry.
     */
    private final ReadOnlyObjectWrapper<Format> bodyFormat = new ReadOnlyObjectWrapper<>(this, BODY_FORMAT_PROP, Format.STYLED_TEXT);
    
    public final ReadOnlyObjectProperty<Format> bodyFormatProperty() {
        return bodyFormat.getReadOnlyProperty();
    }
    public Format getBodyFormat() {
        return bodyFormat.get();

    }
    
    
    /**
     * Sets the body content of the log entry.
     * @param format    The format type of the body content.
     * @param value The text of the body content.
     */
    public void setBody(Format format, String value) {
        this.bodyFormat.set(format);
        this.body.set(value);
    }
    
    
    public static Callback<LogEntry, Observable[]> extractor() {
        return (LogEntry logEntry) -> new Observable [] { 
            logEntry.titleProperty(),
            logEntry.timePeriodProperty(),
            logEntry.zoneIdProperty(),
            logEntry.latestAuthorProperty(),
            logEntry.detailLevelProperty(),
            logEntry.tagsProperty(),
            logEntry.bodyFormatProperty(),
            logEntry.bodyProperty(),
        };
    }
    
    
    /**
     * Constructor.
     * @param guid  The GUID string, if <code>null</code> a random GUID will be generated.
     * @param timePeriod   The time period, if <code>null</code> {@link TimePeriod#now() } is called
     * to obtain the time period.
     * @param zoneId The zone id to associate with the start of the log entry, if
     * <code>null</code> {@link ZoneId#systemDefault() } is called to obtain the zone id.
     */
    public LogEntry(String guid, TimePeriod timePeriod, ZoneId zoneId) {
        if (guid == null) {
            guid = UUID.randomUUID().toString();
        }
        this.guid = guid;
        
        if (timePeriod == null) {
            timePeriod = TimePeriod.now();
        }
        
        this.timePeriod.set(timePeriod);
        this.zoneId.set(zoneId);
        
        this.timePeriod.addListener((property, oldValue, newValue)-> {
            fireLogEntryChanged();
        });
        this.zoneId.addListener((value)-> {
            fireLogEntryChanged();
        });
        this.title.addListener((value)-> {
            fireLogEntryChanged();
        });
        this.latestAuthor.addListener((value)-> {
            fireLogEntryChanged();
        });
        this.tags.addListener((SetChangeListener.Change<? extends String> change) -> {
            fireLogEntryChanged();
        });
        this.detailLevel.addListener((value) -> {
            fireLogEntryChanged();
        });

        // Since the body format is only changed when the body itself is changed
        // we don't need a listener for bodyFormat.
        this.body.addListener((value)-> {
            fireLogEntryChanged();
        });
    }
    
    /**
     * Empty constructor.
     */
    public LogEntry() {
        this(null, null, null);
    }
    
    
    /**
     * Copy constructor. The GUID is copied.
     * @param other The log entry to be copied.
     */
    public LogEntry(LogEntry other) {
        this(other.getGuid(), other.getTimePeriod(), other.getZoneId());
        copyFrom(other);
    }
    
    /**
     * Pseudo-copy constructor, except an explicit guid is assigned.
     * @param guid  The GUID to use.
     * @param other The log entry to be copied.
     */
    public LogEntry(String guid, LogEntry other) {
        this(guid, other.getTimePeriod(), other.getZoneId());
        copyFrom(other);
    }
    

    /**
     * Creates an instance of LogEntry based upon the key-values in a JSON object.
     * @param jsonObject    The JSON object.
     * @return The log entry.
     * @throws JSONException    if a required key is missing or invalid.
     */
    public static LogEntry fromJSON(JSONObject jsonObject) throws JSONException {
        String guid = jsonObject.getString(GUID_KEY);
        int version = 0;
        if (jsonObject.has(VERSION_KEY)) {
            version = jsonObject.getInt(VERSION_KEY);
        }
        
        TimePeriod timePeriod = TimePeriod.fromJSON(jsonObject, TIME_PERIOD_PROP);        
        ZoneId zoneId = TimePeriod.optZoneIdFromJSON(jsonObject, ZONE_ID_PROP, null);
        if (version == 0) {
            if (!timePeriod.isFullDays() && timePeriod.isFullDays(zoneId)) {
                timePeriod = TimePeriod.fromEdgeDates(timePeriod.getLocalStartDate(zoneId), 
                    timePeriod.getLocalEndDate(zoneId).minusDays(1));
            }
        }
        
        LogEntry logEntry = new LogEntry(guid, timePeriod, zoneId);
        
        logEntry.setTitle(jsonObject.optString(TITLE_PROP, null));
        logEntry.setLatestAuthor(jsonObject.optString(LATEST_AUTHOR_PROP, null));
        
        JSONArray tags = jsonObject.getJSONArray(TAGS_PROP);
        JSONUtil.arrayToSet(tags, logEntry.tags.get());
        
        String detailLevelText = jsonObject.optString(DETAIL_LEVEL_PROP, DetailLevel.HIGHLIGHT.toString());
        DetailLevel detailLevel;
        try {
            detailLevel = DetailLevel.valueOf(detailLevelText);
        }
        catch (RuntimeException ex) {
            detailLevel = DetailLevel.HIGHLIGHT;
        }
        logEntry.setDetailLevel(detailLevel);
        
        String bodyFormatText = jsonObject.optString(BODY_FORMAT_PROP, Format.PLAIN_TEXT.toString());
        String body = jsonObject.optString(BODY_PROP, null);
        if (body != null) {
            Format bodyFormat;
            try {
                bodyFormat = Format.valueOf(bodyFormatText);
            }
            catch (RuntimeException ex) {
                bodyFormat = Format.PLAIN_TEXT;
            }
            
            logEntry.setBody(bodyFormat, body);
        }
        
        return logEntry;
    }
    
    
    /**
     * Writes the state of the log entry to a JSON object.
     * @param jsonObject The JSON object to write to.
     */
    public void toJSON(JSONObject jsonObject) {
        jsonObject.put(GUID_KEY, this.guid);
//        jsonObject.put(VERSION_KEY, 1);
        
        this.timePeriod.get().toJSON(jsonObject, TIME_PERIOD_PROP);
        TimePeriod.zoneIdToJSON(jsonObject, ZONE_ID_PROP, this.zoneId.get());
        jsonObject.put(TITLE_PROP, this.title.get());
        jsonObject.put(LATEST_AUTHOR_PROP, this.latestAuthor.get());
        jsonObject.put(TAGS_PROP, this.tags.get());
        jsonObject.put(DETAIL_LEVEL_PROP, this.detailLevel.get());
        jsonObject.put(BODY_FORMAT_PROP, this.bodyFormat.get());
        jsonObject.put(BODY_PROP, this.body.get());
    }
    
    
    /**
     * Creates a {@link JSONObject} and writes the state of the log entry to it.
     * @return The JSON object.
     */
    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        toJSON(jsonObject);
        return jsonObject;
    }
    
    
    /**
     * Copies all but the Guid of another log entry into this one.
     * @param other The log entry to be copied.
     */
    public void copyFrom(LogEntry other) {
        if (this == other) {
            return;
        }
        
        ++this.listenerDisableCount;
        try {
            setTimePeriod(other.getTimePeriod());
            setZoneId(other.getZoneId());
            setTitle(other.getTitle());
            setLatestAuthor(other.getLatestAuthor());
            setDetailLevel(other.getDetailLevel());

            this.tags.get().clear();
            this.tags.get().addAll(other.getTags());

            setBody(other.getBodyFormat(), other.getBody());
        }
        finally {
            --this.listenerDisableCount;
            if (this.listenerDisableCount == 0) {
                fireLogEntryChanged();
            }
        }
    }
    
    
    /**
     * @return The unique identifier of the log entry.
     */
    public final String getGuid() {
        return this.guid;
    }

    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.guid);
        hash = 79 * hash + Objects.hashCode(this.timePeriod.get());
        hash = 79 * hash + Objects.hashCode(this.zoneId.get());
        hash = 79 * hash + Objects.hashCode(this.title.get());
        hash = 79 * hash + Objects.hashCode(this.latestAuthor.get());
        hash = 79 * hash + Objects.hashCode(this.tags.get());
        hash = 79 * hash + Objects.hashCode(this.detailLevel.get());
        hash = 79 * hash + Objects.hashCode(this.bodyFormat.get());
        hash = 79 * hash + Objects.hashCode(this.body.get());
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
        final LogEntry other = (LogEntry) obj;
        if (!Objects.equals(this.guid, other.guid)) {
            return false;
        }
        if (!Objects.equals(this.timePeriod.get(), other.timePeriod.get())) {
            return false;
        }
        if (!Objects.equals(this.zoneId.get(), other.zoneId.get())) {
            return false;
        }
        if (!Objects.equals(this.title.get(), other.title.get())) {
            return false;
        }
        if (!Objects.equals(this.latestAuthor.get(), other.latestAuthor.get())) {
            return false;
        }
        if (!Objects.equals(this.tags.get(), other.tags.get())) {
            return false;
        }
        if (!Objects.equals(this.detailLevel.get(), other.detailLevel.get())) {
            return false;
        }
        if (!Objects.equals(this.bodyFormat.get(), other.bodyFormat.get())) {
            return false;
        }
        if (!Objects.equals(this.body.get(), other.body.get())) {
            return false;
        }
        return true;
    }
    
    
    public static boolean zonesEqual(ZoneId a, ZoneId b) {
        if (a == null) {
            return b == null;
        }
        else if (b == null) {
            return false;
        }
        return a.equals(b);
    }
    
    void updateTimeSettings(TimePeriod timePeriod, ZoneId zoneId) {
        if (this.timePeriod.get().equals(timePeriod) && zonesEqual(this.zoneId.get(), zoneId)) {
            return;
        }
        
        ++this.listenerDisableCount;
        try {
            this.timePeriod.set(timePeriod);
            this.zoneId.set(zoneId);
        }
        finally {
            --this.listenerDisableCount;
        }
        
        if (this.listenerDisableCount == 0) {
            fireLogEntryChanged();
        }
    }
    
    /**
     * Sets the start and end times of the log entry using local date/times.
     * @param dateTimeA One of the date/times.
     * @param dateTimeB The other of the date/times.
     * @param zoneId    The zone id to use, if <code>null</code> then the zone id
     * returned by {@link ZoneId#systemDefault} will be used.
     */
    public final void setStartEndTime(LocalDateTime dateTimeA, LocalDateTime dateTimeB, ZoneId zoneId) {
        if (zoneId == null) {
            zoneId = ZoneId.systemDefault();
        }
        
        updateTimeSettings(TimePeriod.fromEdgeTimes(dateTimeA, dateTimeB, zoneId), zoneId);
    }
    
    /**
     * Sets the start and end times of the log entry using date/times in the system default time zone.
     * @param dateTimeA One of the date/times.
     * @param dateTimeB The other of the date/times.
     */
    public final void setStartEndTime(LocalDateTime dateTimeA, LocalDateTime dateTimeB) {
        setStartEndTime(dateTimeA, dateTimeB, null);
    }
        
    
    /**
     * Sets the time period represented by the entry to a specific local date/time.
     * The duration is set to zero.
     * @param dateTime  The date time to set.
     * @param zoneId The zone id to use, if <code>null</code> then {@link ZoneId#systemDefault} will
     * be called.
     */
    public final void setDateTime(LocalDateTime dateTime, ZoneId zoneId) {
        if (zoneId == null) {
            zoneId = ZoneId.systemDefault();
        }
        
        updateTimeSettings(TimePeriod.fromEdgeTimes(dateTime, dateTime, zoneId), zoneId);
    }
    
    /**
     * Sets the time period represented by the entry to a date/time in the system default time zone.
     * The duration is set to 0.
     * @param dateTime The date/time.
     */
    public final void setDateTime(LocalDateTime dateTime) {
        setDateTime(dateTime, null);
    }
    
    /**
     * Sets the time period represented by the entry to a specific date. The duration
     * is set to the entire day.
     * @param date  The date to set.
     * @param zoneId The zone id to use, if <code>null</code> then {@link ZoneId#systemDefault} will
     * be called.
     */
    public final void setDate(LocalDate date, ZoneId zoneId) {
        if (zoneId == null) {
            zoneId = ZoneId.systemDefault();
        }
        
        updateTimeSettings(TimePeriod.fromEdgeDates(date, date), zoneId);
    }

    /**
     * Sets the time period represented by the entry to a date in the system default time zone.
     * The duration is set to the entire day.
     * @param date The date to set.
     */
    public final void setDate(LocalDate date) {
        setDate(date, null);
    }
    
    
    /**
     * Retrieves a string of text that can be used to represent the log entry in a heading.
     * @param useDateIfEmpty    If true, the time period is converted to a string and returned
     * if no other text could be found.
     * @return The text.
     */
    public final String getHeadingText(boolean useDateIfEmpty) {
        String text = getTitle();
        if (!TextUtil.isAnyText(text)) {
            String firstLine = StyledText.getFirstTextSentence(getBody());
            if (firstLine != null) {
                return firstLine;
            }
            
            if (useDateIfEmpty) {
                text = this.timePeriod.get().toString();
            }
            else {
                text = "";
            }
        }
        return text;
    }
    
    
    /**
     * Determines if the entry has any significant content (title, body)
     * @return <code>true</code> if it does.
     */
    public final boolean isAnyContent() {
        if (TextUtil.isAnyText(getTitle())) {
            return true;
        }
        if (TextUtil.isAnyText(getBody())) {
            return true;
        }
        
        return false;
    }
    
    
    /**
     * Comparator that orders on the guid.
     */
    public static class GuidComparator implements Comparator <LogEntry> {

        @Override
        public int compare(LogEntry o1, LogEntry o2) {
            return o1.guid.compareTo(o2.guid);
        }
        
    }
    
    
    /**
     * Comparator that orders log entries by the start time of the time period then guid.
     */
    public static class StartComparator implements Comparator <LogEntry> {

        @Override
        public int compare(LogEntry o1, LogEntry o2) {
            int result = timePeriodComparator.compare(o1.timePeriod.get(), o2.timePeriod.get());
            if (result != 0) {
                return result;
            }
            
            return o1.guid.compareTo(o2.guid);
        }
        
        final TimePeriod.StartComparator timePeriodComparator = new TimePeriod.StartComparator();
    }
    
    
    /**
     * Comparator that orders log entries by the end time of the time period then guid.
     * <ul>
     *  <li>end of the time period
     *  <li>guid
     * </ul>
     */
    public static class EndComparator implements Comparator <LogEntry> {

        @Override
        public int compare(LogEntry o1, LogEntry o2) {
            int result = timePeriodComparator.compare(o1.timePeriod.get(), o2.timePeriod.get());
            if (result != 0) {
                return result;
            }
            
            return o1.guid.compareTo(o2.guid);
        }
        
        final TimePeriod.EndComparator timePeriodComparator = new TimePeriod.EndComparator();
    }


    /**
     * A helper object for sorting log entries by time period.
     */
    public static class TimePeriodKey implements Comparable<TimePeriodKey> {
        final String guid;
        final TimePeriod timePeriod;
        
        public TimePeriodKey(final String guid, final TimePeriod timePeriod) {
            this.guid = guid;
            this.timePeriod = timePeriod;
        }
        
        public TimePeriodKey(final LogEntry logEntry) {
            this.guid = logEntry.guid;
            this.timePeriod = logEntry.getTimePeriod();
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + Objects.hashCode(this.guid);
            hash = 97 * hash + Objects.hashCode(this.timePeriod);
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
            final TimePeriodKey other = (TimePeriodKey) obj;
            if (!Objects.equals(this.guid, other.guid)) {
                return false;
            }
            if (!Objects.equals(this.timePeriod, other.timePeriod)) {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(TimePeriodKey o) {
            int result = this.timePeriod.compareTo(o.timePeriod);
            if (result != 0) {
                return result;
            }
            return this.guid.compareTo(o.guid);
        }
        
    }
    
    
    /**
     * Comparator that orders {@link TimePeriodKey}s by the start time of the time period.
     */
    public static class TimePeriodStartComparator implements Comparator <TimePeriodKey> {

        @Override
        public int compare(TimePeriodKey o1, TimePeriodKey o2) {
            int result = timePeriodComparator.compare(o1.timePeriod, o2.timePeriod);
            if (result != 0) {
                return result;
            }
            
            return o1.guid.compareTo(o2.guid);
        }
        
        final TimePeriod.StartComparator timePeriodComparator = new TimePeriod.StartComparator();
    }
    
    
    /**
     * Comparator that orders {@link TimePeriodKey}s by the end time of the time period.
     */
    public static class TimePeriodEndComparator implements Comparator <TimePeriodKey> {

        @Override
        public int compare(TimePeriodKey o1, TimePeriodKey o2) {
            int result = timePeriodComparator.compare(o1.timePeriod, o2.timePeriod);
            if (result != 0) {
                return result;
            }
            
            return o1.guid.compareTo(o2.guid);
        }
        
        final TimePeriod.EndComparator timePeriodComparator = new TimePeriod.EndComparator();
    }
    
    
    /**
     * Interface for anyone that wants to know when the entry is modified.
     */
    public interface Listener {
        /**
         * Called whenever a log entry is modified. The calls may be consolidated
         * when several related properties are modified together.
         * @param logEntry The log entry that's calling this.
         */
        public void logEntryChanged(LogEntry logEntry);
    }
    
    /**
     * Adds a listener.
     * @param listener The listener to add.
     */
    public final void addListener(Listener listener) {
        this.listeners.add(listener);
    }
    
    /**
     * Removes a listener.
     * @param listener The listener to remove.
     */
    public final void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }
    
    
    /**
     * Helper that calls {@link Listener#logEntryChanged(leeboardslog.data.LogEntry)  } for
     * each listener if the listeners have not been disabled.
     */
    protected void fireLogEntryChanged() {
        if ((this.listenerDisableCount <= 0) && !this.listeners.isEmpty()) {
            this.listeners.forEach((listener)-> {
                listener.logEntryChanged(this);
            });
        }
    }
    
}
