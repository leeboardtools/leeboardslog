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

import com.leeboardtools.util.ChangeId;
import com.leeboardtools.util.ChangeId.Tracker;
import com.leeboardtools.util.TimePeriod;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.ReadOnlyMapWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//
// TODO: Put together an ObservableLogBookMap implements ObservableMap<LocalDate, List<String>>
// 

/**
 *
 * @author Albert Santos
 */
public class LogBook {
    public static final String NAME_PROP = "name";
    public static final String ACTIVE_AUTHOR_PROP = "activeAuthor";
    public static final String CURRENT_ZONE_ID_PROP = "currentZoneId";
    public static final String LOG_ENTRIES_KEY = "logEntries";
    
    private final StringProperty name = new SimpleStringProperty(this, NAME_PROP);
    private final StringProperty activeAuthor = new SimpleStringProperty(this, ACTIVE_AUTHOR_PROP, "");
    
    private final ObjectProperty<ZoneId> currentZoneId = new SimpleObjectProperty<>(this, CURRENT_ZONE_ID_PROP);
    
    private final Map<String, EntryMaster> masterLogEntries = new HashMap<>();
    private final SortedMap<LogEntry.TimePeriodKey, EntryMaster> entriesByStart = new TreeMap<>(new LogEntry.TimePeriodStartComparator());
    private final SortedMap<LogEntry.TimePeriodKey, EntryMaster> entriesByEnd = new TreeMap<>(new LogEntry.TimePeriodEndComparator());
    private final ObservableMap<LocalDate, SortedMap<LogEntry.TimePeriodKey, LogEntry>> entriesByDate = FXCollections.observableHashMap();
    private final ReadOnlyMapWrapper<LocalDate, SortedMap<LogEntry.TimePeriodKey, LogEntry>> readOnlyEntriesByDate 
            = new ReadOnlyMapWrapper<>(FXCollections.unmodifiableObservableMap(entriesByDate));
    
    private final List<Listener> listeners = new ArrayList<>();

    private final ChangeId changeId = new ChangeId();
    
    /**
     * Default constructor.
     */
    public LogBook() {
        this(null, null);
    }
    
    
    /**
     * Constructor
     * @param name  The name of the log book. 
     * @param zoneId    The zone id, may be <code>null</code>
     */
    public LogBook(String name, ZoneId zoneId) {
        if (name != null) {
            this.name.setValue(name);
        }
        
        this.name.addListener((observable)-> {
            markChanged();
        });
        this.activeAuthor.addListener((observable)-> {
            markChanged();
        });
        
        this.currentZoneId.addListener((observable) -> {
            markChanged();
        });
    }
    
    
    /**
     * Creates a LogBook instance based upon the key-value pairs in a JSON object.
     * @param jsonObject    The JSON object.
     * @return The log book.
     * @throws JSONException    if a required key is missing or invalid.
     */
    public static LogBook fromJSON(JSONObject jsonObject) throws JSONException {
        String name = jsonObject.optString(NAME_PROP, null);
        ZoneId currentZoneId = TimePeriod.optZoneIdFromJSON(jsonObject, CURRENT_ZONE_ID_PROP, null);
        JSONArray jsonLogEntries = jsonObject.getJSONArray(LOG_ENTRIES_KEY);
        
        LogBook logBook = new LogBook(name, currentZoneId);
        
        int length = jsonLogEntries.length();
        LogEntry [] logEntries = new LogEntry[length];
        for (int i = 0; i < length; ++i) {
            JSONObject jsonLogEntry = jsonLogEntries.getJSONObject(i);
            logEntries[i] = LogEntry.fromJSON(jsonLogEntry);
        }
        
        logBook.addLogEntries(logEntries);
        
        return logBook;
    }
    
    /**
     * Writes the state of the log book to a JSON object.
     * @param jsonObject The JSON object to write to.
     */
    public void toJSON(JSONObject jsonObject) {
        jsonObject.put(NAME_PROP, this.name.get());
        TimePeriod.zoneIdToJSON(jsonObject, CURRENT_ZONE_ID_PROP, this.currentZoneId.get());
        
        JSONArray logEntries = new JSONArray();
        this.masterLogEntries.forEach((key, masterEntry) -> {
            logEntries.put(masterEntry.logEntry.toJSONObject());
        });
        
        jsonObject.put(LOG_ENTRIES_KEY, logEntries);
    }
    
    /**
     * Creates a {@link JSONObject} and writes the state of the log book to it.
     * A log book with the same state can be recreated from the JSON object with
     * {@link LogBook#fromJSON(org.json.JSONObject) }.
     * @return The JSON object.
     */
    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        toJSON(jsonObject);
        return jsonObject;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.name.get());
        hash = 83 * hash + Objects.hashCode(this.currentZoneId.get());
        hash = 83 * hash + Objects.hashCode(this.activeAuthor.get());
        
        for (Map.Entry<String, EntryMaster> entry : this.masterLogEntries.entrySet()) {
            hash = 83 * hash + Objects.hashCode(entry.getValue().logEntry);
        }
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
        final LogBook other = (LogBook) obj;
        if (!Objects.equals(this.name.get(), other.name.get())) {
            return false;
        }
        if (!Objects.equals(this.activeAuthor.get(), other.activeAuthor.get())) {
            return false;
        }
        if (!Objects.equals(this.currentZoneId.get(), other.currentZoneId.get())) {
            return false;
        }
        
        if (this.masterLogEntries.size() != other.masterLogEntries.size()) {
            return false;
        }
        
        for (Map.Entry<String, EntryMaster> entry : this.masterLogEntries.entrySet()) {
            EntryMaster otherEntryMaster = other.masterLogEntries.get(entry.getKey());
            if (otherEntryMaster == null) {
                return false;
            }
            if (!entry.getValue().logEntry.equals(otherEntryMaster.logEntry)) {
                return false;
            }
        }

        return true;
    }
    
    /**
     * Closes the log book. The log book should not be accessed after this is called.
     */
    public void closeLogBook() {
        this.masterLogEntries.forEach((key, value) -> {
            value.logEntry.removeListener(value);
        });
        this.masterLogEntries.clear();
        this.entriesByStart.clear();
        this.entriesByEnd.clear();
    }
    

    /**
     * Marks the log book as having been changed.
     */
    public void markChanged() {
        this.changeId.markChanged();
    }
    
    
    /**
     * Creates an instance of {@link Tracker} for tracking changes
     * to the log book or any of its contents.
     * @return The tracker.
     */
    public ChangeId.Tracker newChangeIdTracker() {
        return this.changeId.newTracker();
    }
    
    
    /**
     * @return The value of the name property.
     */
    public final String getName() {
        return name.get();
    }
    
    /**
     * Sets the value of the name property.
     * @param value The value to set.
     */
    public final void setName(String value) {
        name.set(value);
    }
    
    /**
     * Defines the name of the log book.
     * @return The name property.
     */
    public final StringProperty nameProperty() {
        return name;
    }
    
    
    /**
     * @return The value of the activeAuthor property.
     */
    public final String getActiveAuthor() {
        return activeAuthor.get();
    }
    
    /**
     * Sets the value of the activeAuthor property.
     * @param value The value to set.
     */
    public final void setActiveAuthor(String value) {
        activeAuthor.set(value);
    }
    
    /**
     * Defines the author that's actively editing the log book.
     * @return The activeAuthor property.
     */
    public final StringProperty activeAuthorProperty() {
        return activeAuthor;
    }
    

    /**
     * @return The value of the currentZone property.
     */
    public ZoneId getCurrentZoneId() {
        return currentZoneId.get();
    }

    /**
     * Sets the value of the currentZone property.
     * @param value The value to set.
     */
    public void setCurrentZoneId(ZoneId value) {
        currentZoneId.set(value);
    }

    /**
     * Defines the zone id used to convert between entry time instances and local date/times.
     * @return The zone id property.
     */
    public ObjectProperty currentZoneIdProperty() {
        return currentZoneId;
    }
    
    
    /**
     * @return The number of {@link LogEntry} objects in the book.
     */
    public final int getLogEntryCount() {
        return this.masterLogEntries.size();
    }
    
    
    /**
     * Retrieves a modifiable list of the log entries sorted by increasing start time.
     * The list is a new copy and may be modified freely.
     * @return The list of log entries.
     */
    public final List<LogEntry> getLogEntriesByStart() {
        List<LogEntry> logEntries = new ArrayList<>(this.entriesByStart.size());
        this.entriesByStart.forEach((key, value) -> {
            logEntries.add(value.logEntry);
        });
        return logEntries;
    }
    
    /**
     * Retrieves a modifiable list of the log entries sorted by increasing end time.
     * The list is a new copy and may be modified freely.
     * @return The list of log entries.
     */
    public final List<LogEntry> getLogEntriesByEnd() {
        List<LogEntry> logEntries = new ArrayList<>(this.entriesByEnd.size());
        this.entriesByEnd.forEach((key, value) -> {
            logEntries.add(value.logEntry);
        });
        return logEntries;
    }
    
    /**
     * Retrieves a collection containing all the log entries.
     * @param logEntries    The collection to receive the log entries, if <code>null</code> a
     * {@link TreeSet} with a comparator of {@link LogEntry.StartComparator} will be used.
     * @return The collection containing the entries.
     */
    public final Collection<LogEntry> getLogEntries(Collection<LogEntry> logEntries) {
        if (logEntries == null) {
            logEntries = new TreeSet<>(new LogEntry.StartComparator());
        }
        
        final Collection<LogEntry> destEntries = logEntries;
        this.masterLogEntries.forEach((key, value)-> {
            destEntries.add(value.logEntry);
        });
        return logEntries;
    }
    
    
    /**
     * Retrieves the log entries whose time period intersects a given time period.
     * @param timePeriod    The time period of interest.
     * @param logEntries    The collection to receive the log entries, if <code>null</code> a
     * {@link TreeSet} with a comparator of {@link LogEntry.StartComparator} will be used.
     * @return The collection contains the log entries.
     */
    public final Collection<LogEntry> getLogEntriesInTimePeriod(TimePeriod timePeriod,
            Collection<LogEntry> logEntries) {
        if (logEntries == null) {
            logEntries = new TreeSet<>(new LogEntry.StartComparator());
        }
        
        // Start with the first log entry whose time period ends within the desired time period.
        final TimePeriod startTimePeriod = TimePeriod.fromEdgeTimes(timePeriod.getStartInstant(), timePeriod.getStartInstant());
        final LogEntry.TimePeriodKey startKey = new LogEntry.TimePeriodKey("", startTimePeriod);
        final SortedMap<LogEntry.TimePeriodKey, EntryMaster> validEndKeys = this.entriesByEnd.tailMap(startKey);
        
        // And then we're stuck just going through the list...
        
        final Collection<LogEntry> destEntries = logEntries;
        
        validEndKeys.forEach((key, entryMaster) -> {
            TimePeriod.Overlap overlap = entryMaster.logEntry.getTimePeriod().getOverlap(timePeriod);
            switch (overlap) {
                case OTHER_START :
                case OTHER_END :
                case ENCLOSES_OTHER :
                case INSIDE_OTHER :
                    destEntries.add(entryMaster.logEntry);
                    break;
            }
        });
        
        return logEntries;
    }
    
    /**
     * Retrieves the log entries whose time period includes a given date.
     * @param date  The local date, the currentZoneId property value is used for the zone.
     * @param logEntries    The collection to receive the log entries, if <code>null</code> a
     * {@link TreeSet} with a comparator of {@link LogEntry.StartComparator} will be used.
     * @return The collection contains the log entries.
     */
    public final Collection<LogEntry> getLogEntriesWithDate(LocalDate date, Collection<LogEntry> logEntries) {
        if (logEntries == null) {
            logEntries = new TreeSet<>(new LogEntry.StartComparator());
        }
        
        final Collection<LogEntry> destEntries = logEntries;
        SortedMap<LogEntry.TimePeriodKey, LogEntry> entriesMap = this.entriesByDate.get(date);
        if (entriesMap != null) {
            entriesMap.forEach((key, value) -> { 
                destEntries.add(value);
            });
        }
        return logEntries;
    }
    
    
    /**
     * @return The value of the entriesByDate property.
     */
    public final ObservableMap<LocalDate, SortedMap<LogEntry.TimePeriodKey, LogEntry>> getEntriesByDate() {
        return readOnlyEntriesByDate.getReadOnlyProperty().get();
    }
    
    /**
     * Defines a read-only map whose keys are the local dates of all log entries and whose values
     * are sets containing the log entries that are partly or entirely in the key date.
     * Note that the sets must not be modified.
     * @return The entriesByDate property.
     */
    public final ReadOnlyMapProperty<LocalDate, SortedMap<LogEntry.TimePeriodKey, LogEntry>> entriesByDateProperty() {
        return readOnlyEntriesByDate.getReadOnlyProperty();
    }
    
    
    /**
     * This is used to hold the log entries in the master table. It's used to keep
     * track of the timePeriod used to build the sorted lists, so when the time period
     * is changed, we can update the sorted lists accordingly.
     */
    private class EntryMaster implements LogEntry.Listener {
        final LogEntry logEntry;
        LogEntry.TimePeriodKey timePeriodKey;
        final Set<LocalDate> localDates = new HashSet<>();
        
        EntryMaster(LogEntry logEntry) {
            this.logEntry = logEntry;
            this.timePeriodKey = new LogEntry.TimePeriodKey(logEntry);
            this.timePeriodKey.timePeriod.getDates(this.localDates, getCurrentZoneId());
        }
        
        boolean equals(EntryMaster other) {
            // We use '==' for the logEntry because we want the same object instance...
            return (other != null)
                    && (this.logEntry == other.logEntry)
                    && this.timePeriodKey.equals(other.timePeriodKey);
        }

        @Override
        public void logEntryChanged(LogEntry logEntry) {
            entryMasterLogEntryChanged(this);
        }
    }
    
    private void entryMasterLogEntryChanged(EntryMaster entryMaster) {
        markChanged();
        
        final LogEntry logEntry = entryMaster.logEntry;
        if (!entryMaster.timePeriodKey.timePeriod.equals(logEntry.getTimePeriod())) {
            removeEntryMaster(entryMaster);
            entryMaster.timePeriodKey = new LogEntry.TimePeriodKey(logEntry);
            addEntryMaster(entryMaster);
        }
    }
    
    private void addEntryMaster(EntryMaster entryMaster) {
        this.entriesByStart.put(entryMaster.timePeriodKey, entryMaster);
        this.entriesByEnd.put(entryMaster.timePeriodKey, entryMaster);
        
        entryMaster.localDates.forEach((date) -> {
            SortedMap<LogEntry.TimePeriodKey, LogEntry> mapEntry = this.entriesByDate.get(date);
            if (mapEntry == null) {
                mapEntry = new TreeMap<>();
                this.entriesByDate.put(date, mapEntry);
            }
            mapEntry.put(entryMaster.timePeriodKey, entryMaster.logEntry);
        });
    }
    
    private void removeEntryMaster(EntryMaster entryMaster) {
        this.entriesByStart.remove(entryMaster.timePeriodKey);
        this.entriesByEnd.remove(entryMaster.timePeriodKey);
        
        entryMaster.localDates.forEach((date)-> {
            SortedMap<LogEntry.TimePeriodKey, LogEntry> mapEntry = this.entriesByDate.get(date);
            if (mapEntry != null) {
                mapEntry.remove(entryMaster.timePeriodKey);
                if (mapEntry.isEmpty()) {
                    this.entriesByDate.remove(date);
                }
            }
        });
    }
    
    
    /**
     * Adds a single log entry. Log entries are uniquely identified by their {@link LogEntry#guid},
     * if a log entry passed to this has a guid that matches one already in the log book,
     * the existing log entry instance is replaced with the new one.
     * @param entry The log entry to add.
     */
    public void  addLogEntry(LogEntry entry) {
        addLogEntries(new LogEntry[] { entry });
    }
    
    /**
     * Adds an array of log entries. Log entries are uniquely identified by their {@link LogEntry#guid},
     * if a log entry passed to this has a guid that matches one already in the log book,
     * the existing log entry instance is replaced with the new one.
     * @param entries The array of entries.
     */
    public void addLogEntries(LogEntry [] entries) {
        List<LogEntry> entriesAdded = new ArrayList<>();
        List<LogEntry> entriesRemoved = new ArrayList<>();
        for (LogEntry logEntry : entries) {
            EntryMaster entryMaster = new EntryMaster(logEntry);
            EntryMaster prevEntryMaster = this.masterLogEntries.get(logEntry.getGuid());
            if (entryMaster.equals(prevEntryMaster)) {
                continue;
            }
            
            this.masterLogEntries.put(logEntry.getGuid(), entryMaster);
            logEntry.addListener(entryMaster);
            
            if (prevEntryMaster == null) {
                // Simple new entry.
                entriesAdded.add(logEntry);
            }
            else {
                // Replacing an existing entry.
                removeEntryMaster(prevEntryMaster);

                // Note that the entryMaster.equals() test above should guarantee
                // the log entry objects are not the same at this point.
                entriesRemoved.add(prevEntryMaster.logEntry);
                entriesAdded.add(logEntry);
            }
            
            addEntryMaster(entryMaster);
        }
        
        if (!entriesRemoved.isEmpty()) {
            this.listeners.forEach((listener) -> {
                listener.entriesRemoved(this, entriesRemoved);
            });
        }
        if (!entriesAdded.isEmpty()) {
            this.listeners.forEach((listener) -> {
                listener.entriesAdded(this, entriesAdded);
            });
        }
    }
    
    /**
     * Removes a group of log entries identified via an array of their guids.
     * @param guids The array of guids.
     */
    public void removeLogEntries(String [] guids) {
        List<LogEntry> entriesRemoved = new ArrayList<>();
        for (String guid : guids) {
            EntryMaster entryMaster = this.masterLogEntries.remove(guid);
            if (entryMaster != null) {
                entryMaster.logEntry.removeListener(entryMaster);
                removeEntryMaster(entryMaster);
                entriesRemoved.add(entryMaster.logEntry);
            }
        }
        
        if (!entriesRemoved.isEmpty()) {
            this.listeners.forEach((listener) -> {
                listener.entriesRemoved(this, entriesRemoved);
            });
        }
    }
    
    /**
     * Removes the log entry with a given guid.
     * @param guid The guid of the log entry.
     */
    public void removeLogEntry(String guid) {
        removeLogEntries(new String [] { guid });
    }
    
    /**
     * Removes log entries in an array.
     * @param logEntries The array of log entries.
     */
    public void removeLogEntries(LogEntry [] logEntries) {
        String [] guids = new String[logEntries.length];
        for (int i = 0; i < logEntries.length; ++i) {
            guids[i] = logEntries[i].getGuid();
        }
        removeLogEntries(guids);
    }
    
    /**
     * Removes a log entry.
     * @param logEntry The log entry to remove.
     */
    public void removeLogEntry(LogEntry logEntry) {
        removeLogEntries(new String[] { logEntry.getGuid() });
    }
    
    
    /**
     * Interface for listeners to the log book.
     */
    public interface Listener {
        /**
         * Called whenever a {@link LogEntry} object is added to the book. Note that
         * this means the individual objects.
         * @param logBook   The log book calling this.
         * @param logEntries    The collection of log entry objects that were added.
         */
        public void entriesAdded(LogBook logBook, Collection<LogEntry> logEntries);
        
        /**
         * Called whenever a {@link LogEntry} object is removed from the book. Note that
         * this means the individual objects. If a new object with the same id as an existing
         * object is added to the book, this will be called for the old entry object.
         * @param logBook   The log book calling this.
         * @param logEntries    The collection of log entry objects that were removed.
         */
        public void entriesRemoved(LogBook logBook, Collection<LogEntry> logEntries);
    }
    
    
    /**
     * Adds a listener.
     * @param listener  The listener to add.
     */
    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }
    
    /**
     * Removes a listener.
     * @param listener The listener to remove.
     */
    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }
    
    
}
