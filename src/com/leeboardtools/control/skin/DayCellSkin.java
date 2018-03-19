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
package com.leeboardtools.control.skin;

import com.leeboardtools.control.DayCell;
import com.leeboardtools.control.MultiDayView;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import javafx.scene.control.Cell;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
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
        
        control.itemProperty().addListener((property, oldValue, newValue) -> {
            updateSkin();
        });
        
        control.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent event) -> {
            if (!control.contains(event.getX(), event.getY())) {
                return;
            }
            if (event.getButton() == MouseButton.PRIMARY) {
                MultiDayView<T> view = control.getMultiDayView();
                if ((view.getActiveDate() == null) || !view.getActiveDate().equals(control.getItem())) {
                    view.setActiveDate(control.getItem());
                }
                else if (!view.isEditing() && (event.getClickCount() == 2)) {
                    onDoubleClick(event);
                }
            }
        });
        
        updateSkin();
    }
    
    
    private void setupSkin() {
        VBox vBox = new VBox();
        vBox.setFillWidth(true);
        
        DayCell<T> dayCell = getSkinnable();
        HBox hBoxHeader = new HBox();
        this.dayLabel = new Label(dayCell.getDayOfMonthText());
        this.dayLabel.getStyleClass().add("day-of-month");
        
        dayCell.dayOfMonthTextProperty().addListener((skinnable, oldValue, newValue) -> {
            this.dayLabel.setText(newValue);
        });
        hBoxHeader.getChildren().add(this.dayLabel);
        
        if (dayCell.getHeaderCell() != null) {
            hBoxHeader.getChildren().add(dayCell.getHeaderCell());
        }

        vBox.getChildren().add(hBoxHeader);

        Cell<T> bodyCell = dayCell.getBodyCell();
        if (bodyCell != null) {
            vBox.getChildren().add(bodyCell);
            VBox.setVgrow(bodyCell, Priority.ALWAYS);
        }
       
        getChildren().add(vBox);
        
    }
    
    
    protected void updateSkin() {
        LocalDate date = getSkinnable().getItem();
        if (date == null) {
            this.dayLabel.setText("");
            return;
        }
        
        if (date.getDayOfMonth() == 1) {
            String text = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + " " + date.getDayOfMonth();
            this.dayLabel.setText(text);

            this.dayLabel.pseudoClassStateChanged(DayCell.PSEUDO_CLASS_FIRST_OF_MONTH, true);
        }
        else {
            this.dayLabel.setText(Integer.toString(date.getDayOfMonth()));

            this.dayLabel.pseudoClassStateChanged(DayCell.PSEUDO_CLASS_FIRST_OF_MONTH, false);
        }
    }
    
    
    protected void onDoubleClick(MouseEvent event) {
        MultiDayView<T> view = getSkinnable().getMultiDayView();
        
        view.startEdit();
        event.consume();
    }
}
