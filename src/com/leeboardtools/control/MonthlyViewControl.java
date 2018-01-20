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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableMap;
import javafx.scene.control.Cell;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.util.Callback;

//
// TODO: We have a DayCell, which is created by MonthlyViewSkin.
// DayCell creates a DayCellSkin.
// DayCell has a header row, with the date number and a header item.
// Question is how to handle the value portion. Should we have a DayValueCell,
// along with a DayValueCellSkin? 
// Or just stick in a Cell<?>...
// Look into StringConverter for the cells. Except Fx Cells use StringConverter to
// convert from edited text to the object value.
//
// What is the LogBook scenario?
// items: ObservableMap<LocalDate, TreeMap<LocalDateTime, LogEntry>>
// Need to convert TreeMap<LocalDateTime, LogEntry> to strings.

/**
 *
 * @author Albert Santos
 * @param <T> Used to represent the type of the objects stored in the view's {@link ObservableMap},
 */
public class MonthlyViewControl <T> extends Control {
    private static final String DEFAULT_STYLE_CLASS = "monthly-view-control";

    public final static int NUMBER_DAY_CELL_ROWS = 6;
    public final static int NUMBER_DAYS_VISIBLE = 7 * NUMBER_DAY_CELL_ROWS;
    
    /**
     * The id used to identify the node where the day of month is displayed.
     */
    public static final String DAY_OF_MONTH_NODE_ID = "DayOfMonth";
    
    /**
     * The id used to identify the node containing the header portion of the date cell.
     */
    public static final String DATE_HEADER_NODE_ID = "DateHeader";
    
    /**
     * The id used to identify the node containing the body of the date cell.
     */
    public static final String DATE_BODY_NODE_ID = "DateBody";
    
    // A Day item has three parts: A Labeled node with id DAY_OF_MONTH_NODE_ID,
    // a node with label DATE_HEADER_NODE_ID, and a node with label DATE_BODY_NODE_ID.
    // 
    
    private final int dayOfWeekColumnIndices[] = new int [7];
    private final DayOfWeek columnDayOfWeeks[] = new DayOfWeek [7];
    
    
    private final MapProperty<LocalDate, T> items = new SimpleMapProperty<>(this, "items");
    
    /**
     * @return The value of the items property.
     */
    public final ObservableMap<LocalDate, T> getItems() {
        return items.get();
    }
    
    /**
     * Sets the value of the items property.
     * @param value The value to set.
     */
    public final void setItems(ObservableMap<LocalDate, T> value) {
        items.set(value);
    }
    
    /**
     * The underlying data model for the MonthlyViewControl.
     * @return The items property.
     */
    public final MapProperty<LocalDate, T> itemsProperty() {
        return items;
    }
    
    
    private ObjectProperty<Callback<MonthlyViewControl, DayCell<T>>> dayCellFactory;
    
    /**
     * @return The value of the dayCellFactory property.
     */
    public final Callback<MonthlyViewControl, DayCell<T>> getDayCellFactory() {
        return (dayCellFactory == null) ? null : dayCellFactory.get();
    }
    
    /**
     * Sets the value of the dayCellFactory property.
     * @param factory The value to set.
     */
    public final void setDayCellFactory(Callback<MonthlyViewControl, DayCell<T>> factory) {
        if (this.dayCellFactory == null) {
            this.dayCellFactory = new SimpleObjectProperty<>(this, "dayCellFactory");
        }
        this.dayCellFactory.set(factory);
    }
    
    /**
     * Defines an optional factory callback for creating the cells representing the
     * days of the month.
     * @return The dayCellFactory property.
     */
    public final ObjectProperty<Callback<MonthlyViewControl, DayCell<T>>> dayCellFactoryProperty() {
        return dayCellFactory;
    }
    
    
    private ObjectProperty<Callback<MonthlyViewControl, Cell<T>>> cellFactory;
    
    /**
     * @return The value of the cellFactory property.
     */
    public final Callback<MonthlyViewControl, Cell<T>> getCellFactory() {
        return (cellFactory == null) ? null : cellFactory.get();
    }
    
    /**
     * Sets the value of the cellFactory property.
     * @param factory The value to set.
     */
    public final void setCellFactory(Callback<MonthlyViewControl, Cell<T>> factory) {
        if (this.cellFactory == null) {
            this.cellFactory = new SimpleObjectProperty<>(this, "cellFactory");
        }
        this.cellFactory.set(factory);
    }
    
    /**
     * Defines an optional factory callback for creating the cells representing the
     * contents of each day of the month. This cell is within the body of a DayCell.
     * @return The cellFactory property.
     */
    public final ObjectProperty<Callback<MonthlyViewControl, Cell<T>>> cellFactoryProperty() {
        return cellFactory;
    }
    
    
    private final ObjectProperty<DayOfWeek> firstDayOfWeek = new SimpleObjectProperty<>(this, "firstDayOfWeek", DayOfWeek.SUNDAY);

    /**
     * @return The value of the firstDayOfWeek property.
     */
    public final DayOfWeek getFirstDayOfWeek() {
        return firstDayOfWeek.get();
    }
    
    /**
     * Sets the value of the firstDayOfWeek property.
     * @param dayOfWeek The value to set.
     */
    public final void setFirstDayOfWeek(DayOfWeek dayOfWeek) {
        firstDayOfWeek.set(dayOfWeek);
    }
    
    /**
     * Defines the day of the week that appears in the left-most column.
     * @return The firstDayOfWeek property.
     */
    public final ObjectProperty<DayOfWeek> firstDayOfWeekProperty() {
        return firstDayOfWeek;
    }
    

    private final ReadOnlyObjectWrapper<LocalDate> firstVisibleDate = new ReadOnlyObjectWrapper<>(this, "firstVisibleDate");
    
    /**
     * @return The value of the firstVisibleDate property.
     */
    public final LocalDate getFirstVisibleDate() {
        return firstVisibleDate.get();
    }
    
    /**
     * Defines the first visible date that is visible in the view.
     * @return The property.
     */
    public final ReadOnlyObjectProperty firstVisibleDateProperty() {
        return firstVisibleDate.getReadOnlyProperty();
    }
    

    private final ReadOnlyObjectWrapper<LocalDate> lastVisibleDate = new ReadOnlyObjectWrapper<>(this, "lastVisibleDate");
    
    /**
     * @return The value of the lastVisibleDate property.
     */
    public final LocalDate getLastVisibleDate() {
        return lastVisibleDate.get();
    }
    
    /**
     * Defines the last visible date that is visible in the view.
     * @return The property.
     */
    public final ReadOnlyObjectProperty lastVisibleDateProperty() {
        return lastVisibleDate.getReadOnlyProperty();
    }
    
    
    private final ObjectProperty<LocalDate> activeDate = new SimpleObjectProperty<>(this, "activeDate");
    
    /**
     * @return The value of the activeDate property.
     */
    public final LocalDate getActiveDate() {
        return activeDate.get();
    }
    
    /**
     * Sets the value of the activeDate property.
     * @param date The date to set.
     */
    public final void setActiveDate(LocalDate date) {
        activeDate.set(date);
    }
    
    /**
     * Defines the active date within the view. The active date is normally highlighted,
     * receives the focus, and is always displayed.
     * @return The property.
     */
    public final ObjectProperty<LocalDate> activeDateProperty() {
        return activeDate;
    }
    
    
    // TODO Add DefaultSkin, implements SkinBase<MonthlyViewControl>
    // This is what sets up the grid, that way we can overload the grid.

    // selectedDate
    
    // TODO DayOfWeekCell - use DayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault());
    // TODO DateCell - VBox containing a Label and a ListView..
    
    // TODO Wheel: Scrolls by week.
    // TODO setMonth(Month, Year)
    // TODO showDate(LocalDate)
    // TODO selectedDate? Look into JavaFX selection model.
    // TODO look into CSS styling, how to apply it to the control.
    
    public MonthlyViewControl() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        
        this.firstDayOfWeek.addListener((e) -> {
            firstDayOfWeekUpdated();
        });
        
        this.activeDate.addListener((e)-> {
            activeDateUpdated();
        });
        
        // Initialize the active date to today.
        setActiveDate(LocalDate.now());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MonthlyViewSkin<>(this);
    }
    
    
    protected void firstDayOfWeekUpdated() {
        DayOfWeek dayOfWeek = getFirstDayOfWeek();
        for (int i = 0; i < 7; ++i) {
            int dayOfWeekIndex = dayOfWeek.getValue() - 1;
            this.columnDayOfWeeks[i] = dayOfWeek;
            this.dayOfWeekColumnIndices[dayOfWeekIndex] = i;
            dayOfWeek = dayOfWeek.plus(1);
        }
    }
    
    protected void activeDateUpdated() {
        LocalDate date = this.activeDate.get();
        if (this.firstVisibleDate.get() != null) {
            if (date.isBefore(this.firstVisibleDate.get())) {
                // If we're within a week of the first visible date, just
                // make the active date visible in the first week (basically just
                // scroll a line.
                // We're -8 because the test is after..
                if (date.isAfter(this.firstVisibleDate.get().minusDays(8))) {
                    makeDateInFirstRow(date);
                    return;
                }
            }
            else if (date.isAfter(this.lastVisibleDate.get())) {
                // Same for the last visible date.
                if (date.isBefore(this.lastVisibleDate.get().plusDays(8))) {
                    makeDateInFirstRow(date.minusDays(NUMBER_DAYS_VISIBLE));
                    return;
                }
            }
            else {
                // We're currently visible...
                return;
            }
        }
        
        // We're not visible, make the month fully visible.
        YearMonth yearMonth = YearMonth.from(date);
        makeDateInFirstRow(yearMonth.atDay(1));
    }
    

    /**
     * Adjusts the firstVisibleDate property so a given date is in the first row.
     * @param date The date of interest.
     */
    public void makeDateInFirstRow(LocalDate date) {
        LocalDate newFirstVisibleDate = date;
        while (!newFirstVisibleDate.getDayOfWeek().equals(this.firstDayOfWeek.get())) {
            newFirstVisibleDate = newFirstVisibleDate.minusDays(1);
        }
        
        if ((this.firstVisibleDate.get() == null) || !this.firstVisibleDate.get().equals(newFirstVisibleDate)) {
            this.firstVisibleDate.set(newFirstVisibleDate);
            this.lastVisibleDate.set(newFirstVisibleDate.plusDays(NUMBER_DAYS_VISIBLE - 1));
        }
    }
    
    /**
     * Retrieves the column index of a given day of week.
     * @param dayOfWeek The day of week of interest.
     * @return The column index, &ge; 0 and &lt; 7.
     */
    public final int getDayOfWeekColumnIndex(DayOfWeek dayOfWeek) {
        return dayOfWeekColumnIndices[dayOfWeek.getValue() - 1];
    }
    
    /**
     * Retrieves the day of week of a given column.
     * @param index The index of the column, this must be &ge; 0 and &lt; 7.
     * @return The day of the week.
     */
    public final DayOfWeek getColumnDayOfWeek(int index) {
        return columnDayOfWeeks[index];
    }
    
    /**
     * Retrieves the local date of a given cell. The first visible date is at column 0, row 0.
     * @param columnIndex   The index of the column.
     * @param rowIndex  The index of the row.
     * @return The local date.
     */
    public final LocalDate getDateOfCell(int columnIndex, int rowIndex) {
        return this.firstVisibleDate.get().plusDays(rowIndex * 7 + columnIndex);
    }

    /**
     * Determines if a date is currently visible in the grid.
     * @param date  The date of interest.
     * @return <code>true</code> if date is visible.
     */
    public final boolean isDateVisible(LocalDate date) {
        return !date.isBefore(firstVisibleDate.get()) && !date.isAfter(lastVisibleDate.get());
    }
    
    
}
