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

import javafx.scene.Node;
import javafx.scene.Parent;

/**
 * Miscellaneous JavaFX utility methods.
 * @author Albert Santos
 */
public class FxUtil {
    
    /**
     * Searches the descendants of a parent node for a child node with a given id,
     * the search is performed breadth first.
     * @param parent    The parent node to search.
     * @param id    The id to look for, must not be <code>null</code>
     * @return The descendant node with the id, <code>null</code> if none found.
     */
    public static Node getChildWithId(Parent parent, String id) {
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (id.equals(child.getId())) {
                return child;
            }
        }
        
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof Parent) {
                Node nodeWithId = getChildWithId((Parent)child, id);
                if (nodeWithId != null) {
                    return nodeWithId;
                }
            }
        }
        
        return null;
    }
}
