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

import java.time.LocalDate;
import java.util.List;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;

/**
 * This is used by {@link LogBook} to hold the set of log entries that are contained
 * within one day. The LogBook is responsible for managing the entries within the DayLogEntries.
 * @author Albert Santos
 */
public class DayLogEntries {
    /**
     * Defines the {@link LocalDate} represented by this object. Read-only.
     */
    private final ReadOnlyObjectWrapper<LocalDate> localDate = new ReadOnlyObjectWrapper<>(this, "localDate");

    public final ReadOnlyObjectProperty<LocalDate> localDateProperty() {
        return localDate.getReadOnlyProperty();
    }
    public final LocalDate getLocalDate() {
        // LocalDate is immutable, we don't have to call getReadOnlyProperty()...
        return localDate.get();
    }
    

    /**
     * Defines a list of the {@link LogEntry} that have any part within this object's local date.
     */

    final ObservableList<LogEntry> logEntries = FXCollections.observableArrayList(LogEntry.extractor());
    final ReadOnlyListWrapper<LogEntry> readOnlyLogEntries = new ReadOnlyListWrapper<>(this, "logEntries", logEntries);
    
    public final ReadOnlyListProperty<LogEntry> logEntriesProperty() {
        return readOnlyLogEntries.getReadOnlyProperty();
    }
    public final ObservableList<LogEntry> getLogEntries() {
        return readOnlyLogEntries.getReadOnlyProperty().get();
    }
    
    
    public static Callback<DayLogEntries, Observable[]> extractor() {
        return (DayLogEntries entry) -> new Observable [] {
            entry.localDateProperty(),
            entry.logEntriesProperty(),
        };
    }
    
    
    /**
     * Constructor.
     * @param localDate The local date this represents.
     */
    DayLogEntries(final LocalDate localDate) {
        this.localDate.set(localDate);
    }
    
}
