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
import javafx.scene.control.SkinBase;

/**
 *
 * @author Albert Santos
 * @param <T>
 * @param <S>
 */
public class ItemListCellSkin <T, S> extends SkinBase<ItemListCell<T, S>> {
    
    public ItemListCellSkin(ItemListCell<T, S> control) {
        super(control);
        
        // Let's make a VBox, and then add text items to it as necessary.
    }
}
