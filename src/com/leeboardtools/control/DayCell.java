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

import java.time.LocalDate;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableMap;
import javafx.scene.control.Cell;
import javafx.scene.control.Skin;

/**
 * Cell used to represent the contents of a day within a {@link MultiDayView} derived control.
 * @author Albert Santos
 * @param <T>   The type of the item contained within the day cell..
 */
public class DayCell <T> extends Cell<LocalDate> {
    private static final String DEFAULT_STYLE_CLASS = "day-cell";
    protected final MultiDayView<T> control;
    

    protected Cell<T> headerCell;
    
    /**
     * The header cell, may be <code>null</code>
     * @return The cell.
     */
    public final Cell<T> getHeaderCell() {
        return headerCell;
    }
    
    
    protected Cell<T> bodyCell;
    
    /**
     * The body cell.
     * @return The cell.
     */
    public final Cell<T> getBodyCell() {
        return bodyCell;
    }
    
    
    /**
     * The text for the current day of the month represented by the cell.
     */
    private final ReadOnlyStringWrapper dayOfMonthText = new ReadOnlyStringWrapper(this, "dayOfMonthText", "");
    
    public final ReadOnlyStringProperty dayOfMonthTextProperty() {
        return dayOfMonthText.getReadOnlyProperty();
    }
    public String getDayOfMonthText() {
        return dayOfMonthText.get();
    }
    
    
    /**
     * The data item associated with the current date.
     */
    private final ReadOnlyObjectWrapper<T> itemData = new ReadOnlyObjectWrapper<>(this, "itemData", null);
    
    public final ReadOnlyObjectProperty<T> itemDataProperty() {
        return itemData.getReadOnlyProperty();
    }
    public final T getItemData() {
        return itemData.get();
    }
    
    
    /**
     * Constructor.
     * @param control The control calling this.
     */
    public DayCell(final MultiDayView<T> control) {
        this.control = control;
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }
    
    /**
     * This is called after the day cell is created by {@link MultiDayView#createDayCell() },
     * it's where the header and body cells are created.
     */
    public void setupInnerCells() {
        this.headerCell = control.createHeaderCell(this);
        this.bodyCell = control.createBodyCell(this);
    }

    
    @Override
    protected void updateItem(LocalDate date, boolean empty) {
        super.updateItem(date, empty);

        T item = null;
        if ((date == null) || empty) {
            this.dayOfMonthText.set("");
        }
        else {
            this.dayOfMonthText.set(Integer.toString(date.getDayOfMonth()));

            ObservableMap<LocalDate, T> items = this.control.getItems();
            item = (items != null) ? items.get(date) : null;
        }
        
        this.itemData.set(item);
        
        if (this.headerCell != null) {
            this.headerCell.setItem(item);
        }
        if (this.bodyCell != null) {
            this.bodyCell.setItem(item);
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DayCellSkin(this);
    }

}
