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
import com.leeboardtools.control.MonthlyView;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.TextAlignment;

/**
 * Default skin for {@link MonthlyView}.
 * <p>
 * The skin maintains a 6 rows by 7 columns grid of {@link DayCell}s. The day cells are 
 * optionally created via the {@link MonthlyView#dayCellFactoryProperty() } callback.
 * <p>
 * The day cells are in turn responsible for their contents/skin.
 * 
 * @author Albert Santos
 */
public class MonthlyViewSkin<T> extends SkinBase<MonthlyView> {
    
    private final GridPane gridPane = new GridPane();
    private final Node dayOfWeekNodes[] = new Node [7];
    
    private final static int NUMBER_DAY_CELL_ROWS = MonthlyView.NUMBER_DAY_CELL_ROWS;
    private final DayCell<T> dayCells [] = new DayCell [NUMBER_DAY_CELL_ROWS * 7];
    
    private final EventHandler<KeyEvent> keyTypedEventHandler = (KeyEvent event) -> {
        MonthlyView monthlyView = getSkinnable();
        LocalDate firstVisibleDate;
        int deltaDays;
        switch (event.getCode()) {
            case RIGHT :
                monthlyView.setActiveDate(monthlyView.getActiveDate().plusDays(1));
                break;
            case LEFT :
                monthlyView.setActiveDate(monthlyView.getActiveDate().minusDays(1));
                break;
            case UP :
                monthlyView.setActiveDate(monthlyView.getActiveDate().minusDays(7));
                break;
            case DOWN :
                monthlyView.setActiveDate(monthlyView.getActiveDate().plusDays(7));
                break;
                
            case PAGE_UP :
                firstVisibleDate = monthlyView.getFirstVisibleDate();
                deltaDays = 7 * NUMBER_DAY_CELL_ROWS;
                monthlyView.setActiveDate(monthlyView.getActiveDate().minusDays(deltaDays));
                monthlyView.makeDateInFirstRow(firstVisibleDate.minusDays(deltaDays));
                break;
                
            case PAGE_DOWN :
                firstVisibleDate = monthlyView.getFirstVisibleDate();
                deltaDays = 7 * NUMBER_DAY_CELL_ROWS;
                monthlyView.setActiveDate(monthlyView.getActiveDate().plusDays(7 * NUMBER_DAY_CELL_ROWS));
                monthlyView.makeDateInFirstRow(firstVisibleDate.plusDays(deltaDays));
                break;
            
            case ENTER :
            case SPACE :
                if (!monthlyView.isEditing()) {
                    monthlyView.startEdit();
                }
                break;
                
            default :
                return;
        }
        event.consume();            
    };
    
    private final EventHandler<MouseEvent> mousePressedEventHandler = (MouseEvent event) -> {
        if (getSkinnable().isFocusTraversable()) {
            getSkinnable().requestFocus();
        }
    };
    
    public MonthlyViewSkin(final MonthlyView control) {
        super(control);
        
        gridPane.getStyleClass().add("monthly-view-grid");
        //gridPane.setGridLinesVisible(true);
        
        RowConstraints headerRowConstraints = new RowConstraints();
        headerRowConstraints.setVgrow(Priority.NEVER);
        headerRowConstraints.setMinHeight(10);
        gridPane.getRowConstraints().add(headerRowConstraints);
        
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setVgrow(Priority.SOMETIMES);
        rowConstraints.setMinHeight(10);
        rowConstraints.setPrefHeight(30);
        
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setHgrow(Priority.SOMETIMES);
        columnConstraints.setHalignment(HPos.CENTER);
        columnConstraints.setMinWidth(10);
        columnConstraints.setPrefWidth(100);
        
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            int dayOfWeekIndex = dayOfWeek.getValue() - 1;
            this.dayOfWeekNodes[dayOfWeekIndex] = createDayOfWeekNode(dayOfWeek);
            gridPane.getColumnConstraints().add(columnConstraints);
        }
        
        for (int dayCellRow = 0; dayCellRow < NUMBER_DAY_CELL_ROWS; ++dayCellRow) {
            gridPane.getRowConstraints().add(rowConstraints);
            for (int col = 0; col < 7; ++col) {
                int index = getDayCellIndex(dayCellRow, col);
                
                DayCell dayCell = control.createDayCell();
                this.dayCells[index] = dayCell;
                gridPane.add(dayCell, col, dayCellRow + 1);
            }
        }
        
        updateFirstDayOfWeek();
        
        getChildren().add(gridPane);

        control.firstDayOfWeekProperty().addListener((e) -> { 
            updateFirstDayOfWeek(); 
        });
        
        control.firstVisibleDateProperty().addListener((e) -> {
            updateDatesDisplayed();
        });

        control.activeDateProperty().addListener((e) -> {
            updateDatesDisplayed();
        });
        
        control.addEventFilter(KeyEvent.KEY_PRESSED, keyTypedEventHandler);
        control.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
        
        control.setOnScroll((event) -> { 
            scrollActiveDate((int)event.getDeltaX(), 1);
            scrollActiveDate((int)event.getDeltaY(), 7);
        });
        
        updateDatesDisplayed();
        
        control.setSkinCallback(new MonthlyView.SkinCallback<T>() {
            @Override
            public void reloadDayRange(MonthlyView<T> view, int fromIndex, int toIndex) {
                updateDisplayedDates(fromIndex, toIndex);
            }
        });
    }
    
    final Node createDayOfWeekNode(DayOfWeek dayOfWeek) {
        Label label = new Label();
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setText(dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()));
        
        return label;
    }
    
    final int getDayCellIndex(int dayCellRow, int dayCellCol) {
        return dayCellRow * 7 + dayCellCol;
    }
    
    protected final void updateFirstDayOfWeek() {
        MonthlyView control = getSkinnable();
        DayOfWeek dayOfWeek = control.getFirstDayOfWeek();
        for (int i = 0; i < 7; ++i) {
            int dayOfWeekIndex = dayOfWeek.getValue() - 1;
            if (this.dayOfWeekNodes[dayOfWeekIndex] != null) {
                this.gridPane.add(this.dayOfWeekNodes[dayOfWeekIndex], i, 0);
            }
            
            dayOfWeek = dayOfWeek.plus(1);
        }
    }
    
    protected final void updateDatesDisplayed() {
        updateDisplayedDates(0, dayCells.length - 1);
    }
    
    protected final void updateDisplayedDates(int fromIndex, int toIndex) {
        MonthlyView control = getSkinnable();
        LocalDate date = control.getFirstVisibleDate();
        LocalDate today = LocalDate.now();
        LocalDate activeDate = control.getActiveDate();
        YearMonth activeYearMonth = YearMonth.from(activeDate);
        
        boolean isFullUpdate = (fromIndex <= 0) && ((toIndex + 1) >= dayCells.length);
        
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (fromIndex > 0) {
            date = date.plusDays(fromIndex);
        }
        
        if (toIndex >= dayCells.length) {
            toIndex = dayCells.length - 1;
        }
        
        for (int cellIndex = fromIndex; cellIndex <= toIndex; ++cellIndex) {
            DayCell<T> dayCell = dayCells[cellIndex];

            // Using setAll() because we want to clear all the styles...
            dayCell.getStyleClass().setAll("day-cell", "cell");

            YearMonth yearMonth = YearMonth.from(date);
            if (yearMonth.isBefore(activeYearMonth)) {
                dayCell.setActiveMonthRelation(DayCell.ActiveMonthRelation.BEFORE);
            }
            else if (yearMonth.isAfter(activeYearMonth)) {
                dayCell.setActiveMonthRelation(DayCell.ActiveMonthRelation.AFTER);
            }
            else {
                dayCell.setActiveMonthRelation(DayCell.ActiveMonthRelation.SAME);
            }

            if (!isFullUpdate) {
                // This forces the contents of the day cell to be updated.
                dayCells[cellIndex].updateItem(null, false);
            }
            dayCells[cellIndex].updateItem(date, false);

            dayCell.setIsToday(today.equals(date));

            // updateSelected() has to be called after updateItem() because it doesn't
            // do anything if the cell is empty (the initial state).
            dayCell.updateSelected(date.equals(activeDate));

            date = date.plusDays(1);
        }
    }
    
    protected void scrollActiveDate(int dir, int multiplier) {
        int delta;
        if (dir > 0) {
            delta = -1;
        }
        else if (dir < 0) {
            delta = 1;
        }
        else {
            return;
        }
        
        MonthlyView view = getSkinnable();
        view.setActiveDate(view.getActiveDate().plusDays(delta * multiplier));
    }
}
