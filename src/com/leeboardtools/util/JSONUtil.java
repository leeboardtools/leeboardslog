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

import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Various utility functions for working with JSON objects.
 * @author Albert Santos
 */
public class JSONUtil {

    /**
     * Transfers the members from a JSON array to a String set.
     * @param jsonArray The JSON array.
     * @param set   The set to copy the contents to, if <code>null</code> then a {@link HashSet}
     * will be created.
     * @return The set.
     * @throws JSONException    if a value in the array is not a string.
     */
    public static Set<String> arrayToSet(JSONArray jsonArray, Set<String> set) throws JSONException {
        if (set == null) {
            set = new HashSet<>();
        }

        int length = jsonArray.length();
        for (int i = 0; i < length; ++i) {
            set.add(jsonArray.getString(i));
        }
        
        return set;
    }
}
