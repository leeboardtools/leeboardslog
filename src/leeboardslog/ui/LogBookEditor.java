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
import com.leeboardtools.util.ResourceSource;
import com.leeboardtools.util.TimePeriod;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import leeboardslog.data.LogBook;
import leeboardslog.data.LogBookFile;
import leeboardslog.data.LogEntry;

/**
 * This class serves as a central point for managing the editing of a specific
 * {@link LogBookFile}. It does stuff like:
 * <ul>
 * <li>Track a specific {@link LogBookFile}
 * <li>Put up a dialog box for selecting a log book to edit or open.
 * </ul>
 * @author Albert Santos
 */
public class LogBookEditor {
    private static final Logger LOG = Logger.getLogger(LogBookEditor.class.getName());
    
    public static final String PREFS_PREVIOUS_FILE_NAME = "PreviousFileName";
    public static final String PREFS_ACTIVE_AUTHOR_NAME = "ActiveAuthorName";
    public static final String PREFS_AUTO_SAVE = "AutoSave";
    
    public static final int BTN_NEW_LOG_FILE = 1;
    public static final int BTN_OPEN_LOG_FILE = 2;
    public static final int BTN_EXIT = 3;
    
    public static final String FILE_EXTENSION = "leeboardslog";
    
    String defaultDocSubDir = "LeeboardTools";
    String activeAuthor;
    
    final Preferences preferences;
    
    private final BooleanProperty autoSave = new SimpleBooleanProperty(this, "autoSave", true);

    public final BooleanProperty autoSaveProperty() {
        return autoSave;
    }
    public final boolean getAutoSave() {
        return autoSave.get();
    }
    public final void setAutoSave(boolean value) {
        autoSave.set(value);
    }
    
    
    private final ObjectProperty<UndoManager> undoManager = new SimpleObjectProperty(this, "undoManager", new UndoManager());
    
    public final ObjectProperty<UndoManager> undoManagerProperty() {
        return undoManager;
    }
    public final UndoManager getUndoManager() {
        return undoManager.get();
    }
    public final void setUndoManager(UndoManager value) {
        undoManager.set(value);
    }
    
    
    // The key is the GUID of the log entry.
    final Map<String, LogEntryView> logEntryViewsByGuid = new HashMap<>();
    
    final List<LogBookWindow> logBookWindows = new ArrayList<>();

    final List<Listener> listeners = new ArrayList<>();
    

    /**
     * Defines the log book that's currently being edited.
     */
    private final ReadOnlyObjectWrapper<LogBookFile> logBookFile = new ReadOnlyObjectWrapper<>(this, "logBookFile");

    public final ReadOnlyObjectProperty<LogBookFile> logBookFileProperty() {
        return this.logBookFile.getReadOnlyProperty();
    }
    public final LogBookFile getLogBookFile() {
        return this.logBookFile.get();
    }
    
    
    /**
     * @return The log book being edited, <code>null</code> if none open.
     */
    public final LogBook getLogBook() {
        return (logBookFile.get() == null) ? null : logBookFile.get().getLogBook();
    }
  
    
    public LogBookEditor(Preferences preferences) {
        this.preferences = (preferences == null) ? Preferences.userNodeForPackage(this.getClass()) : preferences;
        
        if (this.preferences != null) {
            this.activeAuthor = this.preferences.get(PREFS_ACTIVE_AUTHOR_NAME, "");
            if (this.activeAuthor.isEmpty()) {
                this.activeAuthor = System.getProperty("user.name");
            }
            
            this.autoSave.set(this.preferences.getBoolean(PREFS_AUTO_SAVE, true));
            this.autoSave.addListener((property, oldValue, newValue) -> {
                if (this.preferences != null) {
                    this.preferences.putBoolean(PREFS_AUTO_SAVE, newValue);
                    try {
                        this.preferences.flush();
                    } catch (BackingStoreException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                }
            });
        }
        
        this.logBookFile.addListener((property, oldValue, newValue) -> {
            UndoManager undoManager = getUndoManager();
            if (undoManager != null) {
                undoManager.discardAllEdits();
            }
        });
    }
    
    protected void generatePromptMessagesForFileException(String prefixId, ArrayList<String> promptMsgs, LogBookFile.FileException ex) {
        promptMsgs.add(ResourceSource.getString(prefixId));
        promptMsgs.add(ex.getFileName());
        
        switch (ex.getReason()) {
            case FILE_NOT_FOUND :
                promptMsgs.add(ResourceSource.getString("Prompt.wasNotFound"));
                break;

            case FILE_IO_ERROR :
            case INVALID_FORMAT :
            default :
                promptMsgs.add(ResourceSource.getString("Prompt.couldNotBeOpened", ex.getLocalizedMessage()));
                break;

        }
    }
    
    
    /**
     * Attempts to open the last log book file based upon the preferences, if
     * that fails this displays a warning, offering the choice of either choosing
     * another log book to open or creating a new log book.
     * @param ownerWindow If not <code>null</code> the owner window.
     * @return  <code>false</code> if the exit was chosen.
     */
    public boolean reopenLastLogBook(Window ownerWindow) {
        ArrayList<String> promptMsgs = new ArrayList<>();
        try {
            if (openLastLogBook(ownerWindow)) {
                return true;
            }
            promptMsgs.add(ResourceSource.getString("Prompt.noLastEditedLogBook_0"));
            promptMsgs.add("");
            promptMsgs.add(ResourceSource.getString("Prompt.noLastEditedLogBook_1"));
        } catch (LogBookFile.FileException ex) {
            generatePromptMessagesForFileException("Prompt.prefixLastEditedLogBook", promptMsgs, ex);
        }
        
        while (true) {
            PromptDialog promptDialog = new PromptDialog();
            promptDialog.setTitle(ResourceSource.getString("Title.application"));
            
            promptMsgs.forEach((msg) -> {
                promptDialog.addMessage(msg);
            });
            promptDialog.addMessage("");
            promptDialog.addMessage(ResourceSource.getString("Prompt.choicesAre"));

            promptDialog.addButton(ResourceSource.getString("Button.newLogFile"), BTN_NEW_LOG_FILE);
            promptDialog.addButton(ResourceSource.getString("Button.openExistingLogFile"), BTN_OPEN_LOG_FILE);
            promptDialog.addButton(ResourceSource.getString("Button.exit"), BTN_EXIT);
        
            try {
                switch (promptDialog.showOptionsDialog(ownerWindow)) {
                    case BTN_NEW_LOG_FILE :
                        if (promptNewLogBook(ownerWindow)) {
                            return true;
                        }
                        break;
                        
                    case BTN_OPEN_LOG_FILE :
                        if (promptOpenLogBook(ownerWindow)) {
                            return true;
                        }
                        break;
                        
                    default :
                        return false;
                }
            }
            catch (LogBookFile.FileException ex) {
                promptMsgs.clear();
                generatePromptMessagesForFileException("Prompt.prefixSelectedLogBook", promptMsgs, ex);
            }
        }
    }
    
    /**
     * Attempts to open the last edited log book file.
     * @param ownerWindow If not <code>null</code> the owner window.
     * @return <code>false</code> if there was no last log book opened.
     * @throws leeboardslog.data.LogBookFile.FileException  Thrown on errors.
     */
    protected boolean openLastLogBook(Window ownerWindow) throws LogBookFile.FileException {
        String previousFileName = this.preferences.get(PREFS_PREVIOUS_FILE_NAME, "");
        if (previousFileName.isEmpty()) {
            return false;
        }

        openLogBook(new File(previousFileName));
        return true;
    }
    
    
    protected FileChooser createFileChooser(File initialFile) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(ResourceSource.getString("ExtensionFilter.logBookFiles"), "*." + FILE_EXTENSION),
                new FileChooser.ExtensionFilter(ResourceSource.getString("ExtensionFilter.allFiles"), "*.*")
        );
        
        boolean isFileSet = false;
        if (initialFile != null) {
            if (initialFile.exists()) {
                isFileSet = true;
                if (initialFile.isDirectory()) {
                    fileChooser.setInitialDirectory(initialFile);
                }
                else {
                    fileChooser.setInitialDirectory(initialFile.getParentFile());
                    fileChooser.setInitialFileName(initialFile.getName());
                }
            }
            else {
                File dir = initialFile.getParentFile();
                if (dir != null) {
                    if (dir.exists()) {
                        isFileSet = true;
                        fileChooser.setInitialDirectory(dir);
                    }
                }
            }
        }
        
        if (!isFileSet) {
            String userDir = System.getProperty("user.home", "");
            if (!userDir.isEmpty()) {
                try {
                    File userDirFile = new File(userDir);
                    if (userDirFile.exists()) {
                        File docDirFile = new File(userDirFile, this.defaultDocSubDir);
                        if (!docDirFile.exists()) {
                            if (docDirFile.mkdir()) {
                                userDirFile = docDirFile;
                            }
                        }
                        else {
                            userDirFile = docDirFile;
                        }
                        fileChooser.setInitialDirectory(userDirFile);
                    }
                }
                catch (SecurityException ex) {
                    // Just ignore this...
                }
            }
        }
        
        return fileChooser;
    }
    
    
    /**
     * Prompts for the name of a new log book file and creates it.
     * @param ownerWindow   The owner window, may be <code>null</code>.
     * @return  <code>true</code> if a new file was created, false if the prompt was canceled.
     * @throws leeboardslog.data.LogBookFile.FileException Thrown on errors.
     */
    public boolean promptNewLogBook(Window ownerWindow) throws LogBookFile.FileException {
        // We need a name for the log book file.
        FileChooser fileChooser = createFileChooser(null);
        
        fileChooser.setTitle(ResourceSource.getString("Title.chooseNewLogBookFileName"));
        fileChooser.setInitialFileName(ResourceSource.getString("Misc.newLogFileName"));
        
        File newFile = fileChooser.showSaveDialog(ownerWindow);
        if (newFile == null) {
            return false;
        }
        
        if (!closeLogBook()) {
            return false;
        }
        
        LogBookFile newLogBookFile = LogBookFile.createLogBookFile(newFile, this.activeAuthor);
        this.logBookFile.set(newLogBookFile);
        this.preferences.put(PREFS_PREVIOUS_FILE_NAME, newFile.getAbsolutePath());
        try {
            this.preferences.flush();
        } catch (BackingStoreException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        
        return true;
    }
    
    
    /**
     * Prompts for the name of an existing log book and opens it.
     * @param ownerWindow   The owner window, may be <code>null</code>.
     * @return  <code>true</code> if a file was opened, false if the prompt was canceled.
     * @throws leeboardslog.data.LogBookFile.FileException Thrown on errors.
     */
    public boolean promptOpenLogBook(Window ownerWindow) throws LogBookFile.FileException {
        LogBookFile currentLogBookFile = this.logBookFile.get();
        File initialFile = (currentLogBookFile != null) ? currentLogBookFile.getFile() : null;
        FileChooser fileChooser = createFileChooser(initialFile);
        fileChooser.setTitle(ResourceSource.getString("Title.chooseOpenLogBookFileName"));
        
        File openFile = fileChooser.showOpenDialog(ownerWindow);
        if (openFile == null) {
            return false;
        }
        
        if (currentLogBookFile != null) {
            // It's the current file...
            if (openFile.equals(currentLogBookFile.getFile())) {
                return true;
            }
        }
        
        if (!closeLogBook()) {
            return false;
        }
        
        openLogBook(openFile);
        return true;
    }
    
    protected void openLogBook(File openFile) throws LogBookFile.FileException {
        LogBookFile newLogBookFile = LogBookFile.openLogBookFile(openFile, this.activeAuthor);
        this.logBookFile.set(newLogBookFile);
        this.preferences.put(PREFS_PREVIOUS_FILE_NAME, openFile.getAbsolutePath());
        try {
            this.preferences.flush();
        } catch (BackingStoreException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    
    protected boolean closeLogBook() {
        if (this.logBookFile.get() == null) {
            return true;
        }
        
        for (Listener listener : this.listeners) {
            if (!listener.canCloseLogBookEditor(this)) {
                return false;
            }
        }
        
        // Do any views need to save changes?
        boolean isChanges = false;
        LogEntryView [] logEntryViews = getOpenLogEntryViews();
        for (LogEntryView logEntryView : logEntryViews) {
            if (logEntryView.isChanges()) {
                isChanges = true;
                break;
            }
        }
        
        boolean isSaveChanges = false;
        String saveChangesPrompt = ResourceSource.getString("Prompt.saveMultipleLogEntryChanges");
        if (!isChanges && this.logBookFile.get().isLogBookChanged()) {
            isChanges = true;
            saveChangesPrompt = ResourceSource.getString("Prompt.saveLogBookChanges");
        }
        
        if (isChanges) {
            // Prompt is 'One or more Log Entries have been modified. Do you want to save the
            // changes to all, select which ones, discard all the changes, cancel.
            PromptDialog promptDialog = new PromptDialog();
            promptDialog.setTitle(ResourceSource.getString("Title.saveChanges"));

            promptDialog.addMessage(saveChangesPrompt);

            promptDialog.addButton(ResourceSource.getString("Button.yes"), PromptDialog.BTN_YES);
            promptDialog.addButton(ResourceSource.getString("Button.no"), PromptDialog.BTN_NO);
            promptDialog.addButton(ResourceSource.getString("Button.cancel"), PromptDialog.BTN_CANCEL);
            promptDialog.setDefaultButtonId(PromptDialog.BTN_YES);
            promptDialog.setCancelButtonId(PromptDialog.BTN_CANCEL);

            switch (promptDialog.showSimpleDialog(null)) {
                case PromptDialog.BTN_YES :
                    isSaveChanges = true;
                    break;
                    
                case PromptDialog.BTN_NO :
                    isSaveChanges = false;
                    break;

                case PromptDialog.BTN_CANCEL :
                    return false;
            }

        }
        
        // Close all the log entry views.
        for (LogEntryView logEntryView : logEntryViews) {
            logEntryView.closeView(isSaveChanges);
        }
        
        listeners.forEach((listener) -> {
            listener.logBookEditorClosing(this);
        });

        if (isSaveChanges) {
            try {
                this.logBookFile.get().updateFile();
            } catch (LogBookFile.FileException ex) {
                LOG.log(Level.SEVERE, null, ex);
                PromptDialog.showOKDialog(ex.getLocalizedMessage(), ResourceSource.getString("Title.severeError"));
            }
        }
        
        this.logBookFile.set(null);
        return true;
    }
    
    
    /**
     * Calls the log book file's {@link LogBookFile#updateFile() } to save any changes to the log book.
     */
    public void saveLogBook() {
        try {
            this.logBookFile.get().updateFile();
        } catch (LogBookFile.FileException ex) {
            LOG.log(Level.SEVERE, null, ex);
            PromptDialog.showOKDialog(ex.getLocalizedMessage(), ResourceSource.getString("Title.severeError"));
        }
    }
    
    
    /**
     * @return Retrieves a modifiable array of the open {@link LogEntryView}s.
     */
    public LogEntryView [] getOpenLogEntryViews() {
        return this.logEntryViewsByGuid.values().toArray(new LogEntryView[this.logEntryViewsByGuid.size()]);
    }
    
    /**
     * Retrieves a {@link LogEntryView} for a {@link LogEntry} with a given id.
     * @param guid  The id of the log entry of interest.
     * @return The view, <code>null</code> if there is no {@link LogEntry} with the id in
     * the log book.
     */
    public LogEntryView getLogEntryView(String guid) {
        LogEntryView view = this.logEntryViewsByGuid.get(guid);
        if (view == null) {
            LogEntry logEntry = this.logBookFile.get().getLogBook().getLogEntryWithGuid(guid);
            if (logEntry == null) {
                return null;
            }
            
            view = createLogEntryView(logEntry);
        }
        
        return view;
    }
    
    /**
     * Retrieves a {@link LogEntryView} for a new {@link LogEntry}. The log entry is not
     * yet added to the log book.
     * @param timePeriod    The initial time period, if <code>null</code> then the current date will be used.
     * @return The view.
     */
    public LogEntryView getViewForNewLogEntry(TimePeriod timePeriod) {
        LogEntry logEntry = new LogEntry();
        
        if (timePeriod == null) {
            LogBook logBook = getLogBookFile().getLogBook();
            LocalDate now = LocalDate.now();
            timePeriod = TimePeriod.fromEdgeDates(now, now, logBook.getCurrentZoneId());
        }
        logEntry.setTimePeriod(timePeriod);
        
        LogEntryView view = createLogEntryView(logEntry);
        
        return view;
    }
    
    protected LogEntryView createLogEntryView(LogEntry logEntry) {
        LogEntryView view = new LogEntryView(logEntry, this);
        this.logEntryViewsByGuid.put(logEntry.getGuid(), view);
        
        view.addListener((LogEntryView view1) -> {
            listeners.forEach((listener)-> {
                listener.logEntryViewClosed(this, view1.getLogEntry());
            });
            logEntryViewsByGuid.remove(view1.getLogEntry().getGuid());
        });
        
        return view;
    }

    
    /**
     * Copies a {@link LogEntry} to the {@link LogEntry} in the log book with a given GUID.
     * @param guid  The GUID of the log entry. If there is no log entry in the log book, a
     * new log entry with this id is added.
     * @param workingLogEntry The log entry whose contents are to be copied.
     */
    public void updateLogEntry(String guid, LogEntry workingLogEntry) {
        EditLogEntryEdit edit = new EditLogEntryEdit(guid, workingLogEntry);
        if (getUndoManager() != null) {
            getUndoManager().addEdit(edit);
        }        
    }
    
    
    public class EditLogEntryEdit extends AbstractUndoableEdit {
        final LogEntry newLogEntrySettings;
        final LogEntry savedLogEntrySettings;
        
        public EditLogEntryEdit(String guid, LogEntry newLogEntrySettings) {
            LogBook logBook = getLogBook();
            LogEntry currentLogEntry = logBook.getLogEntryWithGuid(guid);
            if (currentLogEntry != null) {
                savedLogEntrySettings = new LogEntry(currentLogEntry);
            }
            else {
                savedLogEntrySettings = null;
            }
            this.newLogEntrySettings = new LogEntry(guid, newLogEntrySettings);
            
            applyEdit();
        }
        
        void applyEdit() {
            LogBook logBook = getLogBook();
            if (logBook != null) {
                LogEntry logEntry = logBook.getLogEntryWithGuid(newLogEntrySettings.getGuid());
                if (logEntry != null) {
                    logEntry.copyFrom(newLogEntrySettings);
                }
                else {
                    logBook.addLogEntry(new LogEntry(newLogEntrySettings));
                }
                if (getAutoSave()) {
                    saveLogBook();
                }
            }
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            applyEdit();
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            LogBook logBook = getLogBook();
            if (logBook != null) {
                if (savedLogEntrySettings == null) {
                    logBook.removeLogEntry(newLogEntrySettings.getGuid());
                }
                else {
                    LogEntry logEntry = logBook.getLogEntryWithGuid(savedLogEntrySettings.getGuid());
                    if (logEntry != null) {
                        logEntry.copyFrom(savedLogEntrySettings);
                    }
                }
                if (getAutoSave()) {
                    saveLogBook();
                }
            }
        }
        
    }
    

    /**
     * Opens a new log book window for this editor.
     * @return The log book window. The window is displayed and given focus.
     */
    public final LogBookWindow newLogBookWindow() {
        return newLogBookWindow(null);
    }
    
    /**
     * Opens a new log book window for this editor, with a given {@link Stage} for the window.
     * @param stage The stage to use, may be <code>null</code>.
     * @return The log book window. The window is displayed and given focus.
     */
    public LogBookWindow newLogBookWindow(Stage stage) {
        LogBookWindow window = new LogBookWindow(this, stage);
        this.logBookWindows.add(window);
        window.showWindow();
        return window;
    }
    
    /**
     * Safely attempts to closes a log book window. If it is the last log book window,
     * the current log book file being edited is closed safely.
     * @param window    The log book window to attempt to close.
     * @return <code>true</code> if the window was closed.
     */
    public boolean safeCloseLogBookWindow(LogBookWindow window) {
        // If this is the last log book window open, 
        int index = this.logBookWindows.indexOf(window);
        if (index < 0) {
            LOG.severe("Attempt to remove a LogBookWindow that's not in logBookWindows.");
            return true;
        }
        
        if (this.logBookWindows.size() == 1) {
            if (!closeLogBook()) {
                return false;
            }
        }
        
        this.logBookWindows.remove(index);
        window.shutDownWindow();
        
        return true;
    }
    
    
    public interface Listener {
        /**
         * This is called prior to the log book editor being closed safely, the listener
         * can cancel the closing by returning <code>false</code>.
         * @param editor    The editor calling this.
         * @return <code>true</code> if okay to close, <code>false</code> if not.
         */
        public boolean canCloseLogBookEditor(LogBookEditor editor);

        /**
         * Called during the log book editor being closed safely after everyone that can 
         * cancel has agreed to not cancel, but before any final changes have been
         * saved.
         * @param editor    The editor calling this.
         */
        public void logBookEditorClosing(LogBookEditor editor);
        
        public void logEntryViewClosed(LogBookEditor editor, LogEntry logEntry);
    }
    
    public final void addListener(Listener listener) {
        this.listeners.add(listener);
    }
    
    public final void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }
}
