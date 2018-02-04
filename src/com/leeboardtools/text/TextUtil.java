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
package com.leeboardtools.text;

/**
 *
 * @author Albert Santos
 */
public class TextUtil {
    
    /**
     * Determines if a String instance is not <code>null</code> and is not empty.
     * @param text  The text of interest.
     * @return <code>true</code> if text is both not <code>null</code> and is not empty.
     */
    public static boolean isAnyText(String text) {
        return (text != null) && !text.isEmpty();
    }
    
    
    /**
     * Retrieves the text up to the first newline character.
     * @param text  The text of interest.
     * @return The portion of text up to the first '\n', or if there is none or text
     * is <code>null</code> text.
     */
    public static String getLine(String text) {
        if (text == null) {
            return null;
        }
        int index = text.indexOf('\n');
        if (index >= 0) {
            return text.substring(0, index);
        }
        return text;
    }
}
