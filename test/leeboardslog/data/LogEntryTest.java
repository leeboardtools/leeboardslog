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
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Albert Santos
 */
public class LogEntryTest {
    
    public LogEntryTest() {
    }

    /**
     * Test of getGuid method, of class LogEntry.
     */
    @Test
    public void testGetGuid() {
        System.out.println("getGuid");
        LogEntry instanceA = new LogEntry();
        LogEntry instanceB = new LogEntry();
        assertNotEquals(instanceA.getGuid(), instanceB.getGuid());
    }

    /**
     * Test of fromObservable method, of class LogEntry.
     */
/*    @Test
    public void testFromObservable() {
        System.out.println("fromObservable");
        Observable observable = null;
        LogEntry expResult = null;
        LogEntry result = LogEntry.fromObservable(observable);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of getTimePeriod method, of class LogEntry.
     */
/*  @Test
    public void testGetTimePeriod() {
        System.out.println("getTimePeriod");
        LogEntry instance = new LogEntry();
        TimePeriod expResult = null;
        TimePeriod result = instance.getTimePeriod();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of setTimePeriod method, of class LogEntry.
     */
    @Test
    public void testSetTimePeriod() {
        System.out.println("setTimePeriod");
        TimePeriod timePeriodA = TimePeriod.fromEdgeAndDuration(LocalDateTime.of(2017, 02, 13, 12, 14), Duration.ofHours(1), null);
        LogEntry logEntry = new LogEntry(null, timePeriodA, null);
        assertEquals(logEntry.getTimePeriod(), timePeriodA);

        TimePeriod timePeriodB = TimePeriod.fromEdgeAndDuration(LocalDateTime.of(2017, 02, 13, 12, 14), Duration.ofHours(2), null);
        logEntry.setTimePeriod(timePeriodB);
        assertEquals(logEntry.getTimePeriod(), timePeriodB);
        assertNotEquals(logEntry.getTimePeriod(), timePeriodA);
    }

    /**
     * Test of timePeriodProperty method, of class LogEntry.
     */
/*    @Test
    public void testTimePeriodProperty() {
        System.out.println("timePeriodProperty");
        LogEntry instance = new LogEntry();
        ObjectProperty<TimePeriod> expResult = null;
        ObjectProperty<TimePeriod> result = instance.timePeriodProperty();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of getZoneId method, of class LogEntry.
     */
/*    @Test
    public void testGetZoneId() {
        System.out.println("getZoneId");
        LogEntry instance = new LogEntry();
        ZoneId expResult = null;
        ZoneId result = instance.getZoneId();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of setZoneId method, of class LogEntry.
     */
    @Test
    public void testSetZoneId() {
        System.out.println("setZoneId");
        LogEntry logEntry = new LogEntry();
        assertEquals(null, logEntry.getZoneId());
        
        ZoneId zoneId = ZoneId.of("Europe/Paris");
        logEntry.setZoneId(zoneId);
        assertEquals(logEntry.getZoneId(), zoneId);
    }

    /**
     * Test of zoneId method, of class LogEntry.
     */
/*    @Test
    public void testZoneId() {
        System.out.println("zoneId");
        LogEntry instance = new LogEntry();
        ObjectProperty<ZoneId> expResult = null;
        ObjectProperty<ZoneId> result = instance.zoneId();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/

    /**
     * Test of setStartEndTime method, of class LogEntry.
     */
    @Test
    public void testSetStartEndTime_3args() {
        System.out.println("setStartEndTime");
        LocalDateTime dateTimeA = LocalDateTime.of(2017, 12, 03, 12, 45);
        LocalDateTime dateTimeB = LocalDateTime.of(2017, 12, 03, 12, 46);
        ZoneId zoneId = ZoneId.of("Europe/Paris");
        
        TimePeriod timePeriod = TimePeriod.fromEdgeTimes(dateTimeA, dateTimeB, zoneId);
        
        LogEntry logEntry = new LogEntry();
        logEntry.setStartEndTime(dateTimeA, dateTimeB, zoneId);
        assertEquals(timePeriod, logEntry.getTimePeriod());

        logEntry.setStartEndTime(dateTimeB, dateTimeA, zoneId);
        assertEquals(timePeriod, logEntry.getTimePeriod());
    }

    /**
     * Test of setStartEndTime method, of class LogEntry.
     */
    @Test
    public void testSetStartEndTime_LocalDateTime_LocalDateTime() {
        System.out.println("setStartEndTime");
        LocalDateTime dateTimeA = LocalDateTime.of(2017, 12, 03, 12, 45);
        LocalDateTime dateTimeB = LocalDateTime.of(2017, 12, 03, 12, 46);
        ZoneId zoneId = ZoneId.systemDefault();
        
        TimePeriod timePeriod = TimePeriod.fromEdgeTimes(dateTimeA, dateTimeB, zoneId);
        
        LogEntry logEntry = new LogEntry();
        logEntry.setStartEndTime(dateTimeA, dateTimeB);
        assertEquals(timePeriod, logEntry.getTimePeriod());

        logEntry.setStartEndTime(dateTimeB, dateTimeA);
        assertEquals(timePeriod, logEntry.getTimePeriod());
    }

    /**
     * Test of setDateTime method, of class LogEntry.
     */
    @Test
    public void testSetDateTime_LocalDateTime_ZoneId() {
        System.out.println("setDateTime");
        
        LocalDateTime dateTime = LocalDateTime.of(2017, 1, 15, 1, 23);
        ZoneId zoneId = ZoneId.of("Europe/Paris");
        
        TimePeriod timePeriod = TimePeriod.fromEdgeAndDuration(dateTime, Duration.ofHours(0), zoneId);
        
        LogEntry logEntry = new LogEntry();
        logEntry.setDateTime(dateTime, zoneId);
        assertEquals(timePeriod, logEntry.getTimePeriod());
    }

    /**
     * Test of setDateTime method, of class LogEntry.
     */
    @Test
    public void testSetDateTime_LocalDateTime() {
        System.out.println("setDateTime");
        
        LocalDateTime dateTime = LocalDateTime.of(2017, 1, 15, 1, 23);
        ZoneId zoneId = ZoneId.systemDefault();
        
        TimePeriod timePeriod = TimePeriod.fromEdgeAndDuration(dateTime, Duration.ofHours(0), zoneId);
        
        LogEntry logEntry = new LogEntry();
        logEntry.setDateTime(dateTime);
        assertEquals(timePeriod, logEntry.getTimePeriod());
    }

    /**
     * Test of setDate method, of class LogEntry.
     */
    @Test
    public void testSetDate_LocalDate_ZoneId() {
        System.out.println("setDate");
        LocalDate date = LocalDate.of(2017, 02, 03);
        ZoneId zoneId = ZoneId.of("Europe/Paris");
        TimePeriod timePeriod = TimePeriod.fromEdgeDates(date, date, zoneId);
        
        LogEntry logEntry = new LogEntry();
        logEntry.setDate(date, zoneId);
        assertEquals(timePeriod, logEntry.getTimePeriod());
    }

    /**
     * Test of setDate method, of class LogEntry.
     */
    @Test
    public void testSetDate_LocalDate() {
        System.out.println("setDate");
        LocalDate date = LocalDate.of(2017, 02, 03);
        ZoneId zoneId = ZoneId.systemDefault();
        TimePeriod timePeriod = TimePeriod.fromEdgeDates(date, date, zoneId);
        
        LogEntry logEntry = new LogEntry();
        logEntry.setDate(date);
        assertEquals(timePeriod, logEntry.getTimePeriod());
    }

    /**
     * Test of getTitle method, of class LogEntry.
     */
/*    @Test
    public void testGetTitle() {
        System.out.println("getTitle");
        LogEntry instance = new LogEntry();
        String expResult = "";
        String result = instance.getTitle();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of setTitle method, of class LogEntry.
     */
    @Test
    public void testSetTitle() {
        System.out.println("setTitle");
        LogEntry instance = new LogEntry();
        assertEquals("", instance.getTitle());
        
        String title = "A title";
        instance.setTitle(title);
        assertEquals(title, instance.getTitle());
    }

    /**
     * Test of titleProperty method, of class LogEntry.
     */
/*    @Test
    public void testTitleProperty() {
        System.out.println("titleProperty");
        LogEntry instance = new LogEntry();
        StringProperty expResult = null;
        StringProperty result = instance.titleProperty();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    
    public static class TestListener implements LogEntry.Listener {
        int changeCount = 0;
        
        @Override
        public void logEntryChanged(LogEntry logEntry) {
            ++changeCount;
        }
        
    }
    /**
     * Test of tagsProperty method, of class LogEntry.
     */
    @Test
    public void testTagsProperty() {
        System.out.println("tagsProperty");
        
        TestListener listener = new TestListener();
        LogEntry logEntry = new LogEntry();
        logEntry.addListener(listener);
        
        int lastChangeCount;
        lastChangeCount = listener.changeCount;
        logEntry.getTags().add("A");
        assertEquals(lastChangeCount + 1, listener.changeCount);
        assertTrue(logEntry.getTags().contains("A"));
        
        lastChangeCount = listener.changeCount;
        logEntry.tagsProperty().get().add("B");
        assertEquals(lastChangeCount + 1, listener.changeCount);
        assertTrue(logEntry.getTags().contains("B"));
        
        lastChangeCount = listener.changeCount;
        logEntry.getTags().remove("C");
        assertEquals(lastChangeCount, listener.changeCount);
        
        lastChangeCount = listener.changeCount;
        logEntry.getTags().remove("B");
        assertEquals(lastChangeCount + 1, listener.changeCount);
        assertFalse(logEntry.getTags().contains("B"));
    }

    
    /**
     * Test of addListener method, of class LogEntry.
     */
/*    @Test
    public void testAddListener() {
        System.out.println("addListener");
        LogEntry.Listener listener = null;
        LogEntry instance = new LogEntry();
        instance.addListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of removeListener method, of class LogEntry.
     */
/*    @Test
    public void testRemoveListener() {
        System.out.println("removeListener");
        LogEntry.Listener listener = null;
        LogEntry instance = new LogEntry();
        instance.removeListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of toJSON, fromJSON, toJSONObject methods, of class LogEntry.
     */
    @Test
    public void testJSON() {
        System.out.println("json");
        ZoneId zoneId = null;
        LogEntry refLogEntry = new LogEntry();
        refLogEntry.setTimePeriod(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 10, 12, 13, 45), LocalDateTime.of(2017, 10, 12, 16, 45), zoneId));
        refLogEntry.setTitle("The title");

        refLogEntry.getTags().add("Tag A");
        refLogEntry.getTags().add("Tag B");
        
        refLogEntry.setBody(refLogEntry.getBodyFormat(), 
                "<html>"
                + "<p>Paragraph 1</p>"
                + "<p>Paragraph 2</p>");
        
        JSONObject jsonObjectDst = refLogEntry.toJSONObject();
        String jsonText = jsonObjectDst.toString();
        JSONObject jsonObject = new JSONObject(jsonText);
        LogEntry testLogEntry = LogEntry.fromJSON(jsonObject);
        assertEquals(refLogEntry, testLogEntry);
        
        refLogEntry.setBody(refLogEntry.getBodyFormat(), "");
        assertNotEquals(refLogEntry, testLogEntry);

        jsonObject = refLogEntry.toJSONObject();
        testLogEntry = LogEntry.fromJSON(jsonObject);
        assertEquals(refLogEntry, testLogEntry);
        
        // Make sure a completely blank log entry works...
        refLogEntry = new LogEntry();
        jsonObjectDst = refLogEntry.toJSONObject();
        jsonText = jsonObjectDst.toString();
        jsonObject = new JSONObject(jsonText);
        testLogEntry = LogEntry.fromJSON(jsonObject);
        assertEquals(refLogEntry, testLogEntry);
    }
    
}
