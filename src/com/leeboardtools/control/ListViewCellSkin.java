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

import javafx.scene.control.ListView;
import javafx.scene.control.SkinBase;

/**
 * The default skin used by {@link ListViewCell}.
 * @author Albert Santos
 * @param <T>   The type of the item of the object.
 * @param <S>   The type of the elements contained within the cell's ListView.
 */
public class ListViewCellSkin<T, S> extends SkinBase<ListViewCell<T, S>> {
    private final ListView<S> listView;
    
    public ListViewCellSkin(ListViewCell<T, S> control) {
        super(control);
        
        this.listView = new ListView<>(control.getItems());
        getChildren().add(this.listView);
    }
    
}
