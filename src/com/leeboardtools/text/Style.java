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

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Abstract class for styles that can be applied to text.
 * 
 * Not sure about the architecture yet. Some thoughts:
 * Would be nice to apply the styles via CSS style classes if possible.
 * Maybe have pre-defined styles via CSS style classes, defined
 * styles via inline style?
 * 
 * Some styling rules:
 * Text can have one and only one style from each category applied. So one character style
 * and one paragraph style at a time.
 * 
 * How will we embed styling into text? HTML style.
 * span HTML element for character styling.
 * 
 * Saving/restoring:
 * Add styles as CSS to an HTML document.
 * So really, just have an HTML editor + pre-defined styles + custom pre-defined styles.
 * One set of pre-defined styles is the built-in HTML tags.
 * Another set of pre-defined styles is anything we come up with.
 * And another set is application specific.
 * 
 * Then there are custom styles.
 * 
 * @author Albert Santos
 */
public abstract class Style {
    
    /**
     * Defines the name of the property.
     */
    final StringProperty name = new SimpleStringProperty(this, "name");
    public final StringProperty nameProperty() {
        return name;
    }
    public final String getName() {
        return name.get();
    }
    public final void setName(String value) {
        name.set(value);
    }
    
}
