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

import com.leeboardtools.control.skin.ItemListCellSkin;
import com.leeboardtools.util.ListConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Cell;
import javafx.scene.control.Skin;

/**
 * A cell that whose item is a list of items.
 * It uses a {@link ListConverter} to generate the list of individual items from
 * the cell's item.
 * @author Albert Santos
 * @param <T>   The type of the item of the object.
 * @param <S>   The type of the elements contained within the cell's list of items.
 * @author Albert Santos
 */
public class ItemListCell <T, S> extends Cell<T> {
    private final ListConverter<T, S> listConverter;
    
    /**
     * The item list displayed by the cell.
     */
    private final ObservableList<S> items = FXCollections.observableArrayList();
    
    public final ObservableList<S> getItems() {
        return items;
    }
    
    
    /**
     * Constructor.
     * @param listConverter The converter to use.
     */
    public ItemListCell(ListConverter<T, S> listConverter) {
        this.listConverter = listConverter;
        this.itemProperty().addListener((property, oldValue, newValue) -> {
            updateItem(newValue, (newValue == null));
        });
    }
    

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        
        if ((item == null) || empty) {
            this.items.clear();
        }
        else {
            ObservableList<S> newItems = this.listConverter.toList(item);
            if ((newItems == null) || newItems.isEmpty()) {
                this.items.clear();
            }
            else if (!this.items.equals(newItems)) {
                this.items.clear();
                this.items.addAll(newItems);
            }
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ItemListCellSkin<>(this);
    }

}
