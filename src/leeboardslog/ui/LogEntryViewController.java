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
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import leeboardslog.data.LogEntry;

/**
 * FXML Controller class
 *
 * @author Albert Santos
 */
public class LogEntryViewController implements Initializable {

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
    private ComboBox<?> levelPicker;
    @FXML
    private TextField tagsEditor;
    
    
    private TimePeriodEditController timePeriodEditController;

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
    
    void setLogEntryView(LogEntryView logEntryView) {
        this.logEntryView = logEntryView;
    }
    
    void setLogEntry(LogEntry logEntry) {
        if (this.logEntry == logEntry) {
            return;
        }
        
        this.logEntry = logEntry;
        if (this.logEntry != null) {
            this.titleEditor.setDisable(false);
            this.titleEditor.setText(this.logEntry.getTitle());
            
            this.timePeriodEditController.setTimePeriod(this.logEntry.getTimePeriod());
        }
        else {
            this.titleEditor.setDisable(true);
            this.titleEditor.setText("");

            this.timePeriodEditController.setTimePeriod(null);
        }
    }
    
    
    void updateLogEntryFromControls() {
        this.logEntry.setTitle(this.titleEditor.getText());
        
        // TODO: Make sure the contents are up-to-date.
        // The time period is already up to date.
        
    }
}
