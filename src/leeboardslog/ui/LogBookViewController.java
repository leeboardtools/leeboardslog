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

import com.leeboardtools.control.LocalDateSpinnerValueFactory;
import com.leeboardtools.control.MonthlyView;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import leeboardslog.LeeboardsLog;
import leeboardslog.data.DayLogEntries;
import com.leeboardtools.util.ListConverter;
import com.leeboardtools.util.TimePeriod;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import leeboardslog.data.LogBook;


/**
 * The controller for the monthly view...
 * @author Albert Santos
 */
public class LogBookViewController implements Initializable {
    
    @FXML
    private Spinner<LocalDate> month;
    @FXML
    private Spinner<LocalDate> year;
    @FXML
    private TextField searchText;
    @FXML
    private BorderPane mainPane;
    @FXML
    private CheckMenuItem monthlyViewMenuItem;
    @FXML
    private CheckMenuItem entryListMenuItem;
    @FXML
    private CheckMenuItem timeLineViewMenuItem;
    @FXML
    private MenuButton mainMenuButton;
    @FXML
    private SeparatorMenuItem logEntryViewsSeparator;
    @FXML
    private Menu logEntriesMenu;
    

    private MonthlyView<DayLogEntries> monthlyViewControl;
    private LogBookEditor logBookEditor;
    
    private final ObjectProperty<LocalDate> activeDate = new SimpleObjectProperty<>(this, "activeDate", LocalDate.now());
    
    public final LocalDate getActiveDate() {
        return activeDate.get();
    }
    public final void setActiveDate(LocalDate date) {
        activeDate.set(date);
    }
    public final ObjectProperty<LocalDate> activeDateProperty() {
        return activeDate;
    }
    
    
    public enum ViewType {
        MONTHLY,
        ENTRY_LIST,
        TIME_LINE,
    }
    private final ObjectProperty<ViewType> activeViewType = new SimpleObjectProperty<>(this, "activeViewType", null);
    public final ViewType getActiveViewType() {
        return activeViewType.get();
    }
    public final void setActiveViewType(ViewType viewType) {
        activeViewType.set(viewType);
    }
    public final ObjectProperty<ViewType> activeViewTypeProperty() {
        return activeViewType;
    }
    

    @FXML
    private void onNewLogEntry(ActionEvent event) {
        LogBook logBook = this.logBookEditor.getLogBook();
        if (logBook != null) {
            TimePeriod timePeriod = TimePeriod.fromEdgeDates(activeDate.get(), activeDate.get(), logBook.getCurrentZoneId());
            LogEntryView view = this.logBookEditor.getViewForNewLogEntry(timePeriod);
            view.showView();
        }
    }

    @FXML
    private void onMonthlyView(ActionEvent event) {
        setActiveViewType(ViewType.MONTHLY);
    }

    @FXML
    private void onListView(ActionEvent event) {
        setActiveViewType(ViewType.ENTRY_LIST);
    }

    @FXML
    private void onTimeLineView(ActionEvent event) {
        setActiveViewType(ViewType.TIME_LINE);
    }

    @FXML
    private void onSettings(ActionEvent event) {
    }
    
    
    public static class DayLogEntriesConverter implements ListConverter<DayLogEntries, String> {
        @Override
        public ObservableList<String> toList(DayLogEntries object) {
            ObservableList<String> strings = FXCollections.observableArrayList();
            if (object != null) {
                object.getLogEntries().forEach((logEntry)-> {
                    String text = logEntry.getHeadingText();
                    strings.add(text);
                });
            }
            return strings;
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        //
        // Month Spinner...
        LocalDateSpinnerValueFactory.ByMonth monthValueFactory = new LocalDateSpinnerValueFactory.ByMonth();
        this.month.setValueFactory(monthValueFactory);
        monthValueFactory.valueProperty().addListener((observable, oldValue, newValue)-> {
            LocalDate currentActiveDate = this.activeDate.get();
            if ((newValue.getMonth() != currentActiveDate.getMonth()) || (newValue.getYear() != currentActiveDate.getYear())) {
                // We want to line up the entire month, we'll do that by making sure
                // the first of the month is at the first row.
                LocalDate startOfMonth = newValue.minusDays(newValue.getDayOfMonth() - 1);
                this.monthlyViewControl.makeDateInFirstRow(startOfMonth);
            }
            this.activeDate.set(newValue);
        });
        this.activeDate.addListener((observable, oldValue, newValue)-> {
            monthValueFactory.setValue(newValue);
        });
        
        
        //
        // Year Spinner...
        LocalDateSpinnerValueFactory.ByYear yearValueFactory = new LocalDateSpinnerValueFactory.ByYear();
        this.year.setValueFactory(yearValueFactory);
        yearValueFactory.valueProperty().addListener((observable, oldValue, newValue)-> {
            this.activeDate.set(newValue);
        });
        this.activeDate.addListener((observable, oldValue, newValue)-> {
            yearValueFactory.setValue(newValue);
        });
        
        
        //
        // Month View Control...
        this.monthlyViewControl = new MonthlyView<>();
        this.mainPane.setCenter(this.monthlyViewControl);
        
        this.monthlyViewControl.setStringListConverter(new DayLogEntriesConverter());

        this.monthlyViewControl.activeDateProperty().addListener((observable, oldValue, newValue)-> {
            this.activeDate.set(newValue);
        });
        this.activeDate.addListener((observable, oldValue, newValue)-> {
            this.monthlyViewControl.setActiveDate(newValue);
        });
        this.monthlyViewControl.setActiveDate(getActiveDate());
        
        
        //
        // Log Book Editor...
        this.logBookEditor = LeeboardsLog.getLogBookEditor();
        if (this.logBookEditor != null) {
            this.logBookEditor.logBookFileProperty().addListener(((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    this.monthlyViewControl.setItems(newValue.getLogBook().getEntriesByDate());
                }
            }));
            if (this.logBookEditor.getLogBookFile() != null) {
                this.monthlyViewControl.setItems(this.logBookEditor.getLogBookFile().getLogBook().getEntriesByDate());
            }
        }
        
        this.activeViewType.addListener((property, oldValue, newValue) -> {
            boolean isMonthly = false;
            boolean isEntryList = false;
            boolean isTimeLine = false;
            switch (newValue) {
                case MONTHLY :
                    isMonthly = true;
                    break;
                case ENTRY_LIST :
                    isEntryList = true;
                    break;
                case TIME_LINE :
                    isTimeLine = true;
                    break;
            }
            
            this.monthlyViewControl.setVisible(isMonthly);
            this.monthlyViewMenuItem.setSelected(isMonthly);
            
            this.entryListMenuItem.setSelected(isEntryList);
            
            this.timeLineViewMenuItem.setSelected(isTimeLine);
        });
        
        this.mainMenuButton.showingProperty().addListener((property, oldValue, newValue)-> {
            if (newValue) {
                updateLogEntriesMenu();
            }
        });
        
        this.activeViewType.set(ViewType.MONTHLY);
    }
    
    
    void setLogBookEditor(LogBookEditor logBookEditor) {
        if (this.logBookEditor != logBookEditor) {
            this.logBookEditor = logBookEditor;
            
            if (this.logBookEditor != null) {
                this.logBookEditor.logBookFileProperty().addListener(((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        this.monthlyViewControl.setItems(newValue.getLogBook().getEntriesByDate());
                    }
                }));
                if (this.logBookEditor.getLogBookFile() != null) {
                    this.monthlyViewControl.setItems(this.logBookEditor.getLogBookFile().getLogBook().getEntriesByDate());
                }
            }
        }
    }
    
    
    void updateLogEntriesMenu() {
        // Want to delete everything after the logEntryViewsSeparator in logEntriesMenu.
        ObservableList<MenuItem> menuItems = this.logEntriesMenu.getItems();
        int index = menuItems.indexOf(this.logEntryViewsSeparator);
        if (index >= 0) {
            menuItems.remove(index + 1, menuItems.size());
            
            // Now add an entry for each view.
            LogEntryView [] logEntryViews = this.logBookEditor.getOpenLogEntryViews();
            TreeMap<String, List<LogEntryView>> sortedLogViewEntries = new TreeMap<>();
            for (LogEntryView logEntryView : logEntryViews) {
                String title = logEntryView.getViewTitle();
                List<LogEntryView> theseViews = sortedLogViewEntries.get(title);
                if (theseViews == null) {
                    theseViews = new ArrayList<>();
                    sortedLogViewEntries.put(title, theseViews);
                }
                theseViews.add(logEntryView);
            }
            
            sortedLogViewEntries.forEach((title, views) -> {
                views.forEach((logViewEntry) -> {
                    MenuItem menuItem = new MenuItem(title);
                    menuItem.setOnAction((event)-> {
                        logViewEntry.showView();
                    });
                    menuItems.add(menuItem);
                });
            });
        }
    }
    
}
