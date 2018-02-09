/*
 * Copyright 2018 albert.
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
package com.leeboardtools.text;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * My editor for editing styled text. For now it just encapsulates a TextArea.
 * The basic format is HTML, see {@link Style}.
 * @author albert
 */
public class StyledTextEditor extends StackPane {
    TextArea textArea;
    
    public StyledTextEditor() {
        this.textArea = new TextArea();
        this.textArea.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        this.textArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.textArea.setPrefRowCount(4);
        
        // TEST!!!
        this.textArea.focusedProperty().addListener((property, oldValue, newValue)-> {
            if (!newValue) {
                fireEvent(new ActionEvent(this, null));
            }
        });
        
        getChildren().add(this.textArea);
    }
    
    
    public void setStyledText(String text) {
        this.textArea.setText(text);
    }
    
    public String getStyledText() {
        return this.textArea.getText();
    }
    
    
    // TODO: Add an API fairly similar to TextInputControl.
    
    private ObjectProperty<EventHandler<ActionEvent>> onAction;
    
    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        if (onAction == null) {
            onAction = new SimpleObjectProperty<EventHandler<ActionEvent>>(this, "onAction", null) {
                @Override 
                protected void invalidated() {
                    setEventHandler(ActionEvent.ACTION, get());
                }
            };
        }
        return onAction;
    }
    public final EventHandler<ActionEvent> getOnAction() {
        return (onAction != null) ? onAction.get() : null;
    }
    public final void setOnAction(EventHandler<ActionEvent> value) {
        onActionProperty().set(value);
    }
    
    
    // Things to add:
    // Styles!
    // Character Styles
    // Paragraph Styles
    // Other Styles (table styles, what else?)
}
