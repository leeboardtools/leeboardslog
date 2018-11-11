/*
 * Copyright 2018 albert.
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
package leeboardslog.ui;

import com.leeboardtools.text.StyledTextEditor;
import com.leeboardtools.util.TimePeriod;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.StackPane;
import leeboardslog.data.LogBookFile;
import leeboardslog.data.LogEntry;

/**
 * What features do I want to add to this???
 * Show all content
 * Hide all content
 * 
 * @author albert
 */
public class EntryListView extends StackPane implements LogBookEditor.Listener {
    private LogBookEditor logBookEditor;
    private ObjectProperty<LocalDate> activeDateProperty;
    
    private ListView<LogEntry> listView;
    private final ChangeListener<LogBookFile> logBookFileListener = (property, oldValue, newValue) -> {
        if (newValue != null) {
            listView.setItems(newValue.getLogBook().getLogEntriesByStart());
        }
        else {
            listView.setItems(FXCollections.emptyObservableList());
        }
    };
    
    
    private DateTimeFormatter dateOnlyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd  ");
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm a  ");
    
    public EntryListView() {
        this.listView = new ListView<>();
        this.listView.setPrefWidth(USE_COMPUTED_SIZE);
        this.listView.setPrefHeight(USE_COMPUTED_SIZE);
        this.listView.setMaxWidth(Double.MAX_VALUE);
        this.listView.setMaxHeight(Double.MAX_VALUE);
        
        this.listView.setCellFactory((ListView<LogEntry> lv) -> {
            return new EntryCell();
        });
        
        this.setPrefWidth(USE_COMPUTED_SIZE);
        this.setPrefHeight(USE_COMPUTED_SIZE);
        this.setMaxWidth(Double.MAX_VALUE);
        this.setMaxHeight(Double.MAX_VALUE);
        
        this.getChildren().add(this.listView);
    }
    
    public void setupView(LogBookEditor logBookEditor, ObjectProperty<LocalDate> activeDateProperty) {
        if (this.logBookEditor != logBookEditor) {
            if (this.logBookEditor != null) {
                this.logBookEditor.removeListener(this);
                this.logBookEditor.logBookFileProperty().removeListener(this.logBookFileListener);
            }
            
            this.logBookEditor = logBookEditor;
            
            if (this.logBookEditor != null) {
                this.logBookEditor.logBookFileProperty().addListener(this.logBookFileListener);
                this.listView.setItems(this.logBookEditor.getLogBook().getLogEntriesByStart());
                this.logBookEditor.addListener(this);
                
            }
        }
        
        if (this.activeDateProperty != activeDateProperty) {
            this.activeDateProperty = activeDateProperty;
            
            if (this.activeDateProperty != null) {
                this.activeDateProperty.addListener((property, oldValue, newValue) -> {
                    updateFromActiveDate();
                });
            }
        }
        
        updateFromActiveDate();
    }
    
    void updateFromActiveDate() {
        LocalDate activeDate = (this.activeDateProperty == null) ? null : this.activeDateProperty.get();
        if ((activeDate != null) && (logBookEditor != null) && (logBookEditor.getLogBook() != null)) {
            LogEntry tmpLogEntry = new LogEntry();
            tmpLogEntry.setDate(this.activeDateProperty.get());
            ObservableList<LogEntry> logEntries = logBookEditor.getLogBook().getLogEntriesByStart();
            int index = Collections.binarySearch(logEntries, tmpLogEntry, new LogEntry.StartComparator());
            // Result of binarySearch: index = -insertionPoint - 1
            // Therefore: insertionPoint = -(index + 1)
            if (index < 0) {
                index = -(index + 1);
            }
            if (index >= 0) {
                listView.scrollTo(index);
                listView.getFocusModel().focus(index);
            }
        }
    }
    
    public boolean canDelete() {
        ObservableList<LogEntry> selectedLogEntries = this.listView.getSelectionModel().getSelectedItems();
        return !selectedLogEntries.isEmpty();
    }
    
    public void deleteSelectedEntries() {
        ObservableList<LogEntry> selectedLogEntries = this.listView.getSelectionModel().getSelectedItems();
        if (selectedLogEntries.isEmpty()) {
            return;
        }
        
        this.logBookEditor.deleteLogEntries(selectedLogEntries.toArray(new LogEntry[selectedLogEntries.size()]));
    }

    @Override
    public boolean canCloseLogBookEditor(LogBookEditor editor) {
        return true;
    }

    @Override
    public void logBookEditorClosing(LogBookEditor editor) {
        this.entryCells.forEach((cell)-> {
            cell.commitChanges();
        });
        this.entryCells.clear();
        
        this.listView.setItems(null);
    }

    @Override
    public void logEntryViewClosed(LogBookEditor editor, LogEntry logEntry) {        
    }
    
    
    private final List<EntryCell> entryCells = new ArrayList<>();
    
    public class EntryCell extends ListCell<LogEntry> {
        private final TitledPane titledPane = new TitledPane();
        
        private final StyledTextEditor textEditor = new StyledTextEditor();
        
        public EntryCell() {
            this.titledPane.setContent(this.textEditor);
            this.titledPane.setPrefWidth(USE_COMPUTED_SIZE);
            this.titledPane.setMaxWidth(Double.MAX_VALUE);

            this.textEditor.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
            this.textEditor.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            
            this.textEditor.setOnAction((event) -> {
                commitChanges();
            });
            
            this.setOnMouseClicked((event)-> {
                if (event.getClickCount() == 2) {
                    openEditWindow();
                }
            });
            
            setGraphic(this.titledPane);
            
            entryCells.add(this);
        }
        
        public void openEditWindow() {
            if (logBookEditor != null) {
                LogEntryView view = logBookEditor.getLogEntryView(getItem().getGuid());
                if (view != null) {
                    view.showView();
                }
            }
        }
        
        public void commitChanges() {
            if ((getItem() != null) && (logBookEditor != null) && (logBookEditor.getLogBook() != null)) {
                LogEntry workingLogEntry = new LogEntry(getItem());
                workingLogEntry.setBody(LogEntry.Format.STYLED_TEXT, textEditor.getStyledText());
                logBookEditor.updateLogEntry(getItem().getGuid(), workingLogEntry);
            }
        }

        @Override
        protected void updateItem(LogEntry item, boolean empty) {
            //commitChanges();
            
            super.updateItem(item, empty);
            
            if ((item == null) || empty) {
                this.titledPane.setText("");
                this.titledPane.setVisible(false);
            }
            else {
                String timeStamp;
                TimePeriod timePeriod = item.getTimePeriod();
                
                if (timePeriod.isFullDays()) {
                    timeStamp = timePeriod.getFirstFullDay().format(dateOnlyFormatter);
                    if (timePeriod.getFullDayCount() > 1) {
                        timeStamp += timePeriod.getLastFullDay().format(dateOnlyFormatter);
                    }
                }
                else {
                    ZoneId zoneId = logBookEditor.getLogBook().getCurrentZoneId();
                    LocalDateTime dateTime = timePeriod.getLocalStartDateTime(zoneId);
                    if (timePeriod.isFullDays(zoneId)) {
                        timeStamp = dateTime.format(dateOnlyFormatter);
                        if (timePeriod.getDuration().toDays() > 1) {
                            LocalDate endDate = timePeriod.getLocalEndDate(zoneId);
                            timeStamp += endDate.format(dateOnlyFormatter);
                        }
                    }
                    else {
                        timeStamp = dateTime.format(dateTimeFormatter);
                    }
                }
                this.titledPane.setText(timeStamp + item.getHeadingText(false));
                this.titledPane.setVisible(true);
                this.titledPane.setExpanded(false);
                
                this.textEditor.setStyledText(item.getBody());
            }
        }
        
    }
}
