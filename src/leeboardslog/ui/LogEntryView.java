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

import com.leeboardtools.dialog.PromptDialog;
import com.leeboardtools.text.TextUtil;
import com.leeboardtools.util.ResourceSource;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import leeboardslog.data.LogEntry;

/**
 * A view for editing a {@link LogEntry}. For now the view presumes it is the only one editing
 * a log entry and therefore doesn't worry about updates to the log entry outside of the view.
 * @author Albert Santos
 */
public class LogEntryView {
    private LogEntry logEntry;
    private LogEntry workingLogEntry;
    private LogBookEditor logBookEditor;
    private boolean isChanged = false;
    private Stage viewStage;
    private LogEntryViewController viewController;
    private final List<Listener> listeners = new ArrayList<>();
    
    
    /**
     * Constructor, only called by {@link LogBookEditor}.
     * @param logEntry  The log entry to be edited.
     * @param logBookEditor The editor calling this.
     */
    protected LogEntryView(LogEntry logEntry, LogBookEditor logBookEditor) {
        this.logEntry = logEntry;
        this.workingLogEntry = new LogEntry(logEntry.getGuid(), logEntry.getTimePeriod(), logEntry.getZoneId());
        this.workingLogEntry.copyFrom(logEntry);
        this.logBookEditor = logBookEditor;
        
        this.workingLogEntry.addListener((LogEntry entry) -> {
            isChanged = true;
        });
    }
    
    
    /**
     * @return The log entry being edited.
     */
    public final LogEntry getLogEntry() {
        return logEntry;
    }
    
    
    /**
     * @return Determines if any changes have been made to the log entry that have yet
     * to be saved.
     */
    public boolean isChanges() {
        if (this.viewController != null) {
            this.viewController.updateLogEntryFromControls();
        }
        
        return isChanged  && !this.logEntry.equals(this.workingLogEntry);
    }
    
    /**
     * Saves any changes that have been made to the log entry.
     */
    public void saveChanges() {
        if (this.isChanged) {
            this.logBookEditor.updateLogEntry(this.logEntry.getGuid(), this.workingLogEntry);
            this.isChanged = false;
        }
    }
    
    
    /**
     * Closes the view, prompting to save changes if there are any unsaved changes.
     * @return <code>true</code> if the view was closed, <code>false</code> if there were
     * changes and the user canceled saving changes.
     */
    public boolean safeCloseView() {
        boolean saveChanges = false;
        
        if (isChanges()) {
            PromptDialog promptDialog = new PromptDialog();
            promptDialog.setTitle(ResourceSource.getString("Title.saveChanges"));

            promptDialog.addMessage(ResourceSource.getString("Prompt.saveLogEntryChanges"));

            promptDialog.addButton(ResourceSource.getString("Button.yes"), PromptDialog.BTN_YES);
            promptDialog.addButton(ResourceSource.getString("Button.no"), PromptDialog.BTN_NO);
            promptDialog.addButton(ResourceSource.getString("Button.cancel"), PromptDialog.BTN_CANCEL);
            promptDialog.setDefaultButtonId(PromptDialog.BTN_YES);
            promptDialog.setCancelButtonId(PromptDialog.BTN_CANCEL);

            switch (promptDialog.showSimpleDialog(this.viewStage)) {
                case PromptDialog.BTN_YES :
                    saveChanges = true;
                    break;
                    
                case PromptDialog.BTN_NO :
                    saveChanges = false;
                    break;

                case PromptDialog.BTN_CANCEL :
                    return false;
            }
        }
        
        closeView(saveChanges);
        return true;
    }
    
    /**
     * Closes the view. Once the view has been closed it cannot be used.
     * @param saveChanges   If <code>true</code> any changes are saved.
     */
    public void closeView(boolean saveChanges) {
        if (saveChanges) {
            if (!this.workingLogEntry.isAnyContent()) {
                this.logBookEditor.getLogBook().removeLogEntry(this.logEntry);
            }
            else {
                saveChanges();
            }
        }
        
        this.listeners.forEach((listener) -> { 
            listener.viewClosed(this);
        });
        this.listeners.clear();
        
        this.logEntry = null;
        this.workingLogEntry = null;
        this.logBookEditor = null;
        
        if (this.viewStage != null) {
            Stage stage = this.viewStage;
            this.viewStage = null;
            stage.close();
        }
    }
    
    
    public void deleteLogEntry() {
        PromptDialog promptDialog = new PromptDialog();
        promptDialog.setTitle(ResourceSource.getString("Title.confirmDelete"));

        promptDialog.addMessage(ResourceSource.getString("Prompt.confirmDelete"));

        promptDialog.addButton(ResourceSource.getString("Button.delete"), PromptDialog.BTN_YES);
        promptDialog.addButton(ResourceSource.getString("Button.cancel"), PromptDialog.BTN_CANCEL);
        
        switch (promptDialog.showOptionsDialog(this.viewStage)) {
            case PromptDialog.BTN_YES :
                break;
                
            case PromptDialog.BTN_CANCEL :
                return;
        }
        
        this.logBookEditor.getLogBook().removeLogEntry(this.logEntry);
        
        closeView(false);
    }
    
    
    public void showView() {
        if (this.viewStage == null) {
            setupStage();
        }
        
        if (this.viewStage != null) {
            this.viewStage.show();
            this.viewStage.toFront();
            this.viewStage.requestFocus();
        }
    }
    
    protected void setupStage() {
        try {
            this.viewStage = new Stage();

            URL location = LogEntryViewController.class.getResource("LogEntryView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(location, ResourceSource.getBundle());
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add("leeboardslog/Styles.css");
            
            this.viewController = (LogEntryViewController)fxmlLoader.getController();
            this.viewController.setLogEntryView(this);

            this.viewStage.setTitle(getViewTitle());
            this.viewController.setLogEntry(this.workingLogEntry);
            
            this.viewStage.setScene(scene);
            this.viewStage.setOnCloseRequest((event)-> {
                if (!safeCloseView()) {
                    event.consume();
                }
            });
            
            this.isChanged = false;
        } catch (IOException ex) {
            Logger.getLogger(LogEntryView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String getViewTitle() {
        String title = this.workingLogEntry.getTitle();
        if (TextUtil.isAnyText(title)) {
            title = this.workingLogEntry.getTimePeriod().toString();
        }
        return title;
    }
    
    
    public interface Listener {
        public void viewClosed(LogEntryView view);
    }
    
    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }
    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }
}
