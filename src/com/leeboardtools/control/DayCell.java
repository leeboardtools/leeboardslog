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
import java.time.format.TextStyle;
import java.util.Locale;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.scene.control.Cell;
import javafx.scene.control.Skin;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/**
 * Cell used to represent the contents of a day within a {@link MultiDayView} derived control.
 * @author Albert Santos
 * @param <T>   The type of the item contained within the day cell..
 */
public class DayCell <T> extends Cell<LocalDate> {
    private static final String DEFAULT_STYLE_CLASS = "day-cell";
    
    public static final String STYLE_CLASS_PREVIOUS_MONTH = "previous-month";
    public static final String STYLE_CLASS_NEXT_MONTH = "next-month";
    public static final String STYLE_CLASS_SELECTED = "selected";
    public static final String STYLE_CLASS_TODAY = "today";
    
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
    private final BooleanProperty isToday = new SimpleBooleanProperty(this, "isToday", false);
    
    public final boolean isToday() {
        return isToday.get();
    }
    public final void setIsToday(boolean value) {
        isToday.set(value);
    }
    public final BooleanProperty isTodayProperty() {
        return isToday;
    }
    
    
    private final ObjectProperty<Background> previousMonthBackground = new SimpleObjectProperty<>(this, "previousMonthBackground",
        new Background(new BackgroundFill(Color.GAINSBORO, CornerRadii.EMPTY, Insets.EMPTY)));

    public final Background getPreviousMonthBackground() {
        return previousMonthBackground.get();
    }
    public final void setPreviousMonthBackground(Background value) {
        previousMonthBackground.set(value);
    }
    public final ObjectProperty<Background> previousMonthBackgroundProperty() {
        return previousMonthBackground;
    }
    
    
    private final ObjectProperty<Background> currentMonthBackground = new SimpleObjectProperty<>(this, "currentMonthBackground",
        null);

    public final Background getCurrentMonthBackground() {
        return currentMonthBackground.get();
    }
    public final void setCurrentMonthBackground(Background value) {
        currentMonthBackground.set(value);
    }
    public final ObjectProperty<Background> currentMonthBackgroundProperty() {
        return currentMonthBackground;
    }
    
    
    private final ObjectProperty<Background> nextMonthBackground = new SimpleObjectProperty<>(this, "nextMonthBackground",
        new Background(new BackgroundFill(Color.GAINSBORO, CornerRadii.EMPTY, Insets.EMPTY)));

    public final Background getNextMonthBackground() {
        return nextMonthBackground.get();
    }
    public final void setNextMonthBackground(Background value) {
        nextMonthBackground.set(value);
    }
    public final ObjectProperty<Background> nextMonthBackgroundProperty() {
        return nextMonthBackground;
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
    public void updateItem(LocalDate date, boolean empty) {
        super.updateItem(date, empty);
        
        switch (activeMonthRelation.get()) {
            case BEFORE :
                getStyleClass().add(STYLE_CLASS_PREVIOUS_MONTH);
                setBackground(getPreviousMonthBackground());
                break;

            case SAME :
                setBackground(getCurrentMonthBackground());
                break;

            case AFTER :
                getStyleClass().add(STYLE_CLASS_NEXT_MONTH);
                setBackground(getNextMonthBackground());
                break;
        }
        
        if (isSelected()) {
            getStyleClass().add(STYLE_CLASS_SELECTED);
        }
        
        if (isToday()) {
            getStyleClass().add(STYLE_CLASS_TODAY);
        }

        
        T item = null;
        if ((date == null) || empty) {
            setText("");
            setGraphic(null);
            
            this.dayOfMonthText.set("");
        }
        else {
            if (date.getDayOfMonth() == 1) {
                String text = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + " " + date.getDayOfMonth();
                this.dayOfMonthText.set(text);
            }
            else {
                this.dayOfMonthText.set(Integer.toString(date.getDayOfMonth()));
            }

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
