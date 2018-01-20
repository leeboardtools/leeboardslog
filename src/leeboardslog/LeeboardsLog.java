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
package leeboardslog;

import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import leeboardslog.ui.LogBookEditor;
import leeboardslog.ui.MonthlyController;

/**
 *
 * @author Albert Santos
 */
public class LeeboardsLog extends Application {
    public static final String PREF_LOG_FILE_NAME = "LogFileName";
    
    private static LeeboardsLog me;
    private LogBookEditor logBookEditor;
    
    @Override
    public void start(Stage stage) throws Exception {
        me = this;
        
        Preferences preferences = Preferences.userNodeForPackage(LeeboardsLog.class);
        logBookEditor = new LogBookEditor(preferences);
        
        if (!logBookEditor.restoreLastLogBook(stage)) {
            Platform.exit();
            return;
        }
        
        /*
        TTF Font loading:
    String fName = "/fonts/A.ttf";
    InputStream is = Main.class.getResourceAsStream(fName);
    Font font = Font.createFont(Font.TRUETYPE_FONT, is);
        */
        
        Parent root = FXMLLoader.load(MonthlyController.class.getResource("Monthly.fxml"));
        
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
