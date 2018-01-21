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
import java.time.format.TextStyle;
import java.util.Locale;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;

/**
 * Default skin for {@link MonthlyViewControl}.
 * <p>
 * The skin maintains a 6 rows by 7 columns grid of {@link DayCell}s. The day cells are 
 * optionally created via the {@link MonthlyViewControl#dayCellFactoryProperty() } callback.
 * <p>
 * The day cells are in turn responsible for their contents/skin.
 * 
 * @author Albert Santos
 */
public class MonthlyViewSkin<T> extends SkinBase<MonthlyViewControl> {
    
    private final GridPane gridPane = new GridPane();
    private final Node dayOfWeekNodes[] = new Node [7];
    
    private final static int NUMBER_DAY_CELL_ROWS = MonthlyViewControl.NUMBER_DAY_CELL_ROWS;
    private final DayCell<T> dayCells [] = new DayCell [NUMBER_DAY_CELL_ROWS * 7];
    
    public MonthlyViewSkin(final MonthlyViewControl control) {
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
                DayCell dayCell = createDayCell(dayCellRow, col);
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
        
        updateDatesDisplayed();
    }
    
    final Node createDayOfWeekNode(DayOfWeek dayOfWeek) {
        Label label = new Label();
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setText(dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()));
        
        return label;
    }
    
    final DayCell<T> createDayCell(int dayCellRow, int dayCellCol) {
        MonthlyViewControl<T> control = getSkinnable();
        Callback<MonthlyViewControl, DayCell<T>> factory = control.getDayCellFactory();
        if (factory != null) {
            return factory.call(control);
        }
        return new DayCell<>(control);
    }
    
    final int getDayCellIndex(int dayCellRow, int dayCellCol) {
        return dayCellRow * 7 + dayCellCol;
    }
    
    protected final void updateFirstDayOfWeek() {
        MonthlyViewControl control = getSkinnable();
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
        MonthlyViewControl control = getSkinnable();
        LocalDate date = control.getFirstVisibleDate();
        LocalDate today = LocalDate.now();
        LocalDate activeDate = control.getActiveDate();
        YearMonth activeYearMonth = YearMonth.from(activeDate);
        
        int cellIndex = 0;
        for (int i = 0; i < 7; ++i) {
            for (int row = 0; row < NUMBER_DAY_CELL_ROWS; ++row) {
                DayCell<T> dayCell = dayCells[cellIndex];
                
                // Using setAll() because we want to clear all the styles...
                dayCell.getStyleClass().setAll("day-cell", "cell");
                if (today.equals(date)) {
                    dayCell.getStyleClass().add("today");
                }
                if (today.equals(activeDate)) {
                    dayCell.getStyleClass().add("active");
                }
                
                YearMonth yearMonth = YearMonth.from(date);
                if (yearMonth.isBefore(activeYearMonth)) {
                    dayCell.getStyleClass().add("previous-month");
                }
                else if (yearMonth.isAfter(activeYearMonth)) {
                    dayCell.getStyleClass().add("next-month");
                }
                
                dayCells[cellIndex].updateItem(date, false);
                
                ++cellIndex;
                date = date.plusDays(1);
            }
        }
    }
}
