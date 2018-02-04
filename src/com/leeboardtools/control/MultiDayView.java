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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.MapChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;

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
    
    private final MapChangeListener<LocalDate, T> itemsListener = (MapChangeListener.Change<? extends LocalDate, ? extends T> change) -> {
        reloadDate(change.getKey());
    };
    
    
    /**
     * Reloads the days within two dates, inclusive.
     * @param dateA The first date.
     * @param dateB The second date.
     * @return <code>true</code> if any visible dates were reloaded.
     */
    public boolean reloadDateRange(LocalDate dateA, LocalDate dateB) {
        if (dateA.isAfter(dateB)) {
            LocalDate tmp = dateA;
            dateA = dateB;
            dateB = tmp;
        }
        
        final LocalDate firstVisDate = this.firstVisibleDate.get();
        final LocalDate lastVisDate = this.lastVisibleDate.get();
        if (dateA.isAfter(lastVisDate) || dateB.isBefore(firstVisDate)) {
            return false;
        }

        if (dateA.isBefore(firstVisDate)) {
            dateA = firstVisDate;
        }
        if (dateB.isAfter(lastVisDate)) {
            dateB = lastVisDate;
        }
        
        doDateRangeReload(dateA, dateB);
        return true;
    }
    
    /**
     * Reloads a date.
     * @param date The date of interest.
     * @return <code>true</code> if the date was visible.
     */
    public boolean reloadDate(LocalDate date) {
        return reloadDateRange(date, date);
    }
    
    protected abstract void doDateRangeReload(LocalDate fromDate, LocalDate toDate);
    
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
     * @param <T>   The object type of the MultiDayView.
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
            return new ItemListCell<>(converter);
        }
        
        // TODO Should we return a Cell???
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
     * Determines if a date is currently visible.
     * @param date  The date of interest.
     * @return <code>true</code> if date is visible (between firstVisibleDate and lastVisibleDate, inclusive).
     */
    public final boolean isDateVisible(LocalDate date) {
        if ((firstVisibleDate.get() == null) || (lastVisibleDate.get() == null) || (date == null)) {
            return false;
        }
        return !firstVisibleDate.get().isAfter(date) && !lastVisibleDate.get().isBefore(date);
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
    
    
    /**
     * Determines if the view is potentially editable.
     */
    final BooleanProperty editable = new SimpleBooleanProperty(this, "editable");
    
    public final BooleanProperty editableProperty() {
        return editable;
    }
    public final boolean isEditable() {
        return editable.get();
    }
    public final void setEditable(boolean value) {
        editable.set(value);
    }
    
    
    /**
     * Read-only property that's <code>true</code> if the active cell is currently being edited.
     */
    final ReadOnlyBooleanWrapper editing = new ReadOnlyBooleanWrapper(this, "editing", false);
    
    public final ReadOnlyBooleanProperty editingProperty() {
        return editing.getReadOnlyProperty();
    }
    public final boolean isEditing() {
        return editing.get();
    }
    
    
    private LocalDate editingDate;
    
    protected MultiDayView() {
        this.activeDate.addListener((property, oldValue, newValue) -> {
            cancelEdit();
        });
        
        this.items.addListener((property, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.removeListener(itemsListener);
            }
            if (newValue != null) {
                newValue.addListener(itemsListener);
            }
            reloadDateRange(getFirstVisibleDate(), getLastVisibleDate());
        });
    }
    
    
    /**
     * Event used for the edit related events in MultiDayViews.
     * @param <T> The object type from the MultiDayView.
     */
    public static class EditEvent <T> extends Event {
        private final LocalDate editDate;
        private final T newValue;
        
        public EditEvent(MultiDayView<T> source, EventType<? extends MultiDayView.EditEvent<T>> eventType,
                LocalDate editDate, T newValue) {
            super(source, Event.NULL_SOURCE_TARGET, eventType);
            this.editDate = editDate;
            this.newValue = newValue;
        }

        @Override
        public MultiDayView<T> getSource() {
            return (MultiDayView<T>)super.getSource();
        }
        
        public final LocalDate getEditDate() {
            return editDate;
        }
        
        public final T getNewValue() {
            return newValue;
        }
    }
    
    private static final EventType<?> EDIT_ANY_EVENT = new EventType(Event.ANY, "MULTI_DAY_VIEW_EDIT");
    public static final <T> EventType<EditEvent<T>> editAnyEventType() {
        return (EventType<EditEvent<T>>)EDIT_ANY_EVENT;
    }
    
    private static final EventType<?> EDIT_START_EVENT = new EventType(EDIT_ANY_EVENT, "EDIT_START");
    public static final <T> EventType<EditEvent<T>> editStartEventType() {
        return (EventType<EditEvent<T>>)EDIT_START_EVENT;
    }
    
    private static final EventType<?> EDIT_COMMIT_EVENT = new EventType(EDIT_ANY_EVENT, "EDIT_COMMIT");
    public static final <T> EventType<EditEvent<T>> editCommitEventType() {
        return (EventType<EditEvent<T>>)EDIT_COMMIT_EVENT;
    }
    
    private static final EventType<?> EDIT_CANCEL_EVENT = new EventType(EDIT_ANY_EVENT, "EDIT_CANCEL");
    public static final <T> EventType<EditEvent<T>> editCancelEventType() {
        return (EventType<EditEvent<T>>)EDIT_CANCEL_EVENT;
    }
    
    
    /**
     * Starts editing of the item with the active date.
     */
    public void startEdit() {
        if (isEditing()) {
            return;
        }
        
        this.editingDate = this.activeDate.get();
        this.editing.set(true);
        fireEvent(new EditEvent(this, editStartEventType(), this.editingDate, null));
    }
    
    public void commitEdit(T newItem) {
        if (!isEditing()) {
            return;
        }
        
        LocalDate date = this.editingDate;
        this.editingDate = null;
        this.editing.set(false);
        fireEvent(new EditEvent(this, editCommitEventType(), date, newItem));
    }
    
    public void cancelEdit() {
        if (!isEditing()) {
            return;
        }
        
        LocalDate date = this.editingDate;
        this.editingDate = null;
        this.editing.set(false);
        fireEvent(new EditEvent(this, editCancelEventType(), date, null));
    }
    
    
    
    /**
     * Event handler fired when editing is started on the active date.
     */
    private ObjectProperty<EventHandler<EditEvent<T>>> onEditStart;
    
    public final ObjectProperty<EventHandler<EditEvent<T>>> onEditStartProperty() {
        if (onEditStart == null) {
            onEditStart = new SimpleObjectProperty<EventHandler<EditEvent<T>>>(this, "onEditStart", null) {
                @Override 
                protected void invalidated() {
                    setEventHandler(editStartEventType(), get());
                }
            };
        }
        return onEditStart;
    }
    public final EventHandler<EditEvent<T>> getOnEditStart() {
        return (onEditStart == null) ? null : onEditStart.get();
    }
    public final void setOnEditStart(EventHandler<EditEvent<T>> handler) {
        onEditStartProperty().set(handler);
    }
    
    
    /**
     * Event handler fired when editing is committed on the active date.
     */
    private ObjectProperty<EventHandler<EditEvent<T>>> onEditCommit;
    
    public final ObjectProperty<EventHandler<EditEvent<T>>> onEditCommitProperty() {
        if (onEditCommit == null) {
            onEditCommit = new SimpleObjectProperty<EventHandler<EditEvent<T>>>(this, "onEditCommit", null) {
                @Override 
                protected void invalidated() {
                    setEventHandler(editCommitEventType(), get());
                }
            };
        }
        return onEditCommit;
    }
    public final EventHandler<EditEvent<T>> getOnEditCommit() {
        return (onEditCommit == null) ? null : onEditCommit.get();
    }
    public final void setOnEditCommit(EventHandler<EditEvent<T>> handler) {
        onEditCommitProperty().set(handler);
    }
    
    
    /**
     * Event handler fired when editing is canceled on the active date.
     */
    private ObjectProperty<EventHandler<EditEvent<T>>> onEditCancel;
    
    public final ObjectProperty<EventHandler<EditEvent<T>>> onEditCancelProperty() {
        if (onEditCancel == null) {
            onEditCancel = new SimpleObjectProperty<EventHandler<EditEvent<T>>>(this, "onEditCancel", null) {
                @Override 
                protected void invalidated() {
                    setEventHandler(editCancelEventType(), get());
                }
            };
        }
        return onEditCancel;
    }
    public final EventHandler<EditEvent<T>> getOnEditCancel() {
        return (onEditCancel == null) ? null : onEditCancel.get();
    }
    public final void setOnEditCancel(EventHandler<EditEvent<T>> handler) {
        onEditCancelProperty().set(handler);
    }
}
