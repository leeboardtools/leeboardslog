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
package com.leeboardtools.util;

import javafx.collections.ObservableList;

/**
 * A converter that defines conversion behavior between an object of type T and 
 * a {@link ObservableList} of objects of type S.
 * @author Albert Santos
 * @param <T>   The type of the object converted to/from a list.
 * @param <S>   The type of the object in the list.
 */
public interface ListConverter <T, S> {
    /**
     * Converts an object to a list of strings.
     * @param object    The object to convert.
     * @return The list of objects.
     */
    public ObservableList<S> toList(T object);
    
    // TODO: Add fromList conversion.
}
