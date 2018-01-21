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

import javafx.scene.control.Cell;
import javafx.scene.control.Skin;

/**
 * This is a cell that has a {@link javafx.scene.control.ListView} inside the cell.
 * @author Albert Santos
 * @param <T>   The type of the elements contained within the cell's ListView.
 */
public class ListViewCell<T> extends Cell<T> {

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ListViewCellSkin<>(this);
    }
    
}
