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

import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * My editor for editing styled text. For now it just encapsulates a TextArea.
 * @author albert
 */
public class StyledTextEditor extends StackPane {
    TextArea textArea;
    
    
    public StyledTextEditor() {
        this.textArea = new TextArea();
        this.textArea.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        this.textArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.textArea.setPrefRowCount(4);
        
        getChildren().add(this.textArea);
    }
    
    
    public void setText(String text) {
        this.textArea.setText(text);
    }
    
    public String getText() {
        return this.textArea.getText();
    }
}
