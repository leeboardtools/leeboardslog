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
import com.leeboardtools.util.ListConverter;

/**
 * Base class for views that display data associated with multiple days.
 * @author Albert Santos
 * @param <T> Used to represent the type of the objects stored in the view's {@link ObservableMap},
 */
public abstract class MultiDayView <T> extends Control {
    
    
    /**
     * The underlying data model for the {@link MultiDayView}.
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
    
    
    /**
     * Defines an optional factory callback for creating the cells representing the
     * days of the month.
     */
    private ObjectProperty<Callback<MultiDayView, DayCell<T>>> dayCellFactory;

    public final ObjectProperty<Callback<MultiDayView, DayCell<T>>> dayCellFactoryProperty() {
        return dayCellFactory;
    }
    public final Callback<MultiDayView, DayCell<T>> getDayCellFactory() {
        return (dayCellFactory == null) ? null : dayCellFactory.get();
    }
    public final void setDayCellFactory(Callback<MultiDayView, DayCell<T>> factory) {
        if (this.dayCellFactory == null) {
            this.dayCellFactory = new SimpleObjectProperty<>(this, "dayCellFactory");
        }
        this.dayCellFactory.set(factory);
    }
    
    /**
     * Creates a day cell.
     * @return The day cell.
     */
    public DayCell<T> createDayCell() {
        Callback<MultiDayView, DayCell<T>> factory = getDayCellFactory();
        
        DayCell<T> dayCell;
        if (factory != null) {
            dayCell = factory.call(this);
        }
        else {
            dayCell = new DayCell<>(this);
        }
        
        if (dayCell != null) {
            dayCell.setupInnerCells();
        }
        return dayCell;
        
    }
    
    
    /**
     * Used to pass information to the header and body cell factory callbacks.
     * @param <T> 
     */
    public static class ContentsCellFactoryInfo<T> {
        public final MultiDayView view;
        public final DayCell<T> dayCell;
        
        public ContentsCellFactoryInfo(final MultiDayView view, final DayCell<T> dayCell) {
            this.view = view;
            this.dayCell = dayCell;
        }
    }
    
    /**
     * Defines an optional factory callback for creating the cells representing the
     * header of the contents of each day of the month. This cell is within the body of a DayCell.
     */
    private ObjectProperty<Callback<ContentsCellFactoryInfo<T>, Cell<T>>> headerCellFactory;

    public final ObjectProperty<Callback<ContentsCellFactoryInfo<T>, Cell<T>>> headerCellFactoryProperty() {
        return headerCellFactory;
    }
    public final Callback<ContentsCellFactoryInfo<T>, Cell<T>> getHeaderCellFactory() {
        return (headerCellFactory == null) ? null : headerCellFactory.get();
    }
    public final void setHeaderCellFactory(Callback<ContentsCellFactoryInfo<T>, Cell<T>> factory) {
        if (this.headerCellFactory == null) {
            this.headerCellFactory = new SimpleObjectProperty<>(this, "headerCellFactory");
        }
        this.headerCellFactory.set(factory);
    }
    

    /**
     * Creates a header cell for a {@link DayCell}. This should only be called from a day cell.
     * @param dayCell   The day cell the header cell is for.
     * @return The header cell, <code>null</code> if header cells are not used.
     */
    public Cell<T> createHeaderCell(DayCell<T> dayCell) {
        Callback<ContentsCellFactoryInfo<T>, Cell<T>> factory = getHeaderCellFactory();
        if (factory != null) {
            return factory.call(new ContentsCellFactoryInfo<>(this, dayCell));
        }
        
        return null;
    }
    
    
    /**
     * Defines an optional factory callback for creating the cells representing the
     * body of the contents of each day of the month. This cell is within the body of a DayCell.
     */
    private ObjectProperty<Callback<ContentsCellFactoryInfo<T>, Cell<T>>> bodyCellFactory;

    public final ObjectProperty<Callback<ContentsCellFactoryInfo<T>, Cell<T>>> bodyCellFactoryProperty() {
        return bodyCellFactory;
    }
    public final Callback<ContentsCellFactoryInfo<T>, Cell<T>> getBodyCellFactory() {
        return (bodyCellFactory == null) ? null : bodyCellFactory.get();
    }
    public final void setBodyCellFactory(Callback<ContentsCellFactoryInfo<T>, Cell<T>> factory) {
        if (this.bodyCellFactory == null) {
            this.bodyCellFactory = new SimpleObjectProperty<>(this, "bodyCellFactory");
        }
        this.bodyCellFactory.set(factory);
    }
    
    
    /**
     * Creates a body cell for a {@link DayCell}. This should only be called from a day cell.
     * @param dayCell   The day cell the body cell is for.
     * @return The body cell, <code>null</code> if a body cell is not used (it should be used!)
     */
    public Cell<T> createBodyCell(DayCell<T> dayCell) {
        Callback<ContentsCellFactoryInfo<T>, Cell<T>> factory = getBodyCellFactory();
        if (factory != null) {
            return factory.call(new ContentsCellFactoryInfo<>(this, dayCell));
        }
        
        ListConverter<T, String> converter = getStringListConverter();
        if (converter != null) {
            // A list based converter...
            return new ListViewCell<>(converter);
        }
        
        return null;
    }
    
    
    /**
     * Defines an optional converter for converting the objects of type T into a list of strings.
     */
    private ObjectProperty<ListConverter<T, String>> stringListConverter;
    
    public final ObjectProperty<ListConverter<T, String>> stringListConverterProperty() {
        return stringListConverter;
    }
    public final ListConverter<T, String> getStringListConverter() {
        return (this.stringListConverter == null) ? null : this.stringListConverter.get();
    }
    public final void setStringListConverter(ListConverter<T, String> converter) {
        if (this.stringListConverter == null) {
            this.stringListConverter = new SimpleObjectProperty<>(this, "stringListConverter");
        }
        this.stringListConverter.set(converter);
    }
    
    
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
