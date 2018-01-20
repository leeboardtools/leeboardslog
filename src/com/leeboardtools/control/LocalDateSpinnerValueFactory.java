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
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

/**
 * Outer class for {@link LocalDate} {@link SpinnerValueFactory}s, since JavaFX's
 * SpinnerValueFactor.LocalDateSpinnerValueFactory isn't public.
 * @author Albert Santos
 */
public abstract class LocalDateSpinnerValueFactory extends SpinnerValueFactory<LocalDate> {
    
    /**
     * {@link SpinnerValueFactory} for a {@link LocalDate} that increments/decrements the month.
     * @author Albert Santos
     */
    public static class ByMonth extends LocalDateSpinnerValueFactory {
        public ByMonth() {
            setValue(LocalDate.now());
            setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate object) {
                    return object.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
                }

                @Override
                public LocalDate fromString(String string) {
                    Month month = Month.valueOf(string);
                    return getValue().withMonth(month.getValue());
                }
            });
        }

        @Override
        public void decrement(int steps) {
            setValue(getValue().minusMonths(steps));
        }

        @Override
        public void increment(int steps) {
            setValue(getValue().plusMonths(steps));
        }
    }
    
    
    /**
     * {@link SpinnerValueFactory} for a {@link LocalDate} that increments/decrements the year.
     * @author Albert Santos
     */
    public static class ByYear extends LocalDateSpinnerValueFactory {
        public ByYear() {
            setValue(LocalDate.now());
            setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate object) {
                    return Integer.toString(object.getYear());
                }

                @Override
                public LocalDate fromString(String string) {
                    int year = Integer.parseInt(string);
                    return getValue().withYear(year);
                }
            });
        }

        @Override
        public void decrement(int steps) {
            setValue(getValue().minusYears(steps));
        }

        @Override
        public void increment(int steps) {
            setValue(getValue().plusYears(steps));
        }

    }
}
