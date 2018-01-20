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
package com.leeboardtools.control;

import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Albert Santos
 * @param <T>   The type of the item contained within the day cell..
 */
public class DayCellSkin <T> extends SkinBase<DayCell<T>> {
    
    public DayCellSkin(DayCell<T> control) {
        super(control);
        setupSkin();
    }

    private void setupSkin() {
        VBox vBox = new VBox();
        
        HBox hBoxHeader = new HBox();
        Label dayLabel = new Label();
        dayLabel.setId(MonthlyViewControl.DAY_OF_MONTH_NODE_ID);
        dayLabel.getStyleClass().add("day-of-month");
        
        Pane headerPane = new Pane();
        headerPane.setId(MonthlyViewControl.DATE_HEADER_NODE_ID);
        hBoxHeader.getChildren().addAll(dayLabel, headerPane);
        vBox.getChildren().add(hBoxHeader);
        
        Pane bodyPane = new Pane();
        bodyPane.setId(MonthlyViewControl.DATE_BODY_NODE_ID);
        vBox.getChildren().add(bodyPane);
        
        getChildren().add(vBox);
    }
    
}
