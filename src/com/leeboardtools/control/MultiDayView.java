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

import com.leeboardtools.util.StringListConverter;
import java.time.LocalDate;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableMap;
import javafx.scene.control.Cell;
import javafx.scene.control.Control;
import javafx.util.Callback;

/**
 * Base class for views that display data associated with multiple days.
 * @author Albert Santos
 * @param <T> Used to represent the type of the objects stored in the view's {@link ObservableMap},
 */
public abstract class MultiDayView <T> extends Control {

    /**
     * The id used to identify the node where the day of month is displayed.
     */
    public static final String DAY_OF_MONTH_NODE_ID = "DayOfMonth";
    /**
     * The id used to identify the node containing the body of the date cell.
     */
    public static final String DATE_BODY_NODE_ID = "DateBody";
    /**
     * The id used to identify the node containing the header portion of the date cell.
     */
    public static final String DATE_HEADER_NODE_ID = "DateHeader";
    
    
    //
    //--------------------------------------------------------------------------
    // items Property
    
    /**
     * The underlying data model for the {@link MonthlyViewControl}.
     */
    private final MapProperty<LocalDate, T> items = new SimpleMapProperty<>(this, "items");

    public final MapProperty<LocalDate, T> itemsProperty() {
        return items;
    }
    public final ObservableMap<LocalDate, T> getItems() {
        return items.get();
    }
    public final void setItems(ObservableMap<LocalDate, T> value) {
        items.set(value);
    }
    
    
    //
    //--------------------------------------------------------------------------
    // dayCellFactory Property
    
    /**
     * Defines an optional factory callback for creating the cells representing the
     * days of the month.
     */
    private ObjectProperty<Callback<MonthlyViewControl, DayCell<T>>> dayCellFactory;

    public final ObjectProperty<Callback<MonthlyViewControl, DayCell<T>>> dayCellFactoryProperty() {
        return dayCellFactory;
    }
    public final Callback<MonthlyViewControl, DayCell<T>> getDayCellFactory() {
        return (dayCellFactory == null) ? null : dayCellFactory.get();
    }
    public final void setDayCellFactory(Callback<MonthlyViewControl, DayCell<T>> factory) {
        if (this.dayCellFactory == null) {
            this.dayCellFactory = new SimpleObjectProperty<>(this, "dayCellFactory");
        }
        this.dayCellFactory.set(factory);
    }
    
    
    //
    //--------------------------------------------------------------------------
    // cellFactory Property
    /**
     * Defines an optional factory callback for creating the cells representing the
     * contents of each day of the month. This cell is within the body of a DayCell.
     */
    private ObjectProperty<Callback<MonthlyViewControl, Cell<T>>> cellFactory;

    public final ObjectProperty<Callback<MonthlyViewControl, Cell<T>>> cellFactoryProperty() {
        return cellFactory;
    }
    public final Callback<MonthlyViewControl, Cell<T>> getCellFactory() {
        return (cellFactory == null) ? null : cellFactory.get();
    }
    public final void setCellFactory(Callback<MonthlyViewControl, Cell<T>> factory) {
        if (this.cellFactory == null) {
            this.cellFactory = new SimpleObjectProperty<>(this, "cellFactory");
        }
        this.cellFactory.set(factory);
    }
    


    //
    //--------------------------------------------------------------------------
    // stringListConverter Property
    /**
     * Defines an optional converter for converting the objects of type T into a list of strings.
     */
    private ObjectProperty<StringListConverter<T>> stringListConverter;
    
    public final ObjectProperty<StringListConverter<T>> stringListConverterProperty() {
        return stringListConverter;
    }
    public final StringListConverter<T> getStringListConverter() {
        return (this.stringListConverter == null) ? null : this.stringListConverter.get();
    }
    public final void setStringListConverter(StringListConverter<T> converter) {
        if (this.stringListConverter == null) {
            this.stringListConverter = new SimpleObjectProperty<>(this, "stringListConverter");
        }
        this.stringListConverter.set(converter);
    }
    
    
    //
    //--------------------------------------------------------------------------
    // firstVisibleDate Read-only Property
    /**
     * Defines the first visible date that is visible in the view.
     */
    final ReadOnlyObjectWrapper<LocalDate> firstVisibleDate = new ReadOnlyObjectWrapper<>(this, "firstVisibleDate");
    
    public final ReadOnlyObjectProperty firstVisibleDateProperty() {
        return firstVisibleDate.getReadOnlyProperty();
    }
    public final LocalDate getFirstVisibleDate() {
        // LocalDate is immutable, we don't have to call getReadOnlyProperty()...
        return firstVisibleDate.get();
    }
    
    

    //
    //--------------------------------------------------------------------------
    // lastVisibleDate Read-only Property
    /**
     * Defines the last visible date that is visible in the view.
     */
    final ReadOnlyObjectWrapper<LocalDate> lastVisibleDate = new ReadOnlyObjectWrapper<>(this, "lastVisibleDate");
    
    public final ReadOnlyObjectProperty lastVisibleDateProperty() {
        return lastVisibleDate.getReadOnlyProperty();
    }
    public final LocalDate getLastVisibleDate() {
        // LocalDate is immutable, we don't have to call getReadOnlyProperty()...
        return lastVisibleDate.get();
    }
    
    
    
    //
    //--------------------------------------------------------------------------
    // activeDate Property
    /**
     * Defines the active date within the view. The active date is normally highlighted,
     * receives the focus, and is always displayed.
     */
    final ObjectProperty<LocalDate> activeDate = new SimpleObjectProperty<>(this, "activeDate");
    
    public final ObjectProperty<LocalDate> activeDateProperty() {
        return activeDate;
    }
    public final LocalDate getActiveDate() {
        return activeDate.get();
    }
    public final void setActiveDate(LocalDate date) {
        activeDate.set(date);
    }
    
    
    
}
