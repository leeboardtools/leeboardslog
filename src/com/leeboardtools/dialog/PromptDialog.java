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
package com.leeboardtools.dialog;

import com.leeboardtools.util.FxUtil;
import com.leeboardtools.util.ResourceSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

/**
 * This is a generic dialog box that offers a list of options to choose from as
 * well as a list of text messages.
 * <p>
 * Usage:
 * <pre>
 * {@code 
 *      PromptDialog dialog = new PromptDialog();
 *      dialog.addMessage("This is a prompt.");
 *      dialog.addMessage("");  // Force a newline this if you want, or use '\n' in the string.
 *      dialog.addMessage("You have the following choices:");
 * 
 *      dialog.addButton("Do Nothing", 1);
 *      dialog.addButton("Do Something", 2);
 *      dialog.addButton("Quit While You're Ahead", 3);
 *      dialog.setCancelButtonId(3);
 * 
 *      switch (dialog.showDialog()) {
 *      case 1 :
 *          System.out.println("Do Nothing Chosen");
 *          break;
 *      case 2:
 *          System.out.println("Do Something Chosen");
 *          break;
 *      case 3:
 *          System.out.println("Getting Out of Here!");
 *          break;
 *      }
 * }
 * </pre>
 * TODO Some day add an icon...
 * @author Albert Santos
 */
public class PromptDialog {
    public static final int INVALID_BUTTON_ID = Integer.MIN_VALUE;
    
    public static final String STYLE_MESSAGE = "prompt-message";
    public static final String STYLE_BUTTON = "prompt-button";
    public static final String STYLE_TEXT_INPUT_LABEL = "label-text-input";
    public static final String STYLE_TEXT_INPUT_EDIT = "edit-tex-input";
    
    private int defaultButtonId = INVALID_BUTTON_ID;
    private int cancelButtonId = INVALID_BUTTON_ID;
    
    private String title = "";
    
    private final List<String> messageEntries = new ArrayList<>();
    
    private static class TextInputEntry {
        final String label;
        final String id;
        String initialText;
        String promptText;
        boolean isTextRequired;
        TextField textField;
        
        TextInputEntry(String label, String id, String initialText, String promptText, boolean isTextRequired) {
            this.label = label;
            this.id = id;
            this.initialText = initialText;
            this.promptText = promptText;
            this.isTextRequired = isTextRequired;
        }
    }
    
    private final List<TextInputEntry> textInputEntries = new ArrayList<>();
    
    
    private static class ButtonEntry {
        final String text;
        final int id;
        Button button;
        
        ButtonEntry(String text, int id) {
            this.text = text;
            this.id = id;
        }
    }
    private final List<ButtonEntry> buttonEntries = new ArrayList<>();
    
    private Callback<Integer, Boolean> buttonCloseCallback;
    
    private int chosenButtonId = INVALID_BUTTON_ID;
    private Stage stage;
    
    /**
     * Some pre-defined button ids, they're only for convenience.
     */
    public static final int BTN_CANCEL      = 0;
    public static final int BTN_OK          = 1;
    public static final int BTN_YES         = 2;
    public static final int BTN_NO          = 3;
    
    
    /**
     * Constructor.
     */
    public PromptDialog() {
    }
    
    /**
     * Sets the title of the dialog box.
     * @param title The title.
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Adds message text. The text appears below any previously added message text.
     * @param text The text.
     */
    public void addMessage(String text) {
        this.messageEntries.add(text);
    }
    
    public void addTextInput(String label, String id, String initialText, String promptText, boolean textRequired) {
        this.textInputEntries.add(new TextInputEntry(label, id, initialText, promptText, textRequired));
    }
    
    public String getTextInputText(String id) {
        for (TextInputEntry entry : this.textInputEntries) {
            if (entry.id.equals(id)) {
                if (entry.textField != null) {
                    return entry.textField.getText();
                }
                return null;
            }
        }
        return null;
    }
    
    /**
     * Adds a button. The button appears below any previously added buttons.
     * @param buttonText    The text for the button.
     * @param id The id associated with the button, if the button is chosen this
     * is what is returned by {@link #showOptionsDialog() }.
     */
    public void addButton(String buttonText, int id) {
        this.buttonEntries.add(new ButtonEntry(buttonText, id));
    }
    
    /**
     * Sets the id of the button that is to appear as the default button.
     * @param id The id of the button.
     */
    public void setDefaultButtonId(int id) {
        this.defaultButtonId = id;
    }
    
    /**
     * Sets the id of the button that is to appear as the cancel button.
     * @param id The id of the button.
     */
    public void setCancelButtonId(int id) {
        this.cancelButtonId = id;
    }
    
    
    /**
     * Installs an optional callback that gets called when a button is chosen, prior
     * to the dialog being closed to allow validation. The callback should return
     * <code>false</code> if the dialog box should not be closed.
     * @param callback The callback, may be <code>null</code>.
     */
    public void setButtonCloseCallback(Callback<Integer, Boolean> callback) {
        this.buttonCloseCallback = callback;
    }
    
    
    protected void setupRoot(Parent root) {
        setupMessages(root);
        setupTextInputEntries(root);
        setupButtons(root);
        updateButtons();
    }
    
    protected void setupMessages(Parent root) {
        Node node = FxUtil.getChildWithId(root, "promptContainer");
        if (node instanceof VBox) {
            VBox vBox = (VBox)node;
            vBox.getChildren().clear();
            
            for (int i = 0; i < this.messageEntries.size(); ++i) {
                Label text = new Label(this.messageEntries.get(i));
                text.setWrapText(true);
                text.setMaxWidth(Double.MAX_VALUE);
                text.getStyleClass().add(STYLE_MESSAGE);
                
                vBox.getChildren().add(text);
            }
        }
    }
    
    protected void updateButtons() {
        boolean isDefaultDisabled = false;
        
        for (TextInputEntry entry : this.textInputEntries) {
            if (entry.isTextRequired) {
                String text = null;
                if (entry.textField != null) {
                    text = entry.textField.getText();
                }
                if (text != null) {
                    text = text.trim();
                }
                if ((text == null) || text.isEmpty()) {
                    isDefaultDisabled = true;
                    break;
                }
            }
        }
        
        for (ButtonEntry entry : this.buttonEntries) {
            if (entry.id == this.defaultButtonId) {
                entry.button.setDisable(isDefaultDisabled);
            }
        }
    }
    
    protected void setupTextInputEntries(Parent root) {
        Node node = FxUtil.getChildWithId(root, "promptContainer");
        if (node instanceof VBox) {
            VBox vBox = (VBox)node;
            
            for (int i = 0; i < this.textInputEntries.size(); ++i) {
                TextInputEntry textInputEntry = this.textInputEntries.get(i);
                Label label = new Label(textInputEntry.label);
                label.setWrapText(true);
                label.setMaxWidth(Double.MAX_VALUE);
                label.getStyleClass().add(STYLE_TEXT_INPUT_LABEL);
                
                vBox.getChildren().add(label);
                
                TextField edit = new TextField(textInputEntry.initialText);
                if (textInputEntry.promptText != null) {
                    edit.setPromptText(textInputEntry.promptText);
                }
                
                if (textInputEntry.isTextRequired) {
                    edit.textProperty().addListener((prop, oldValue, newValue) -> {
                        updateButtons();
                    });
                }
                
                edit.setMaxWidth(Double.MAX_VALUE);
                edit.getStyleClass().add(STYLE_TEXT_INPUT_EDIT);
                
                textInputEntry.textField = edit;

                vBox.getChildren().add(edit);
            }
        }
    }
    
    protected void setupButtons(Parent root) {
        Node node = FxUtil.getChildWithId(root, "buttonContainer");
        if (node instanceof VBox) {
            VBox vBox = (VBox)node;

            for (int i = 0; i < this.buttonEntries.size(); ++i) {
                ButtonEntry entry = this.buttonEntries.get(i);
                Button button = new Button(entry.text);
                if (entry.id == this.cancelButtonId) {
                    button.setCancelButton(true);
                }
                if (entry.id == this.defaultButtonId) {
                    button.setDefaultButton(true);
                }
                button.setMaxWidth(Double.MAX_VALUE);
                button.getStyleClass().add(STYLE_BUTTON);
                
                button.setOnAction((e)-> {
                    onButtonChosen(entry.id);
                });
                
                entry.button = button;
                
                vBox.getChildren().add(button);
            }
        }
        else if (node instanceof HBox) {
            HBox hBox = (HBox)node;
            hBox.getChildren().clear();

            for (int i = 0; i < this.buttonEntries.size(); ++i) {
                ButtonEntry entry = this.buttonEntries.get(i);
                Button button = new Button(entry.text);
                if (entry.id == this.cancelButtonId) {
                    button.setCancelButton(true);
                }
                if (entry.id == this.defaultButtonId) {
                    button.setDefaultButton(true);
                }
                button.setPrefWidth(80);
                button.setMaxWidth(Double.MAX_VALUE);
                button.getStyleClass().add(STYLE_BUTTON);
                
                button.setOnAction((e)-> {
                    onButtonChosen(entry.id);
                });
                
                if (this.buttonEntries.size() > 1) {
                //    HBox.setHgrow(button, Priority.ALWAYS);
                }

                entry.button = button;

                hBox.getChildren().add(button);
            }
        }
    }
    
    protected void onButtonChosen(int id) {
        if (buttonCloseCallback != null) {
            if (!buttonCloseCallback.call(id)) {
                return;
            }
        }
        
        chosenButtonId = id;
        stage.close();
    }
    
    protected void showRoot(Parent root, Window ownerWindow) {
        this.stage = new Stage();
        
        if (this.title != null) {
            this.stage.setTitle(this.title);
        }
        
        Scene scene = new Scene(root);
        this.stage.setScene(scene);
        
        if (root instanceof Region) {
            Region rootRegion = (Region)root;
            
            double minWidth = rootRegion.getMinWidth();
            if ((minWidth != Control.USE_COMPUTED_SIZE) && (minWidth != Control.USE_PREF_SIZE)) {
                this.stage.setMinWidth(minWidth);
            }
            
            double minHeight = rootRegion.getMinHeight();
            if ((minHeight != Control.USE_COMPUTED_SIZE) && (minHeight != Control.USE_PREF_SIZE)) {
                this.stage.setMinHeight(minHeight);
            }
        }
        
        this.stage.initModality(Modality.APPLICATION_MODAL);
        this.stage.initOwner(ownerWindow);
        this.stage.showAndWait();
    }
    
    protected int showDialog(Window ownerWindow, String fxmlName) {
        this.chosenButtonId = INVALID_BUTTON_ID;
        try {
            Parent root = FXMLLoader.load(PromptDialog.class.getResource(fxmlName));
            setupRoot(root);
            showRoot(root, ownerWindow);
        } catch (IOException ex) {
            Logger.getLogger(PromptDialog.class.getName()).log(Level.SEVERE, null, ex);
            return this.cancelButtonId;
        }
        return this.chosenButtonId;
    }
    
    /**
     * Displays the dialog box as an options choice dialog box and waits for a button to be chosen.
     * @param ownerWindow   The owner window, may be <code>null</code>
     * @return The id of the chosen button.
     */
    public int showOptionsDialog(Window ownerWindow) {
        return showDialog(ownerWindow, "OptionsPromptDialog.fxml");
    }
    
    
    /**
     * Displays the dialog box as an options choice dialog box and waits for a button to be chosen.
     * @return The id of the chosen button.
     */
    public int showOptionsDialog() {
        return showOptionsDialog(null);
    }
    
    
    /**
     * Displays the dialog box as a simple prompt dialog box and waits for a button to be chosen.
     * @param ownerWindow   The owner window, may be <code>null</code>
     * @return The id of the chosen button.
     */
    public int showSimpleDialog(Window ownerWindow) {
        return showDialog(ownerWindow, "SimplePromptDialog.fxml");
    }
    
    
    /**
     * Displays the dialog box as a simple prompt dialog box and waits for a button to be chosen.
     * @return The id of the chosen button.
     */
    public int showSimpleDialog() {
        return showSimpleDialog(null);
    }
    

    /**
     * Helper that puts up a dialog box with just an OK button.
     * @param ownerWindow   The owner window, may be <code>null</code>
     * @param message   The message to display.
     * @param title If not <code>null</code> the title for the window.
     */
    public static void showOKDialog(Window ownerWindow, String message, String title) {
        PromptDialog dialog = new PromptDialog();
        if (title != null) {
            dialog.setTitle(title);
        }
        dialog.addMessage(message);
        dialog.addButton(ResourceSource.getString("LBDialog.OK"), BTN_OK);
        
        dialog.showSimpleDialog(ownerWindow);
    }

    /**
     * Helper that puts up a dialog box with just an OK button.
     * @param message   The message to display.
     * @param title If not <code>null</code> the title for the window.
     */
    public static void showOKDialog(String message, String title) {
        showOKDialog(null, message, title);
    }
    

    /**
     * Helper that puts up a dialog box with an OK and a Cancel button.
     * @param ownerWindow   The owner window, may be <code>null</code>
     * @param message   The message to display.
     * @param title If not <code>null</code> the title for the window.
     * @return <code>true</code> if the OK button was chosen.
     */
    public static boolean showOKCancelDialog(Window ownerWindow, String message, String title) {
        PromptDialog dialog = new PromptDialog();
        if (title != null) {
            dialog.setTitle(title);
        }
        dialog.addMessage(message);
        dialog.addButton(ResourceSource.getString("LBDialog.OK"), BTN_OK);
        dialog.setDefaultButtonId(BTN_OK);
        dialog.addButton(ResourceSource.getString("LBDialog.Cancel"), BTN_CANCEL);
        dialog.setCancelButtonId(BTN_CANCEL);
        
        return dialog.showSimpleDialog(ownerWindow) == BTN_OK;
    }

    /**
     * Helper that puts up a dialog box with an OK and a Cancel button.
     * @param message   The message to display.
     * @param title If not <code>null</code> the title for the window.
     * @return <code>true</code> if the OK button was chosen.
     */
    public static boolean showOKCancelDialog(String message, String title) {
        return showOKCancelDialog(null, message, title);
    }
}
