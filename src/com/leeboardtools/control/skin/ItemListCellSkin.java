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

import com.leeboardtools.control.ItemListCell;
import javafx.collections.ListChangeListener;
import javafx.scene.control.SkinBase;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * The skin for {@link ItemListCell}.
 * @author Albert Santos
 * @param <T>   The type of the item of the object.
 * @param <S>   The type of the elements contained within the cell's list of items.
 */
public class ItemListCellSkin <T, S> extends SkinBase<ItemListCell<T, S>> {
    private final Rectangle clipRect = new Rectangle();
    private final TextFlow container = new TextFlow() {
        @Override
        protected double computePrefHeight(double width) {
            double result = super.computePrefHeight(width); //To change body of generated methods, choose Tools | Templates.
            return result;
        }
        
    };
    
    public ItemListCellSkin(ItemListCell<T, S> control) {
        super(control);
        
        // We don't want the item sizes...
        container.setMinSize(0, 0);
        container.setClip(this.clipRect);
        
        control.getItems().addListener((ListChangeListener.Change<? extends S> c) -> {
            updateCellContents();
        });
        container.layoutBoundsProperty().addListener((property, oldValue, newValue) -> {
            clipRect.setWidth(newValue.getWidth());
            clipRect.setHeight(newValue.getHeight());
        });
        
        getChildren().add(container);
        
        updateCellContents();
    }
    
    private void updateCellContents() {
        container.getChildren().clear();
        
        ItemListCell<T, S> cell = getSkinnable();
        cell.getItems().forEach((item) -> {
            String textToShow = item.toString();
            // Treat each item as a paragraph.
            if ((textToShow.length() > 0) && (textToShow.charAt(textToShow.length() - 1) != '\n')) {
                textToShow += '\n';
            }
            Text text = new Text(textToShow);
            container.getChildren().add(text);
        });
    }
}
