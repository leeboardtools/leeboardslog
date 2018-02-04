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

import com.leeboardtools.control.skin.DayCellSkin;
import java.time.LocalDate;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableMap;
import javafx.css.PseudoClass;
import javafx.scene.control.Cell;
import javafx.scene.control.Skin;

/**
 * Cell used to represent the contents of a day within a {@link MultiDayView} derived control.
 * @author Albert Santos
 * @param <T>   The type of the item contained within the day cell..
 */
public class DayCell <T> extends Cell<LocalDate> {
    private static final String DEFAULT_STYLE_CLASS = "day-cell";
    
    public static final String STYLE_CLASS_PREVIOUS_MONTH = "previous-month";
    public static final String STYLE_CLASS_NEXT_MONTH = "next-month";
    public static final String STYLE_CLASS_HEADER = "header";
    public static final String STYLE_CLASS_BODY = "body";
    
    public static final PseudoClass PSEUDO_CLASS_TODAY = PseudoClass.getPseudoClass("today");
    public static final PseudoClass PSEUDO_CLASS_FIRST_OF_MONTH = PseudoClass.getPseudoClass("first-of-month");
    public static final PseudoClass PSEUDO_CLASS_ODD_MONTH = PseudoClass.getPseudoClass("odd-month");
    
    protected final MultiDayView<T> control;
    
    /**
     * @return The {@link MultiDayView} to which this belongs.
     */
    public final MultiDayView<T> getMultiDayView() {
        return control;
    }
    
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
    
    
    public static enum ActiveMonthRelation {
        BEFORE,
        SAME,
        AFTER,
    }
    
    /**
     * The relationship of the month of the date represented by the cell to the active month.
     */
    private final ObjectProperty<ActiveMonthRelation> activeMonthRelation = new SimpleObjectProperty<>(this, "activeMonthRelation", null);
    
    public final ActiveMonthRelation getActiveMonthRelation() {
        return activeMonthRelation.get();
    }
    public final void setActiveMonthRelation(ActiveMonthRelation value) {
        activeMonthRelation.set(value);
    }
    public final ObjectProperty<ActiveMonthRelation> activeMonthRelation() {
        return activeMonthRelation;
    }
    
    
    /**
     * Returns whether the cell represents 'today'.
     */
    private final BooleanProperty cellIsToday = new SimpleBooleanProperty(this, "cellIsToday", false) {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(PSEUDO_CLASS_TODAY, get());
        }
        
    };
    
    public final boolean cellIsToday() {
        return cellIsToday.get();
    }
    public final void setCellIsToday(boolean value) {
        cellIsToday.set(value);
    }
    public final BooleanProperty cellIsTodayProperty() {
        return cellIsToday;
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
        if (this.headerCell != null) {
            this.headerCell.getStyleClass().add(STYLE_CLASS_HEADER);
        }
        
        this.bodyCell = control.createBodyCell(this);
        if (this.bodyCell != null) {
            this.bodyCell.getStyleClass().add(STYLE_CLASS_BODY);
        }
    }

    
    @Override
    public void updateItem(LocalDate date, boolean empty) {
        super.updateItem(date, empty);
        
        switch (activeMonthRelation.get()) {
            case BEFORE :
                getStyleClass().add(STYLE_CLASS_PREVIOUS_MONTH);
                break;

            case SAME :
                break;

            case AFTER :
                getStyleClass().add(STYLE_CLASS_NEXT_MONTH);
                break;
        }
        
        T item = null;
        if ((date == null) || empty) {
            setText("");
            setGraphic(null);
            
            this.dayOfMonthText.set("");
        }
        else {
            ObservableMap<LocalDate, T> items = this.control.getItems();
            item = (items != null) ? items.get(date) : null;
            
            boolean isOddMonth = (date.getMonthValue() & 0x01) != 0;
            pseudoClassStateChanged(PSEUDO_CLASS_ODD_MONTH, isOddMonth);
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
