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
    
    
    
    /**
     * Retrieves the first sentence from a string. The first sentence is all the text up to 
     * and including the first period or sequence of periods (as in '...'). If there is no
     * period the entire string is returned. The returned string has had its whitespace trimmed.
     * @param text  The text of interest.
     * @param startOffset   The offset of the start of the text to search.
     * @param endOffset The offset of the end of the text to search, exclusive. If &lt; 0
     * the length of the text is used.
     * @return The first sentenced trimmed of whitespace, or <code>null</code> if text is
     * <code>null</code>
     */
    public static String getFirstSentence(String text, int startOffset, int endOffset) {
        if (text == null) {
            return null;
        }
        
        if (endOffset < 0) {
            endOffset = text.length();
        }
        
        int period = text.indexOf('.', startOffset);
        if ((period >= 0) && (period < endOffset)) {
            int endIndex = period + 1;
            while ((text.charAt(endIndex) == '.') && (endIndex < endOffset)) {
                ++endIndex;
            }
            
            endOffset = endIndex;
        }
        
        return text.substring(startOffset, endOffset).trim();
    }
}
