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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableMap;
import javafx.scene.control.Skin;

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
// items: ObservableMap<LocalDate, DayLogEntry>
// Need to be able to convert DayLogEntry into a collection
// of strings.

/**
 *
 * @author Albert Santos
 * @param <T> Used to represent the type of the objects stored in the view's {@link ObservableMap},
 */
public class MonthlyView <T> extends MultiDayView<T> {
    private static final String DEFAULT_STYLE_CLASS = "monthly-view-control";

    public final static int NUMBER_DAY_CELL_ROWS = 6;
    public final static int NUMBER_DAYS_VISIBLE = 7 * NUMBER_DAY_CELL_ROWS;
    
    
    // A Day item has three parts: A Labeled node with id DAY_OF_MONTH_NODE_ID,
    // a node with label DATE_HEADER_NODE_ID, and a node with label DATE_BODY_NODE_ID.
    // 
    
    private final int dayOfWeekColumnIndices[] = new int [7];
    private final DayOfWeek columnDayOfWeeks[] = new DayOfWeek [7];


    //
    //--------------------------------------------------------------------------
    // firstDayOfWeek Property
    
    /**
     * Defines the day of the week that appears in the left-most column.
     */
    private final ObjectProperty<DayOfWeek> firstDayOfWeek = new SimpleObjectProperty<>(this, "firstDayOfWeek", DayOfWeek.SUNDAY);
    public final DayOfWeek getFirstDayOfWeek() {
        return firstDayOfWeek.get();
    }
    public final void setFirstDayOfWeek(DayOfWeek dayOfWeek) {
        firstDayOfWeek.set(dayOfWeek);
    }
    public final ObjectProperty<DayOfWeek> firstDayOfWeekProperty() {
        return firstDayOfWeek;
    }
    

    // TODO Add DefaultSkin, implements SkinBase<MonthlyView>
    // This is what sets up the grid, that way we can overload the grid.

    // selectedDate
    
    // TODO DayOfWeekCell - use DayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault());
    // TODO DateCell - VBox containing a Label and a ListView..
    
    // TODO Wheel: Scrolls by week.
    // TODO setMonth(Month, Year)
    // TODO showDate(LocalDate)
    // TODO selectedDate? Look into JavaFX selection model.
    // TODO look into CSS styling, how to apply it to the control.
    
    public MonthlyView() {
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