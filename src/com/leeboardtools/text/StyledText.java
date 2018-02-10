/*
 * Copyright 2018 albert.
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
 * @author albert
 */
public class StyledText {
    
    /**
     * Retrieves the first sentence from a block of styled text.
     * @param text  The text of interest.
     * @return The first sentence in text, <code>null</code> if text is <code>null</code>.
     */
    public static String getFirstTextSentence(String text) {
        if (text == null) {
            return null;
        }
        
        int paragraphStart = text.indexOf("<p>");
        if (paragraphStart < 0) {
            return null;
        }
        paragraphStart += 3;
        
        int paragraphEnd = text.indexOf("</p>", paragraphStart);
        if (paragraphEnd < 0) {
            paragraphEnd = text.length();
        }
        
        return TextUtil.getFirstSentence(text, paragraphStart, paragraphEnd);
    }
}
