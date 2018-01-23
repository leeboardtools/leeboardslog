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

import com.leeboardtools.util.JSONUtil;
import com.leeboardtools.util.TimePeriod;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
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
    public static final String TIME_PERIOD_PROP = "timePeriod";
    public static final String ZONE_ID_PROP = "zoneId";
    public static final String TITLE_PROP = "title";
    public static final String LATEST_AUTHOR_PROP = "latestAuthor";
    public static final String TAGS_PROP = "tags";
    public static final String CONTENT_HTML_BODY_TEXT_PROP = "contentHTMLBodyText";
    
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
    private final ObjectProperty<ZoneId> zoneId = new SimpleObjectProperty<>(this, ZONE_ID_PROP, ZoneId.systemDefault());

    public final ObjectProperty<ZoneId> zoneId() {
        return this.zoneId;
    }
    public final ZoneId getZoneId() {
        return this.zoneId.get();
    }
    public final void setZoneId(ZoneId value) {
        if (value == null) {
            value = ZoneId.systemDefault();
        }
        this.zoneId.set(value);
    }
    


    /**
     * Defines the title of the log entry.
     */
    private final StringProperty title = new SimpleStringProperty(this, TITLE_PROP);

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
    private final SetProperty<String> tags = new SimpleSetProperty<>(this, TAGS_PROP, FXCollections.observableSet(new HashSet<>()));
    
    public final SetProperty<String> tagsProperty() {
        return tags;
    }
    public final ObservableSet<String> getTags() {
        return tags.get();
    }
    
    
    /**
     * Defines the content of the log entry as HTML text that appears
     * within a &lt;body&gt; element, the &lt;body&gt; element is not included.
     * @return The content property.
     */
    private final StringProperty contentHTMLBodyText = new SimpleStringProperty(this, CONTENT_HTML_BODY_TEXT_PROP);

    public final StringProperty contentHTMLBodyText() {
        return contentHTMLBodyText;
    }
    public final String getContentHTMLBodyText() {
        return contentHTMLBodyText.get();
    }
    public final void setContentHTMLBodyText(String value) {
        contentHTMLBodyText.set(value);
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
        if (zoneId == null) {
            zoneId = ZoneId.systemDefault();
        }
        
        this.timePeriod.set(timePeriod);
        this.zoneId.set(zoneId);
        
        this.timePeriod.addListener((value)-> {
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

        this.contentHTMLBodyText.addListener((value)-> {
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
     * Creates an instance of LogEntry based upon the key-values in a JSON object.
     * @param jsonObject    The JSON object.
     * @return The log entry.
     * @throws JSONException    if a required key is missing or invalid.
     */
    public static LogEntry fromJSON(JSONObject jsonObject) throws JSONException {
        String guid = jsonObject.getString(GUID_KEY);
        TimePeriod timePeriod = TimePeriod.fromJSON(jsonObject, TIME_PERIOD_PROP);
        ZoneId zoneId = TimePeriod.optZoneIdFromJSON(jsonObject, ZONE_ID_PROP, null);
        LogEntry logEntry = new LogEntry(guid, timePeriod, zoneId);
        
        logEntry.setTitle(jsonObject.optString(TITLE_PROP, null));
        logEntry.setLatestAuthor(jsonObject.optString(LATEST_AUTHOR_PROP, null));
        
        JSONArray tags = jsonObject.getJSONArray(TAGS_PROP);
        JSONUtil.arrayToSet(tags, logEntry.tags.get());
        
        logEntry.setContentHTMLBodyText(jsonObject.optString(CONTENT_HTML_BODY_TEXT_PROP, null));
        
        return logEntry;
    }
    
    
    /**
     * Writes the state of the log entry to a JSON object.
     * @param jsonObject The JSON object to write to.
     */
    public void toJSON(JSONObject jsonObject) {
        jsonObject.put(GUID_KEY, this.guid);
        this.timePeriod.get().toJSON(jsonObject, TIME_PERIOD_PROP);
        TimePeriod.zoneIdToJSON(jsonObject, ZONE_ID_PROP, this.zoneId.get());
        jsonObject.put(TITLE_PROP, this.title.get());
        jsonObject.put(LATEST_AUTHOR_PROP, this.latestAuthor.get());
        jsonObject.put(TAGS_PROP, this.tags.get());
        jsonObject.put(CONTENT_HTML_BODY_TEXT_PROP, this.contentHTMLBodyText.get());
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
     * @return The unique identifier of the log entry.
     */
    public final String getGuid() {
        return this.guid;
    }
    
    
    /**
     * Helper that returns the LogEntry given an observable passed to a property event
     * listener.
     * @param observable
     * @return The LogEntry if observable is in fact a LogEntry, <code>null</code> otherwise.
     */
    public static LogEntry fromObservable(Observable observable) {
        if (observable instanceof ReadOnlyProperty) {
            Object bean = ((ReadOnlyProperty)observable).getBean();
            if (bean instanceof LogEntry) {
                return (LogEntry)bean;
            }
        }
        return null;
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
        hash = 79 * hash + Objects.hashCode(this.contentHTMLBodyText.get());
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
        if (!Objects.equals(this.contentHTMLBodyText.get(), other.contentHTMLBodyText.get())) {
            return false;
        }
        return true;
    }
    
    
    void updateTimeSettings(TimePeriod timePeriod, ZoneId zoneId) {
        if (this.timePeriod.get().equals(timePeriod) && this.zoneId.get().equals(zoneId)) {
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
        
        updateTimeSettings(TimePeriod.fromEdgeDates(date, date, zoneId), zoneId);
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
     * @return The text.
     */
    public final String getHeadingText() {
        String text = getTitle();
        if ((text == null) || text.isEmpty()) {
            // TODO Get the first line of ContentHTMLBodyText()...
            text = this.timePeriod.toString();
        }
        return text;
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
     * Helper that calls {@link Listener#logEntryChanged(leeboardslog.data.LogEntry) } for
     * each listener if the listeners have not been disabled.
     */
    protected void fireLogEntryChanged() {
        if (this.listenerDisableCount <= 0) {
            this.listeners.forEach((listener)-> {
                listener.logEntryChanged(this);
            });
        }
    }
    
}
