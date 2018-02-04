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

import com.leeboardtools.util.ResourceSource;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Represents the a main window for editing an {@link LogBook} via a {@link LogBookEditor}.
 * This encapsulates the actual {@link Window}.
 * @author Albert Santos
 */
public class LogBookWindow {
    final LogBookEditor logBookEditor;
    Stage stage;
    LogBookViewController controller;
    
    protected LogBookWindow(LogBookEditor logBookEditor, Stage stage) {
        this.logBookEditor = logBookEditor;
        this.stage = stage;
    }
    
    public void showWindow() {
        if (this.controller == null) {
            setupWindow();
        }
        
        if (this.stage != null) {
            this.stage.show();
            this.stage.toFront();
            this.stage.requestFocus();
        }
    }
    
    protected void setupWindow() {
        try {
            if (this.stage == null) {
                this.stage = new Stage();
            }
            
            URL location = LogBookViewController.class.getResource("LogBookView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(location, ResourceSource.getBundle());
            Parent root = fxmlLoader.load();
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add("leeboardslog/Styles.css");
            
            this.stage.setScene(scene);
            this.stage.setTitle(this.logBookEditor.getLogBookFile().getFile().getName());
            
            this.controller = (LogBookViewController)fxmlLoader.getController();
            if (this.controller != null) {
                this.controller.setLogBookEditor(this.logBookEditor);
            }
            
            this.stage.setOnCloseRequest((event)-> {
                if (this.logBookEditor != null) {
                    if (!this.logBookEditor.safeCloseLogBookWindow(this)) {
                        event.consume();
                    }
                    else {
                        if (this.controller != null) {
                            this.controller.setLogBookEditor(null);
                            this.controller = null;
                        }
                    }
                }
            });
            
        } catch (IOException ex) {
            Logger.getLogger(LogBookWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void shutDownWindow() {
        
    }
}
