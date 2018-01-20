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

import com.leeboardtools.util.FxUtil;
import java.time.LocalDate;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

/**
 *
 * @author Albert Santos
 * @param <T>   The type of the item contained within the day cell..
 */
public class DayCell <T> extends Cell<LocalDate> {
    private static final String DEFAULT_STYLE_CLASS = "day-cell";
    protected final MonthlyViewControl<T> control;
    private boolean isSetup = false;
    private Label dayOfMonthLabel;
    private Pane headerPane;
    private Pane bodyPane;

    public DayCell(final MonthlyViewControl<T> control) {
        this.control = control;
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    @Override
    protected void updateItem(LocalDate date, boolean empty) {
        super.updateItem(date, empty);

        updateDayOfMonthLabel(date);        
        
        ObservableMap<LocalDate, T> items = this.control.getItems();
        T item = (items != null) ? items.get(date) : null;
        updateDayItem(item);
    }
    
    protected void updateDayOfMonthLabel(LocalDate date) {
        if (this.dayOfMonthLabel != null) {
            if (date != null) {
                this.dayOfMonthLabel.setText(Integer.toString(date.getDayOfMonth()));
            }
            else {
                this.dayOfMonthLabel.setText(null);
            }
        }
    }
    
    protected void updateDayItem(T item) {
        
    }

    protected void loadNodes() {
        Node node = FxUtil.getChildWithId(this, MonthlyViewControl.DAY_OF_MONTH_NODE_ID);
        if (node instanceof Label) {
            this.dayOfMonthLabel = (Label) node;
        }
        node = FxUtil.getChildWithId(this, MonthlyViewControl.DATE_HEADER_NODE_ID);
        if (node instanceof Pane) {
            this.headerPane = (Pane) node;
        }
        node = FxUtil.getChildWithId(this, MonthlyViewControl.DATE_BODY_NODE_ID);
        if (node instanceof Pane) {
            this.bodyPane = (Pane) node;
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DayCellSkin(this);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        if (!this.isSetup) {
            this.isSetup = true;
            loadNodes();
            updateItem(this.itemProperty().get(), this.emptyProperty().get());
        }
    }
    
}
