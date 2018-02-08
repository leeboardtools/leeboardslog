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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
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
public class EntryListView extends StackPane {
    private LogBookEditor logBookEditor;
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
    
    public void setupView(LogBookEditor logBookEditor) {
        if (this.logBookEditor != logBookEditor) {
            if (this.logBookEditor != null) {
                this.logBookEditor.logBookFileProperty().removeListener(this.logBookFileListener);
            }
            this.logBookEditor = logBookEditor;
            
            if (this.logBookEditor != null) {
                this.logBookEditor.logBookFileProperty().addListener(this.logBookFileListener);
                this.listView.setItems(this.logBookEditor.getLogBook().getLogEntriesByStart());
                
            }
        }
    }
    
    
    
    // What do we want the cell to be???
    // Can we somehow shoehorn DayCell into this?
    // Not really, since a DayCell refers to a MultiDayView.
    // 
    // Date:Time: HeadingText ...
    public class EntryCell extends ListCell<LogEntry> {
        private final TitledPane titledPane = new TitledPane();
        
        private final StyledTextEditor textEditor = new StyledTextEditor();
        
        public EntryCell() {
            this.titledPane.setContent(this.textEditor);
            this.titledPane.setPrefWidth(USE_COMPUTED_SIZE);
            this.titledPane.setMaxWidth(Double.MAX_VALUE);

            this.textEditor.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
            this.textEditor.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            
            setGraphic(this.titledPane);
        }

        @Override
        protected void updateItem(LogEntry item, boolean empty) {
            super.updateItem(item, empty);
            
            if ((item == null) || empty) {
                this.titledPane.setText("");
                this.titledPane.setVisible(false);
            }
            else {
                ZoneId zoneId = logBookEditor.getLogBook().getCurrentZoneId();
                LocalDateTime dateTime = item.getTimePeriod().getLocalStartDateTime(zoneId);
                String timeStamp;
                if (item.getTimePeriod().isFullDays(zoneId)) {
                    timeStamp = dateTime.format(dateOnlyFormatter);
                    if (item.getTimePeriod().getDuration().toDays() > 1) {
                        LocalDate endDate = item.getTimePeriod().getLocalEndDate(zoneId);
                        timeStamp += endDate.format(dateOnlyFormatter);
                    }
                }
                else {
                    timeStamp = dateTime.format(dateTimeFormatter);
                }
                this.titledPane.setText(timeStamp + item.getHeadingText(false));
                this.titledPane.setVisible(true);
                this.titledPane.setExpanded(false);
                
                this.textEditor.setText(item.getBody());
            }
        }
        
    }
}
