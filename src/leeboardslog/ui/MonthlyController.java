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
package leeboardslog.ui;

import com.leeboardtools.control.LocalDateSpinnerValueFactory;
import com.leeboardtools.control.MonthlyViewControl;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import leeboardslog.LeeboardsLog;


/**
 * The controller for the monthly view...
 * @author Albert Santos
 */
public class MonthlyController implements Initializable {
    
    @FXML
    private Spinner<LocalDate> month;
    @FXML
    private Spinner<LocalDate> year;
    @FXML
    private VBox editPane;
    @FXML
    private TextField searchText;
    @FXML
    private BorderPane mainPane;
    
    private MonthlyViewControl monthlyViewControl;
    private LogBookEditor logBookEditor;
    
    private final ObjectProperty<LocalDate> activeDate = new SimpleObjectProperty<>(this, "activeDate", LocalDate.now());
    
    public final LocalDate getActiveDate() {
        return activeDate.get();
    }
    public final void setActiveDate(LocalDate date) {
        activeDate.set(date);
    }
    public final ObjectProperty<LocalDate> activeDateProperty() {
        return activeDate;
    }
    
    
    @FXML
    private ListView<String> testList;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        LocalDateSpinnerValueFactory.ByMonth monthValueFactory = new LocalDateSpinnerValueFactory.ByMonth();
        this.month.setValueFactory(monthValueFactory);
        monthValueFactory.valueProperty().addListener((observable, oldValue, newValue)-> {
            this.activeDate.set(newValue);
        });
        this.activeDate.addListener((observable, oldValue, newValue)-> {
            monthValueFactory.setValue(newValue);
        });
        
        LocalDateSpinnerValueFactory.ByYear yearValueFactory = new LocalDateSpinnerValueFactory.ByYear();
        this.year.setValueFactory(yearValueFactory);
        yearValueFactory.valueProperty().addListener((observable, oldValue, newValue)-> {
            this.activeDate.set(newValue);
        });
        this.activeDate.addListener((observable, oldValue, newValue)-> {
            yearValueFactory.setValue(newValue);
        });
        
        this.monthlyViewControl = new MonthlyViewControl();
        this.mainPane.setCenter(this.monthlyViewControl);

        // Can't seem to get a lambda expression to work so using an anonymous class...
        this.monthlyViewControl.activeDateProperty().addListener(new ChangeListener<LocalDate>() {
            @Override
            public void changed(ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) {
                activeDate.set(newValue);
            }
        });
        //this.monthlyViewControl.activeDateProperty().addListener((observable, oldValue, newValue)-> {
        //    this.activeDate.set(newValue);
        //});
        this.activeDate.addListener((observable, oldValue, newValue)-> {
            this.monthlyViewControl.setActiveDate(newValue);
        });
        
        
        this.logBookEditor = LeeboardsLog.getLogBookEditor();
        if (this.logBookEditor != null) {
            
        }
        
        // TEST!!!
        ObservableList<String> testStrings = FXCollections.observableArrayList("A", "B", "C");
        testList.setItems(testStrings);
    }
    
    
}
