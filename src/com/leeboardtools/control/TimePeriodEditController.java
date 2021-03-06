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

import com.leeboardtools.util.ResourceSource;
import com.leeboardtools.util.TimePeriod;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.TreeSet;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;

/**
 *
 * @author Albert Santos
 */
public class TimePeriodEditController {
    private final DatePicker startDatePicker;

    private final ComboBox<String> startTimePicker;
    private String allDayTimeLabel;
    private DateTimeFormatter timeFormatter;
    
    private final DatePicker endDatePicker;
    private final ComboBox<String> endTimePicker;

    private final ChoiceBox<String> timeZonePicker;
    private String defaultZoneIdLabel;
    
    private final ObjectProperty<TimePeriod> timePeriod = new SimpleObjectProperty<>(this, "timePeriod", null);

    public final ObjectProperty<TimePeriod> timePeriodProperty() {
        return timePeriod;
    }
    public final TimePeriod getTimePeriod() {
        return timePeriod.get();
    }
    public final void setTimePeriod(TimePeriod value) {
        timePeriod.set(value);
    }
    
    private final ObjectProperty<ZoneId> zoneId = new SimpleObjectProperty<>(this, "zoneId", null);
    
    public final ObjectProperty<ZoneId> zoneIdProperty() {
        return zoneId;
    }
    public final ZoneId getZoneId() {
        return zoneId.get();
    }
    public final void setZoneId(ZoneId value) {
        zoneId.set(value);
    }
    
    
    private boolean inUpdateFromValues = false;
    
    
    
    public TimePeriodEditController(DatePicker startDatePicker, ComboBox<String> startTimePicker, 
            DatePicker endDatePicker, ComboBox<String> endTimePicker,
            ChoiceBox<String> timeZonePicker) {
        this.startDatePicker = startDatePicker;
        this.startTimePicker = startTimePicker;
        this.endDatePicker = endDatePicker;
        this.endTimePicker = endTimePicker;
        this.timeZonePicker = timeZonePicker;
        
        this.timePeriod.addListener((property, oldValue, newValue) -> {
            updateFromValues();
        });
        this.zoneId.addListener((property, oldValue, newValue) -> {
            updateFromValues();
        });
        
        setupControls();
    }
    
    private void setupControls() {
        this.startDatePicker.setOnAction((event) -> {
            updateTimePeriod(this.startDatePicker.getValue(), null, null, null, null, false);
        });

        this.timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
        this.allDayTimeLabel = ResourceSource.getString("LBLabel.allDay");
        this.startTimePicker.getItems().add(this.allDayTimeLabel);
        addTimeIncrements(this.startTimePicker.getItems(), this.timeFormatter);
        this.startTimePicker.setOnAction((event)-> {
            updateFromStartTimeEdit();
        });
        
        this.endDatePicker.setOnAction((event) -> {
            updateTimePeriod(null, null, this.endDatePicker.getValue(), null, null, false);
        });
        this.endTimePicker.setOnAction((event) -> {
            updateFromEndTimeEdit();
        });
        addTimeIncrements(this.endTimePicker.getItems(), this.timeFormatter);
        
        if (this.timeZonePicker != null) {
            this.timeZonePicker.setOnAction((event) -> {
                updateFromZoneIdEdit();
            });

            ObservableList<String> zoneIdList = this.timeZonePicker.getItems();
            this.defaultZoneIdLabel = ResourceSource.getString("LBLabel.systemZoneId", ZoneId.systemDefault().getId());
            
            zoneIdList.add(this.defaultZoneIdLabel);
            
            TreeSet<String> sortedNames = new TreeSet<>();
            sortedNames.addAll(ZoneId.getAvailableZoneIds());
            zoneIdList.addAll(sortedNames);
        }
    }
    
    public static void addTimeIncrements(List<String> items, DateTimeFormatter formatter) {
        for (int i = 0; i < 24; ++i) {
            items.add(LocalTime.of(i, 0).format(formatter));
            items.add(LocalTime.of(i, 30).format(formatter));
        }
    }
    
    protected void updateFromStartTimeEdit() {
        TimePeriod currentPeriod = this.getTimePeriod();
        if (currentPeriod == null) {
            return;
        }
        
        String text = this.startTimePicker.getValue();
        if (this.allDayTimeLabel.equalsIgnoreCase(text)) {
            ZoneId zoneIdToUse = getZoneIdToUse();
            LocalDate startDate = currentPeriod.getStartInstant().atZone(zoneIdToUse).toLocalDate();
            LocalDate endDate = currentPeriod.getEndInstant().atZone(zoneIdToUse).toLocalDate();
            setTimePeriod(TimePeriod.fromEdgeDates(startDate, endDate));
        }
        else {
            try {
                LocalTime startTime = LocalTime.parse(text, this.timeFormatter);
                updateTimePeriod(null, startTime, null, null, null, false);
            } catch (DateTimeParseException ex) {
                // This will restore the original value.
                updateFromValues();
            }
        }
    }
    
    protected void updateFromEndTimeEdit() {
        try {
            LocalTime endTime = LocalTime.parse(this.endTimePicker.getValue(), this.timeFormatter);
            updateTimePeriod(null, null, null, endTime, null, false);
        } catch (DateTimeParseException ex) {
            // This will restore the original value.
            updateFromValues();
        }
    }
    
    protected void updateFromZoneIdEdit() {
        String zoneIdText = this.timeZonePicker.getValue();
        if (this.defaultZoneIdLabel.equalsIgnoreCase(zoneIdText)) {
            updateTimePeriod(null, null, null, null, null, true);
        }
        else {
            ZoneId newZoneId = ZoneId.of(zoneIdText);
            updateTimePeriod(null, null, null, null, newZoneId, true);
        }
    }
    
    protected void updateTimePeriod(LocalDate newStartDate, LocalTime newStartTime, LocalDate newEndDate, LocalTime newEndTime, 
            ZoneId newZoneId, boolean isNewZoneId) {
        TimePeriod currentPeriod = this.getTimePeriod();
        if (currentPeriod == null) {
            return;
        }
        
        ZoneId currentZoneId = getZoneIdToUse();
        
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        LocalDate startDate = null;
        LocalTime startTime = null;
        LocalDate endDate = null;
        LocalTime endTime = null;
        
        boolean isAllDay;
        if (currentPeriod.isFullDays()) {
            startDate = currentPeriod.getFirstFullDay();
            endDate = currentPeriod.getLastFullDay();
            isAllDay = true;
        }
        else {
            startDateTime = currentPeriod.getStartInstant().atZone(currentZoneId).toLocalDateTime();
            endDateTime = currentPeriod.getEndInstant().atZone(currentZoneId).toLocalDateTime();

            startDate = startDateTime.toLocalDate();
            startTime = startDateTime.toLocalTime();

            endDate = endDateTime.toLocalDate();
            endTime = endDateTime.toLocalTime();

            isAllDay = false;
        }

        if (newStartDate != null) {
            if (newStartDate.isAfter(endDate)) {
                endDate = newStartDate;
            }
            startDate = newStartDate;
        }
        if (newEndDate != null) {
            if (newEndDate.isBefore(startDate)) {
                startDate = newEndDate;
            }
            endDate = newEndDate;
        }
        
        if (newStartTime != null) {
            if (startDate.equals(endDate)) {
                if ((endTime == null) || newStartTime.isAfter(endTime) || newStartTime.equals(endTime)) {
                    endTime = newStartTime.plusHours(1);
                }
            }
            startTime = newStartTime;
            
            if (isAllDay && !LocalTime.MIDNIGHT.equals(startTime)) {
                // We're coming from all day, so we didn't have an end time so set one.
                if (startDate.equals(endDate)) {
                    endTime = startTime.plusHours(1);
                }
                else {
                    endTime = startTime;
                }
            }
        }
        if (newEndTime != null) {
            if (startDate.equals(endDate)) {
                if (newEndTime.isBefore(startTime) || newEndTime.equals(startTime)) {
                    startTime = newEndTime.minusHours(1);
                }
            }
            endTime = newEndTime;
            
            // Don't have to worry about starting from isAllDay because we won't change
            // the end time unless we're already not all day...
        }

        if (isNewZoneId) {
            this.setZoneId(newZoneId);
            currentZoneId = newZoneId;
        }
        
        if (startTime != null) {
            startDateTime = LocalDateTime.of(startDate, startTime);
            endDateTime = LocalDateTime.of(endDate, endTime);
            this.setTimePeriod(TimePeriod.fromEdgeTimes(startDateTime, endDateTime, currentZoneId));
        }
        else {
            this.setTimePeriod(TimePeriod.fromEdgeDates(startDate, endDate));
        }
    }
    
    protected final ZoneId getZoneIdToUse() {
        ZoneId currentZoneId = this.getZoneId();
        return (currentZoneId == null) ? ZoneId.systemDefault() : currentZoneId;
    }
    
    protected void updateFromValues() {
        if (this.inUpdateFromValues) {
            return;
        }
        
        try {
            this.inUpdateFromValues = true;
            
            TimePeriod currentPeriod = this.getTimePeriod();
            if (currentPeriod != null) {
                ZoneId currentZoneId = this.getZoneId();
                ZoneId zoneIdToUse = (currentZoneId == null) ? ZoneId.systemDefault() : currentZoneId;
                
                LocalDate startDate;
                LocalDate endDate;
                if (currentPeriod.isFullDays()) {
                    startDate = currentPeriod.getFirstFullDay();
                    endDate = currentPeriod.getLastFullDay();

                    this.startTimePicker.setValue(this.allDayTimeLabel);
                    this.endTimePicker.setDisable(true);
                }
                else {
                    LocalDateTime startDateTime = currentPeriod.getStartInstant().atZone(zoneIdToUse).toLocalDateTime();
                    startDate = startDateTime.toLocalDate();
                    LocalTime startTime = startDateTime.toLocalTime();

                    LocalDateTime endDateTime = currentPeriod.getEndInstant().atZone(zoneIdToUse).toLocalDateTime();
                    endDate = endDateTime.toLocalDate();
                    LocalTime endTime = endDateTime.toLocalTime();

                    this.startTimePicker.setValue(startTime.format(this.timeFormatter));

                    this.endTimePicker.setDisable(false);
                    this.endTimePicker.setValue(endTime.format(this.timeFormatter));
                }

                this.startDatePicker.setDisable(false);
                this.startDatePicker.setValue(startDate);

                this.startTimePicker.setDisable(false);

                this.endDatePicker.setDisable(false);
                this.endDatePicker.setValue(endDate);

                if (this.timeZonePicker != null) {
                    if (currentZoneId == null) {
                        this.timeZonePicker.setValue(defaultZoneIdLabel);
                    }
                    else {
                        this.timeZonePicker.setValue(currentZoneId.getId());
                    }
                    this.timeZonePicker.setDisable(false);
                }
            }
            else {
                this.startDatePicker.setDisable(true);
                this.startTimePicker.setDisable(true);
                this.endDatePicker.setDisable(true);
                this.endTimePicker.setDisable(true);

                if (this.timeZonePicker != null) {
                    this.timeZonePicker.setDisable(true);
                }
            }
        } finally {
            this.inUpdateFromValues = false;
        }
    }
}
