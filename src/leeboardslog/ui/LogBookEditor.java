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
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import leeboardslog.data.LogBookFile;

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
    
    public static final int BTN_NEW_LOG_FILE = 1;
    public static final int BTN_OPEN_LOG_FILE = 2;
    public static final int BTN_EXIT = 3;
    
    public static final String FILE_EXTENSION = "leeboardslog";
    
    String defaultDocSubDir = "LeeboardTools";
    String activeAuthor;
    
    final Preferences preferences;
    private final ReadOnlyObjectWrapper<LogBookFile> logBookFile = new ReadOnlyObjectWrapper<>(this, "logBookFile");
    
    public LogBookEditor(Preferences preferences) {
        this.preferences = preferences;
        
        this.activeAuthor = preferences.get(PREFS_ACTIVE_AUTHOR_NAME, "");
        if (this.activeAuthor.isEmpty()) {
            this.activeAuthor = System.getProperty("user.name");
        }
    }
    
    /**
     * @return The value of the log book file property.
     */
    public final LogBookFile getLogBookFile() {
        return this.logBookFile.get();
    }
    
    /**
     * Defines the log book that's currently being edited.
     * @return The logBookFile property.
     */
    public final ReadOnlyObjectProperty<LogBookFile> logBookFileProperty() {
        return this.logBookFile.getReadOnlyProperty();
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
     * @param ownerWindow 
     * @return  <code>false</code> if the exit was chosen.
     */
    public boolean restoreLastLogBook(Window ownerWindow) {
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
                switch (promptDialog.showDialog(ownerWindow)) {
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
     * @param ownerWindow
     * @return <code>false</code> if there was no last log book opened.
     * @throws leeboardslog.data.LogBookFile.FileException 
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
                fileChooser.setInitialDirectory(initialFile);
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
     * @throws leeboardslog.data.LogBookFile.FileException 
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
     * @throws leeboardslog.data.LogBookFile.FileException 
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
    
    
}
