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
import com.leeboardtools.dialog.PromptDialog;
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
import leeboardslog.data.DayLogEntries;
import com.leeboardtools.util.ListConverter;
import com.leeboardtools.util.ResourceSource;
import com.leeboardtools.util.TimePeriod;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javax.swing.undo.UndoManager;
import leeboardslog.data.LogBook;
import leeboardslog.data.LogBookFile;
import leeboardslog.data.LogEntry;


/**
 * The controller for the monthly view...
 * @author Albert Santos
 */
public class LogBookViewController implements Initializable {
    private final static Logger LOG = Logger.getLogger(LogBookViewController.class.getName());
    
    @FXML
    private Spinner<LocalDate> month;
    @FXML
    private Spinner<LocalDate> year;
    @FXML
    private TextField searchText;
    @FXML
    private StackPane mainPane;
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
    @FXML
    private CheckMenuItem autoSaveMenuItem;
    

    private MonthlyView<DayLogEntries> monthlyViewControl;
    private EntryListView entryListViewControl;
    
    private LogBookEditor logBookEditor;
    private Stage stage;
    
    private final ObjectProperty<LocalDate> activeDate = new SimpleObjectProperty<>(this, "activeDate", LocalDate.now());
    @FXML
    private MenuItem undoMenuItem;
    @FXML
    private MenuItem redoMenuItem;
    
    public final LocalDate getActiveDate() {
        return activeDate.get();
    }
    public final void setActiveDate(LocalDate date) {
        activeDate.set(date);
    }
    public final ObjectProperty<LocalDate> activeDateProperty() {
        return activeDate;
    }

    @FXML
    private void onNewLogBook(ActionEvent event) {
        if (this.logBookEditor != null) {
            try {
                this.logBookEditor.promptNewLogBook(this.stage);
            } catch (LogBookFile.FileException ex) {
                LOG.log(Level.SEVERE, null, ex);
                PromptDialog.showOKDialog(stage, ex.getLocalizedMessage(), ResourceSource.getString("Title.severeError"));
            }
        }
    }

    @FXML
    private void onOpenLogBook(ActionEvent event) {
        if (this.logBookEditor != null) {
            try {
                this.logBookEditor.promptOpenLogBook(this.stage);
            } catch (LogBookFile.FileException ex) {
                LOG.log(Level.SEVERE, null, ex);
                PromptDialog.showOKDialog(stage, ex.getLocalizedMessage(), ResourceSource.getString("Title.severeError"));
            }
        }
    }

    @FXML
    private void onSave(ActionEvent event) {
        if (this.logBookEditor != null) {
            this.logBookEditor.saveLogBook();
        }
    }

    @FXML
    private void onAutoSave(ActionEvent event) {
        if (this.logBookEditor != null) {
            this.logBookEditor.setAutoSave(!this.logBookEditor.getAutoSave());
        }
    }

    @FXML
    private void onUndo(ActionEvent event) {
        UndoManager undoManager = (this.logBookEditor != null) ? this.logBookEditor.getUndoManager() : null;
        if (undoManager != null) {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        }
    }

    @FXML
    private void onRedo(ActionEvent event) {
        UndoManager undoManager = (this.logBookEditor != null) ? this.logBookEditor.getUndoManager() : null;
        if (undoManager != null) {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        }
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
        newLogEntry();
    }

    @FXML
    private void onDeleteLogEntry(ActionEvent event) {
        switch (getActiveViewType()) {
            case MONTHLY:
                deleteMonthlyLogEntry();
                break;
            case ENTRY_LIST:
                deleteEntryListLogEntry();
                break;
            case TIME_LINE:
                deleteTimeLineLogEntry();
                break;
            default:
                throw new AssertionError(getActiveViewType().name());
            
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
    
    
    private void newLogEntry() {
        LogBook logBook = this.logBookEditor.getLogBook();
        if (logBook != null) {
            // TODO: Close the primary log entry view...
            TimePeriod timePeriod = TimePeriod.fromEdgeDates(activeDate.get(), activeDate.get(), logBook.getCurrentZoneId());
            LogEntryView view = this.logBookEditor.getViewForNewLogEntry(timePeriod);
            view.showView();
        }
    }
    
    private void deleteMonthlyLogEntry() {
        LogBook logBook = this.logBookEditor.getLogBook();
        if (logBook != null) {
            LocalDate date = this.activeDate.get();
            List<LogEntry> logEntries = new ArrayList<>();
            logBook.getLogEntriesWithDate(date, logEntries);
            
            if (logEntries.size() == 1) {
                // Straight delete...
            }
        }
    }

    private void deleteEntryListLogEntry() {
        this.entryListViewControl.deleteSelectedEntries();
    }

    private void deleteTimeLineLogEntry() {
    }
    
    private void onEditStart(MonthlyView.EditEvent<DayLogEntries> event) {
        LogBook logBook = this.logBookEditor.getLogBook();
        if (logBook != null) {
            LocalDate date = this.activeDate.get();
            List<LogEntry> logEntries = new ArrayList<>();
            logBook.getLogEntriesWithDate(date, logEntries);
            if (logEntries.isEmpty()) {
                newLogEntry();
            }
            else if (logEntries.size() == 1) {
                LogEntryView view = this.logBookEditor.getLogEntryView(logEntries.get(0).getGuid());
                view.showView();
            }
            else {
                // TODO: Need to select which entry to open...
            }
            
            // We're going to go ahead and cancel the edit mode since we're not modal..
            this.monthlyViewControl.cancelEdit();
        }
    }
    
    private void onEditCommit(MonthlyView.EditEvent<DayLogEntries> event) {
    }
    
    private void onEditCancel(MonthlyView.EditEvent<DayLogEntries> event) {
    }
    
    
    public static class DayLogEntriesConverter implements ListConverter<DayLogEntries, String> {
        @Override
        public ObservableList<String> toList(DayLogEntries object) {
            ObservableList<String> strings = FXCollections.observableArrayList();
            if (object != null) {
                object.getLogEntries().forEach((logEntry)-> {
                    String text = logEntry.getHeadingText(false);
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
        this.mainPane.getChildren().add(this.monthlyViewControl);
        
        this.monthlyViewControl.setStringListConverter(new DayLogEntriesConverter());

        this.monthlyViewControl.activeDateProperty().addListener((observable, oldValue, newValue)-> {
            this.activeDate.set(newValue);
        });
        this.activeDate.addListener((observable, oldValue, newValue)-> {
            this.monthlyViewControl.setActiveDate(newValue);
        });
        this.monthlyViewControl.setActiveDate(getActiveDate());
        this.monthlyViewControl.setOnEditStart((event)-> {
            onEditStart(event);
        });
        this.monthlyViewControl.setOnEditCommit((event)-> {
            onEditCommit(event);
        });
        this.monthlyViewControl.setOnEditCancel((event)-> {
            onEditCancel(event);
        });
        
        
        //
        // Entry List View Control...
        this.entryListViewControl = new EntryListView();
        this.mainPane.getChildren().add(this.entryListViewControl);
        
        
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
            
            this.entryListViewControl.setVisible(isEntryList);
            this.entryListMenuItem.setSelected(isEntryList);
            
            this.timeLineViewMenuItem.setSelected(isTimeLine);
        });
        
        this.mainMenuButton.showingProperty().addListener((property, oldValue, newValue)-> {
            if (newValue) {
                updateMenu();
            }
        });
        
        this.activeViewType.set(ViewType.MONTHLY);
        this.activeViewType.set(ViewType.ENTRY_LIST);
    }
    
    
    
    private final LogBook.Listener logBookListener = new LogBook.Listener() {
        @Override
        public void entriesAdded(LogBook logBook, Collection<LogEntry> logEntries) {
            updateFromLogEntries();
        }

        @Override
        public void entriesRemoved(LogBook logBook, Collection<LogEntry> logEntries) {
            updateFromLogEntries();
        }

        @Override
        public void entryModified(LogBook logBook, LogEntry logEntry, LogEntry oldLogEntryValues) {
            if (!logEntry.getTimePeriod().equals(oldLogEntryValues.getTimePeriod())) {
                // We need a full refresh because we need to reload the log entry list..
                updateFromLogEntries();
            }
            else {
                updateFromChangedLogEntry(logEntry);
            }
        }
        
    };
    
    
    void setupController(LogBookEditor logBookEditor, Stage stage) {
        this.stage = stage;
        
        if (this.logBookEditor != logBookEditor) {
            
            this.logBookEditor = logBookEditor;
            
            if (this.logBookEditor != null) {
                this.logBookEditor.logBookFileProperty().addListener(((observable, oldValue, newValue) -> {
                    if (oldValue != null) {
                        // Remove our listener for the log book changes.
                        if (oldValue.getLogBook() != null) {
                            oldValue.getLogBook().removeListener(logBookListener);
                        }
                    }
                    
                    if (newValue != null) {
                        if (newValue.getLogBook() != null) {
                            newValue.getLogBook().addListener(logBookListener);
                        }
                    }
                    updateFromLogEntries();
                }));
                
                if (this.logBookEditor.getLogBook() != null) {
                    this.logBookEditor.getLogBook().addListener(logBookListener);
                }
                
                this.entryListViewControl.setupView(this.logBookEditor, this.activeDate);
                
                updateFromLogEntries();
            }
        }
    }
    
    void updateFromLogEntries() {
        LogBook logBook = null;
        if (this.logBookEditor != null) {
            logBook = this.logBookEditor.getLogBook();
        }
        if (this.monthlyViewControl != null) {
            if (logBook != null) {
                // For now we do a brute force approach, just replace all the items.
                this.monthlyViewControl.setItems(null);
                this.monthlyViewControl.setItems(logBook.getLogEntriesByDate());
            }
            else {
                this.monthlyViewControl.setItems(null);
            }
        }
    }
    
    void updateFromChangedLogEntry(LogEntry logEntry) {
        LogBook logBook = null;
        if (this.logBookEditor != null) {
            logBook = this.logBookEditor.getLogBook();
        }
        if (logBook != null) {
            LocalDate startDate = logEntry.getTimePeriod().getLocalStartDate(logBook.getCurrentZoneId());
            LocalDate endDate = logEntry.getTimePeriod().getAdjustedLocalEndDate(logBook.getCurrentZoneId());
            
            if (this.monthlyViewControl != null) {
                this.monthlyViewControl.reloadDateRange(startDate, endDate);
            }
        }        
    }
    
    void updateMenu() {
        if (this.autoSaveMenuItem != null) {
            this.autoSaveMenuItem.setSelected(this.logBookEditor.getAutoSave());
        }
        
        UndoManager undoManager = (this.logBookEditor != null) ? this.logBookEditor.getUndoManager() : null;
        if (undoManager != null) {
            this.undoMenuItem.setDisable(!undoManager.canUndo());
            this.redoMenuItem.setDisable(!undoManager.canRedo());
        }
        else {
            this.undoMenuItem.setDisable(true);
            this.redoMenuItem.setDisable(true);
        }
        
        updateLogEntriesMenu();
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
