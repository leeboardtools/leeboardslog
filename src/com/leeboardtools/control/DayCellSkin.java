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
 * Default skin created by {@link DayCell}.
 * <p>
 * This skin sets up a container node that contains a {@link Label} for the day of month value,
 * a {@link Pane} for a header that's associated with the day of month label,
 * and a {@link Pane} for a body.
 * @author Albert Santos
 * @param <T>   The type of the item contained within the day cell..
 */
public class DayCellSkin <T> extends SkinBase<DayCell<T>> {
    Label dayLabel;

    /**
     * Constructor.
     * @param control The control this is for.
     */
    public DayCellSkin(DayCell<T> control) {
        super(control);
        setupSkin();
    }
    
    private void setupSkin() {
        VBox vBox = new VBox();
        vBox.setFillWidth(true);
        
        DayCell<T> control = getSkinnable();
        HBox hBoxHeader = new HBox();
        this.dayLabel = new Label(control.getDayOfMonthText());
        this.dayLabel.getStyleClass().add("day-of-month");
        control.dayOfMonthTextProperty().addListener((skinnable, oldValue, newValue) -> {
            this.dayLabel.setText(newValue);
        });
        hBoxHeader.getChildren().add(this.dayLabel);
        
        if (control.getHeaderCell() != null) {
            hBoxHeader.getChildren().add(control.getHeaderCell());
        }

        vBox.getChildren().add(hBoxHeader);

        if (control.getBodyCell() != null) {
            vBox.getChildren().add(control.getBodyCell());
        }
        
        getChildren().add(vBox);
    }
    
}
