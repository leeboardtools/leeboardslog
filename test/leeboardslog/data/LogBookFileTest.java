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
package leeboardslog.data;

import com.leeboardtools.util.TimePeriod;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Albert Santos
 */
public class LogBookFileTest {
    
    public LogBookFileTest() {
    }
    
    File createTempFile() {
        try {
            File file = File.createTempFile("LogBookFile_Test", ".leeboardslog");
            file.deleteOnExit();
            return file;
        } catch (IOException ex) {
            System.out.println("createTempFile() failed. " + ex);
            return null;
        }
    }

    @Test
    public void testIsPossibleFileLogBook() {
        System.out.println("isPossibleFileLogBook");
        File testFile = createTempFile();
        assertFalse(LogBookFile.isPossibleFileLogBook(testFile));
        
        FileWriter writer = null;
        try {
            writer = new FileWriter(testFile);
            writer.write(LogBookFile.FILE_TAG + "\n"
                    + "0.0\n"
                            + "{}");
        } catch (IOException ex) {
            Logger.getLogger(LogBookFileTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    Logger.getLogger(LogBookFileTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                writer = null;
            }
        }
        assertFalse(LogBookFile.isPossibleFileLogBook(testFile));
        
        
        try {
            writer = new FileWriter(testFile);
            writer.write(LogBookFile.FILE_TAG + "\n"
                    + LogBookFile.VERSION_VALUE + "\n"
                    + 123 + "\n"
                    + "Author" + "\n"
                    + "2007-12-03T10:15:30.00Z" + "\n"
                    + "{\n"
                    + "}");
        } catch (IOException ex) {
            Logger.getLogger(LogBookFileTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    Logger.getLogger(LogBookFileTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                writer = null;
            }
        }
        assertTrue(LogBookFile.isPossibleFileLogBook(testFile));
    }

/*    @Test
    public void testIsVersionSupported() {
        System.out.println("isVersionSupported");
        String version = "";
        boolean expResult = false;
        boolean result = LogBookFile.isVersionSupported(version);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    
/*    @Test
    public void testCreateLogBookFile() throws Exception {
        System.out.println("createLogBookFile");
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/

/*    @Test
    public void testOpenLogBookFile() throws Exception {
        System.out.println("openLogBookFile");
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
/*    @Test
    public void testGetFileName() {
        System.out.println("getFileName");
        LogBookFile instance = null;
        String expResult = "";
        String result = instance.getFileName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
/*    @Test
    public void testIsLogBookChanged() {
        System.out.println("isLogBookChanged");
        LogBookFile instance = null;
        boolean expResult = false;
        boolean result = instance.isLogBookChanged();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    @Test
    public void testUpdateFile() throws Exception {
        System.out.println("updateFile");
        
        ZoneId zoneId = ZoneId.of("Europe/Paris");
        
        File file = createTempFile();
        LogBookFile refLogBookFile = LogBookFile.createLogBookFile(file, "Anonymous");
        LogBook logBook = refLogBookFile.getLogBook();

        assertEquals("Anonymous", logBook.getActiveAuthor());
        
        refLogBookFile.setOptIsCompressFile(Optional.of(false));
        
        LogEntry entryA = new LogEntry(null, TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 12, 21, 12, 30), LocalDateTime.of(2018, 1, 2, 3, 4), zoneId),
                zoneId);
        logBook.addLogEntry(entryA);
        entryA.setBody(entryA.getBodyFormat(), "<p>Hello</p>");

        LogEntry entryB = new LogEntry(null, TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 10, 21, 12, 30), LocalDateTime.of(2017, 11, 2, 3, 4), zoneId),
                zoneId);
        logBook.addLogEntry(entryB);
        entryB.setTitle("Entry B");
        
        refLogBookFile.updateFile();
        assertFalse(refLogBookFile.isLogBookChanged());

        {        
            LogBookFile testLogBookFile = LogBookFile.openLogBookFile(file, "Anonymous");
            assertEquals(logBook, testLogBookFile.getLogBook());
        }
        
        entryB.setBody(entryA.getBodyFormat(), "<p>Good-bye</p>");
        assertTrue(refLogBookFile.isLogBookChanged());
        
        refLogBookFile.updateFile();
        assertFalse(refLogBookFile.isLogBookChanged());

        {        
            LogBookFile testLogBookFile = LogBookFile.openLogBookFile(file, "Anonymous");
            assertEquals(logBook, testLogBookFile.getLogBook());
        }
    }
    
}
