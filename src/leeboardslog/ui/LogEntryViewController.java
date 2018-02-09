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
package leeboardslog.ui;

import com.leeboardtools.control.TimePeriodEditController;
import com.leeboardtools.text.StyledTextEditor;
import com.leeboardtools.util.ResourceSource;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;
import leeboardslog.data.LogBook;
import leeboardslog.data.LogEntry;

/**
 * FXML Controller class
 *
 * @author Albert Santos
 */
public class LogEntryViewController implements Initializable {
    @FXML
    private BorderPane mainPane;
    @FXML
    private TextField titleEditor;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private ComboBox<String> startTimePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private ComboBox<String> endTimePicker;
    @FXML
    private ChoiceBox<String> timeZonePicker;
    @FXML
    private ComboBox<LogEntry.DetailLevel> levelPicker;
    @FXML
    private TextField tagsEditor;
    @FXML
    private MenuButton tagsMenuButton;
    
    
    private TimePeriodEditController timePeriodEditController;
    private StyledTextEditor bodyEditor;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        timePeriodEditController = new TimePeriodEditController(startDatePicker, startTimePicker, 
                endDatePicker, endTimePicker, timeZonePicker);
        
        this.timePeriodEditController.timePeriodProperty().addListener((property, oldValue, newValue) -> {
            if (this.logEntry != null) {
                this.logEntry.setTimePeriod(newValue);
            }
        });
        
        this.timePeriodEditController.zoneIdProperty().addListener((property, oldValue, newValue) -> {
            if (this.logEntry != null) {
                this.logEntry.setZoneId(newValue);
            }
        });
        
        this.levelPicker.setConverter(new StringConverter<LogEntry.DetailLevel>() {
            @Override
            public String toString(LogEntry.DetailLevel object) {
                switch (object) {
                    case BIG_PICTURE :
                        return ResourceSource.getString("Misc.bigPictureDetailLevel");
                    case HIGHLIGHT :
                        return ResourceSource.getString("Misc.highlightDetailLevel");
                    case DETAIL :
                        return ResourceSource.getString("Misc.detailDetailLevel");
                    default :
                        return null;
                }
            }

            @Override
            public LogEntry.DetailLevel fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        this.levelPicker.getItems().add(LogEntry.DetailLevel.BIG_PICTURE);
        this.levelPicker.getItems().add(LogEntry.DetailLevel.HIGHLIGHT);
        this.levelPicker.getItems().add(LogEntry.DetailLevel.DETAIL);
        this.levelPicker.setEditable(false);
        this.levelPicker.valueProperty().addListener((property, oldValue, newValue) -> {
            if (this.logEntry != null) {
                this.logEntry.setDetailLevel(newValue);
            }
        });
        
        this.bodyEditor = new StyledTextEditor();
        this.mainPane.setCenter(this.bodyEditor);
    }    

    @FXML
    private void onSave(ActionEvent event) {
        if ((this.logEntryView != null) && (this.logEntry != null)) {
            updateLogEntryFromControls();
            this.logEntryView.closeView(true);
        }
    }

    @FXML
    private void onDelete(ActionEvent event) {
        if (this.logEntryView != null) {
            this.logEntryView.deleteLogEntry();
        }
    }
    
    
    LogEntryView logEntryView;
    LogEntry logEntry;
    LogBook logBook;
    
    void setLogEntryView(LogEntryView logEntryView) {
        this.logEntryView = logEntryView;
    }
    
    void setupController(LogEntry logEntry, LogBook logBook) {
        if ((this.logEntry == logEntry) && (this.logBook == logBook)) {
            return;
        }
        
        this.logBook = logBook;
        this.logEntry = logEntry;
        if (this.logEntry != null) {
            this.titleEditor.setDisable(false);
            this.titleEditor.setText(this.logEntry.getTitle());
            
            this.timePeriodEditController.setTimePeriod(this.logEntry.getTimePeriod());
            this.timePeriodEditController.setZoneId(this.logEntry.getZoneId());
            
            this.levelPicker.setDisable(false);
            this.levelPicker.setValue(this.logEntry.getDetailLevel());
            
            this.tagsEditor.setDisable(false);
            this.tagsEditor.setText(tagsToString(logEntry));
            
            this.tagsMenuButton.setDisable(false);
            this.logBook.tagsInUseProperty().addListener((property, oldValue, newValue) -> {
                updateTagsInUseMenu();
            });
            updateTagsInUseMenu();
            
            this.bodyEditor.setDisable(false);
            this.bodyEditor.setStyledText(this.logEntry.getBody());
        }
        else {
            this.titleEditor.setDisable(true);
            this.titleEditor.setText("");

            this.timePeriodEditController.setTimePeriod(null);
            this.timePeriodEditController.setZoneId(null);
            
            this.levelPicker.setDisable(true);
            this.levelPicker.setValue(null);
            
            this.tagsEditor.setDisable(true);
            this.tagsEditor.setText("");
            
            this.tagsMenuButton.setDisable(true);
            
            this.bodyEditor.setDisable(true);
            this.bodyEditor.setStyledText("");
        }
    }
    
    void updateTagsInUseMenu() {
        if ((this.logBook == null) || (this.tagsMenuButton == null)) {
            return;
        }
        
        ObservableList<MenuItem> menuItems = this.tagsMenuButton.getItems();
        menuItems.clear();
        this.logBook.getTagsInUse().forEach((tag) -> {
            MenuItem menuItem = new MenuItem(tag);
            menuItem.setOnAction((event) -> {
                insertTag(tag);
            });
            menuItems.add(menuItem);
        });
    }
    
    void insertTag(String tag) {
        SortedSet<String> tags = stringToTags(this.tagsEditor.getText());
        tags.add(tag);
        this.tagsEditor.setText(tagsToString(tags));
    }
    
    public static String tagsToString(Set<String> tags) {
        StringBuilder builder = new StringBuilder();
        tags.forEach((tag) -> {
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(tag);
        });
        return builder.toString();
    }
    
    public static SortedSet<String> stringToTags(String text) {
        String [] tags = text.split(",");
        for (int i = 0; i < tags.length; ++i) {
            tags[i] = tags[i].trim();
        }
        
        TreeSet<String> tagsSet = new TreeSet<>(Arrays.asList(tags));
        tagsSet.remove("");
        return tagsSet;
    }
    
    public static String tagsToString(LogEntry logEntry) {
        return tagsToString(logEntry.getTags());
    }
    
    public static void stringToTags(LogEntry logEntry, String text) {
        logEntry.getTags().clear();
        logEntry.getTags().addAll(stringToTags(text));
    }
    
    void updateLogEntryFromControls() {
        this.logEntry.setTitle(this.titleEditor.getText());
        
        stringToTags(this.logEntry, this.tagsEditor.getText());
        
        // TODO: Make sure the contents are up-to-date.
        this.logEntry.setBody(LogEntry.Format.STYLED_TEXT, this.bodyEditor.getStyledText());
        
    }
}
