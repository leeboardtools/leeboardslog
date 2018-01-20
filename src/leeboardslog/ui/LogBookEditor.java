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
import com.leeboardtools.util.FxUtil;
import com.leeboardtools.util.ResourceSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.text.Text;
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
    
    public static final String PREFS_LAST_FILE_NAME = "LastFileName";
    
    public static final int BTN_NEW_LOG_FILE = 1;
    public static final int BTN_OPEN_LOG_FILE = 2;
    public static final int BTN_EXIT = 3;
    
    final Preferences preferences;
    LogBookFile logBookFile;
    
    public LogBookEditor(Preferences preferences) {
        this.preferences = preferences;
    }
    
    protected void generatePromptMessagesForFileException(ArrayList<String> promptMsgs, LogBookFile.FileException ex) {
        switch (ex.getReason()) {
            case FILE_NOT_FOUND :
                promptMsgs.add(ResourceSource.getString("Prompt.lastEditedLogBookNotFound", ex.getFileName(), ex.getLocalizedMessage()));
                break;

            case FILE_IO_ERROR :
            case INVALID_FORMAT :
            default :
                promptMsgs.add(ResourceSource.getString("Prompt.lastEditedLogBookFailed", ex.getFileName(), ex.getLocalizedMessage()));
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
            generatePromptMessagesForFileException(promptMsgs, ex);
        }
        
        while (true) {
            PromptDialog promptDialog = new PromptDialog();
            promptDialog.setTitle(ResourceSource.getString("Title.application"));
            
            for (String msg : promptMsgs) {
                promptDialog.addMessage(msg);
            }
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
                generatePromptMessagesForFileException(promptMsgs, ex);
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
        String lastFileName = this.preferences.get(PREFS_LAST_FILE_NAME, "");
        if (lastFileName.isEmpty()) {
            return false;
        }
        return false;
    }
    
    
    /**
     * Prompts for the name of a new log book file and creates it.
     * @param ownerWindow   The owner window, may be <code>null</code>.
     * @return  <code>true</code> if a new file was created, false if the prompt was canceled.
     * @throws leeboardslog.data.LogBookFile.FileException 
     */
    public boolean promptNewLogBook(Window ownerWindow) throws LogBookFile.FileException {
        // We need a name for the log book file.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(ResourceSource.getString("Title.chooseNewLogBookFileName"));
        
        File newFile = fileChooser.showSaveDialog(ownerWindow);
        if (newFile == null) {
            return false;
        }
        
        LogBookFile newLogBookFile = LogBookFile.createLogBookFile(newFile);
        this.logBookFile = newLogBookFile;
        this.preferences.put(PREFS_LAST_FILE_NAME, newFile.getAbsolutePath());
        
        return true;
    }
    
    
    /**
     * Prompts for the name of an existing log book and opens it.
     * @param ownerWindow   The owner window, may be <code>null</code>.
     * @return  <code>true</code> if a file was opened, false if the prompt was canceled.
     * @throws leeboardslog.data.LogBookFile.FileException 
     */
    public boolean promptOpenLogBook(Window ownerWindow) throws LogBookFile.FileException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(ResourceSource.getString("Title.chooseOpenLogBookFileName"));
        
        File openFile = fileChooser.showOpenDialog(ownerWindow);
        if (openFile == null) {
            return false;
        }
        
        if (this.logBookFile != null) {
            // It's the current file...
            if (openFile.equals(this.logBookFile.getFile())) {
                return true;
            }
        }
        
        LogBookFile newLogBookFile = LogBookFile.openLogBookFile(openFile);
        this.logBookFile = newLogBookFile;
        this.preferences.put(PREFS_LAST_FILE_NAME, openFile.getAbsolutePath());
        
        return true;
    }
}
