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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import javafx.collections.ObservableMap;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Albert Santos
 */
public class LogBookTest {
    
    public LogBookTest() {
    }
    
    

    /**
     * Test of closeLogBook method, of class LogBook.
     */
    @Test
    public void testCloseLogBook() {
        System.out.println("closeLogBook");
        LogBook logBook = newTestLogBook(null);
        assertTrue(logBook.getLogEntryCount() > 0);
        
        logBook.closeLogBook();
        assertEquals(0, logBook.getLogEntryCount());
        
        assertEquals(0, logBook.getLogEntries(null).size());
        assertEquals(0, logBook.getLogEntriesByStart().size());
        assertEquals(0, logBook.getLogEntriesByEnd().size());
    }
    
    
    class TestListener implements LogBook.Listener {
        int lastAddedCount = 0;
        int addedCount = 0;
        
        int lastRemovedCount = 0;
        int removedCount = 0;
        
        void assertAddedCount(int newDelta) {
            int newCount = this.lastAddedCount + newDelta;
            assertEquals(newCount, this.addedCount);
            this.lastAddedCount = newCount;
        }
        
        void assertRemovedCount(int newDelta) {
            int newCount = this.lastRemovedCount + newDelta;
            assertEquals(newCount, this.removedCount);
            this.lastRemovedCount = newCount;
        }
        
        @Override
        public void entriesAdded(LogBook logBook, Collection<LogEntry> logEntries) {
            addedCount += logEntries.size();
        }

        @Override
        public void entriesRemoved(LogBook logBook, Collection<LogEntry> logEntries) {
            removedCount += logEntries.size();
        }
        
    }
    
    static LogEntry newLogEntry(int year, int month, int day, int hour, int min, long hours, ZoneId zoneId, String title) {
        LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, min);
        LogEntry logEntry = new LogEntry(null, TimePeriod.fromEdgeAndDuration(dateTime, Duration.ofHours(hours), zoneId), zoneId);
        logEntry.setTitle(title);
        return logEntry;
    }
    
    static LogEntry newLogEntry(TimePeriod timePeriod, ZoneId zoneId, String title) {
        LogEntry logEntry = new LogEntry(null, timePeriod, zoneId);
        logEntry.setTitle(title);
        return logEntry;
    }
    
    static LogEntry [] newTestLogEntries(ZoneId zoneId) {
        return new LogEntry[] {
            newLogEntry(2017, 1, 2, 3, 4, 1, zoneId, "Entry 0"),
            newLogEntry(2017, 1, 3, 14, 15, 16, zoneId, "Entry 1"),
            newLogEntry(2017, 1, 3, 14, 15, 17, zoneId, "Entry 2"),
            newLogEntry(2017, 1, 3, 15, 0, 0, zoneId, "Entry 3"),
            newLogEntry(2017, 1, 4, 3, 0, 1, zoneId, "Entry 4"),
            newLogEntry(TimePeriod.fromEdgeDates(LocalDate.of(2017, 1, 15), LocalDate.of(2017, 1, 20)), zoneId, "Entry 5"),
            newLogEntry(TimePeriod.fromEdgeDates(LocalDate.of(2017, 1, 15), LocalDate.of(2017, 1, 17)), zoneId, "Entry 6"),
            newLogEntry(TimePeriod.fromEdgeDates(LocalDate.of(2017, 1, 17), LocalDate.of(2017, 1, 20)), zoneId, "Entry 7"),
            newLogEntry(TimePeriod.fromEdgeDates(LocalDate.of(2017, 1, 21), LocalDate.of(2017, 1, 21)), zoneId, "Entry 8"),
            newLogEntry(TimePeriod.fromEdgeDates(LocalDate.of(2017, 1, 21), LocalDate.of(2017, 1, 21)), zoneId, "Entry 9"),
        };
    }
    
    static LogBook newTestLogBook(ZoneId zoneId) {
        LogEntry [] logEntries = newTestLogEntries(zoneId);
        LogBook logBook = new LogBook();
        logBook.addLogEntries(logEntries);
        return logBook;
    }
    
    /**
     * Test of getName method, of class LogBook.
     */
/*    @Test
    public void testGetName() {
        System.out.println("getName");
        LogBook instance = null;
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of setName method, of class LogBook.
     */
    @Test
    public void testSetName() {
        System.out.println("setName");
        LogBook instance = new LogBook();
        assertEquals(null, instance.getName());
        assertEquals(null, instance.nameProperty().get());
        
        String name = "Abc";
        instance.setName(name);
        assertEquals(name, instance.getName());
        assertEquals(name, instance.nameProperty().get());
    }

    /**
     * Test of nameProperty method, of class LogBook.
     */
/*    @Test
    public void testNameProperty() {
        System.out.println("nameProperty");
        LogBook instance = null;
        StringProperty expResult = null;
        StringProperty result = instance.nameProperty();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    
    /**
     * Test of getCurrentZoneId method, of class LogBook.
     */
/*    @Test
    public void testGetCurrentZoneId() {
        System.out.println("getCurrentZoneId");
        LogBook instance = null;
        ZoneId expResult = null;
        ZoneId result = instance.getCurrentZoneId();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of setCurrentZoneId method, of class LogBook.
     */
    @Test
    public void testSetCurrentZoneId() {
        System.out.println("setCurrentZoneId");
        LogBook logBook = new LogBook();
        assertEquals(null, logBook.getCurrentZoneId());
        assertEquals(null, logBook.currentZoneIdProperty().get());
        
        ZoneId zoneId = ZoneId.of("Europe/Paris");
        logBook.setCurrentZoneId(zoneId);
        assertEquals(zoneId, logBook.getCurrentZoneId());
    }

    /**
     * Test of currentZoneIdProperty method, of class LogBook.
     */
/*    @Test
    public void testCurrentZoneIdProperty() {
        System.out.println("currentZoneIdProperty");
        LogBook instance = null;
        ObjectProperty expResult = null;
        ObjectProperty result = instance.currentZoneIdProperty();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of getLogEntryCount method, of class LogBook.
     */
    @Test
    public void testGetLogEntryCount() {
        System.out.println("getLogEntryCount");
        LogEntry [] logEntries = newTestLogEntries(null);
        LogBook logBook = newTestLogBook(null);
        assertEquals(logEntries.length, logBook.getLogEntryCount());
    }
    
    void assertLogEntriesCollections(TreeSet<LogEntry> sortedRefCollection, Collection<LogEntry> testCollection) {
        assertEquals(sortedRefCollection.size(), testCollection.size());
        
        List<LogEntry> testEntries = new ArrayList<>(testCollection);
        int index = 0;
        Iterator<LogEntry> iterator = sortedRefCollection.iterator();
        while (iterator.hasNext()) {
            assertEquals(iterator.next().getGuid(), testEntries.get(index).getGuid());
            ++index;
        }
    }
    
    void assertLogEntriesCollections(Collection<LogEntry> refCollection, Collection<LogEntry> testCollection) {
        assertEquals(refCollection.size(), testCollection.size());
        
        Set<LogEntry> entriesToFind = new TreeSet<>(new LogEntry.GuidComparator());
        entriesToFind.addAll(refCollection);
        
        List<LogEntry> testEntries = new ArrayList<>(testCollection);
        for (int i = 0; i < testEntries.size(); ++i) {
            assertTrue(entriesToFind.remove(testEntries.get(i)));
        }
        
        assertTrue(entriesToFind.isEmpty());
    }
    
    void assertLogEntriesByDate(TreeMap<LocalDate, Collection<LogEntry>> refDateEntries, LogBook logBook) {
        ObservableMap<LocalDate, DayLogEntries> entriesByDate = logBook.getLogEntriesByDate();
        assertEquals(refDateEntries.size(), entriesByDate.size());
        
        refDateEntries.forEach((key, refEntries) -> {
            DayLogEntries testEntries = entriesByDate.get(key);
            assertNotEquals(null, testEntries);
            
            List<LogEntry> testLogEntries = testEntries.getLogEntries();
            assertEquals(refEntries.size(), testLogEntries.size());
            
            refEntries.forEach((logEntry) -> {
                assertTrue(testLogEntries.contains(logEntry));
            });
        });
    }
    
    void addLogEntryDate(TreeMap<LocalDate, Collection<LogEntry>> dateEntries, LocalDate date, LogEntry logEntry) {
        Collection<LogEntry> entries = dateEntries.get(date);
        if (entries == null) {
            entries = new HashSet<>();
            dateEntries.put(date, entries);
        }
        entries.add(logEntry);
    }
    

    /**
     * Test of getLogEntriesByStart method, of class LogBook.
     */
    @Test
    public void testGetLogEntriesByStart() {
        System.out.println("getLogEntriesByStart");
        LogEntry [] logEntries = newTestLogEntries(null);
        LogBook logBook = new LogBook();
        logBook.addLogEntries(logEntries);
        
        TreeSet<LogEntry> refSortedLogEntries = new TreeSet<>(new LogEntry.StartComparator());
        for (int i = 0; i < logEntries.length; ++i) {
            refSortedLogEntries.add(logEntries[i]);
        }
        
        List<LogEntry> testLogEntries = logBook.getLogEntriesByStart();
        assertLogEntriesCollections(refSortedLogEntries, testLogEntries);
        
        
        // Check the sort order is still valid after time period changes.
        ZoneId zoneId = null;
        logBook = new LogBook();
        LogEntry logEntryA = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 5, 12, 0), LocalDateTime.of(2017, 1, 5, 12, 30), zoneId), zoneId, "Entry A");
        logBook.addLogEntry(logEntryA);
        
        LogEntry logEntryB = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 6, 12, 0), LocalDateTime.of(2017, 1, 6, 12, 30), zoneId), zoneId, "Entry B");
        logBook.addLogEntry(logEntryB);
        
        LogEntry logEntryC = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 7, 12, 0), LocalDateTime.of(2017, 1, 7, 12, 30), zoneId), zoneId, "Entry C");
        logBook.addLogEntry(logEntryC);
        
        LogEntry logEntryD = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 6, 12, 0), LocalDateTime.of(2017, 1, 7, 12, 30), zoneId), zoneId, "Entry D");
        logBook.addLogEntry(logEntryD);
        
        refSortedLogEntries.clear();
        refSortedLogEntries.add(logEntryA);
        refSortedLogEntries.add(logEntryB);
        refSortedLogEntries.add(logEntryC);
        refSortedLogEntries.add(logEntryD);
        
        testLogEntries = logBook.getLogEntriesByStart();
        assertLogEntriesCollections(refSortedLogEntries, testLogEntries);
        
        
        logEntryB.setTimePeriod(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 7, 12, 0), LocalDateTime.of(2017, 1, 7, 12, 30), zoneId));
        refSortedLogEntries.clear();
        refSortedLogEntries.add(logEntryA);
        refSortedLogEntries.add(logEntryB);
        refSortedLogEntries.add(logEntryC);
        refSortedLogEntries.add(logEntryD);
        
        testLogEntries = logBook.getLogEntriesByStart();
        assertLogEntriesCollections(refSortedLogEntries, testLogEntries);
        
        
        logEntryC.setTimePeriod(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 5, 12, 0), LocalDateTime.of(2017, 1, 5, 12, 30), zoneId));
        refSortedLogEntries.clear();
        refSortedLogEntries.add(logEntryA);
        refSortedLogEntries.add(logEntryB);
        refSortedLogEntries.add(logEntryC);
        refSortedLogEntries.add(logEntryD);
        
        testLogEntries = logBook.getLogEntriesByStart();
        assertLogEntriesCollections(refSortedLogEntries, testLogEntries);
    }

    /**
     * Test of getLogEntriesByEnd method, of class LogBook.
     */
    @Test
    public void testGetLogEntriesByEnd() {
        System.out.println("getLogEntriesByEnd");
        LogEntry [] logEntries = newTestLogEntries(null);
        LogBook logBook = new LogBook();
        logBook.addLogEntries(logEntries);
        
        TreeSet<LogEntry> refSortedLogEntries = new TreeSet<>(new LogEntry.EndComparator());
        for (int i = 0; i < logEntries.length; ++i) {
            refSortedLogEntries.add(logEntries[i]);
        }
        
        List<LogEntry> testLogEntries = logBook.getLogEntriesByEnd();
        assertLogEntriesCollections(refSortedLogEntries, testLogEntries);
        
        
        // Check the sort order is still valid after time period changes.
        ZoneId zoneId = null;
        logBook = new LogBook();
        LogEntry logEntryA = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 4, 12, 0), LocalDateTime.of(2017, 1, 5, 12, 30), zoneId), zoneId, "Entry A");
        logBook.addLogEntry(logEntryA);
        
        LogEntry logEntryB = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 6, 12, 0), LocalDateTime.of(2017, 1, 6, 12, 30), zoneId), zoneId, "Entry B");
        logBook.addLogEntry(logEntryB);
        
        LogEntry logEntryC = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 7, 12, 0), LocalDateTime.of(2017, 1, 7, 12, 30), zoneId), zoneId, "Entry C");
        logBook.addLogEntry(logEntryC);
        
        LogEntry logEntryD = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 6, 12, 0), LocalDateTime.of(2017, 1, 7, 12, 30), zoneId), zoneId, "Entry D");
        logBook.addLogEntry(logEntryD);
        
        refSortedLogEntries.clear();
        refSortedLogEntries.add(logEntryA);
        refSortedLogEntries.add(logEntryB);
        refSortedLogEntries.add(logEntryC);
        refSortedLogEntries.add(logEntryD);
        
        testLogEntries = logBook.getLogEntriesByEnd();
        assertLogEntriesCollections(refSortedLogEntries, testLogEntries);
        
        
        logEntryB.setTimePeriod(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 6, 12, 15), LocalDateTime.of(2017, 1, 7, 12, 35), zoneId));
        refSortedLogEntries.clear();
        refSortedLogEntries.add(logEntryA);
        refSortedLogEntries.add(logEntryB);
        refSortedLogEntries.add(logEntryC);
        refSortedLogEntries.add(logEntryD);
        
        testLogEntries = logBook.getLogEntriesByEnd();
        assertLogEntriesCollections(refSortedLogEntries, testLogEntries);
        
        
        logEntryD.setTimePeriod(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 5, 12, 15), LocalDateTime.of(2017, 1, 5, 12, 30), zoneId));
        refSortedLogEntries.clear();
        refSortedLogEntries.add(logEntryA);
        refSortedLogEntries.add(logEntryB);
        refSortedLogEntries.add(logEntryC);
        refSortedLogEntries.add(logEntryD);
        
        testLogEntries = logBook.getLogEntriesByEnd();
        assertLogEntriesCollections(refSortedLogEntries, testLogEntries);
    }

    /**
     * Test of getLogEntries method, of class LogBook.
     */
    @Test
    public void testGetLogEntries() {
        System.out.println("getLogEntries");
        LogEntry [] logEntries = newTestLogEntries(null);
        LogBook logBook = new LogBook();
        logBook.addLogEntries(logEntries);
        
        List<LogEntry> refLogEntries = Arrays.asList(logEntries);
        Collection<LogEntry> testLogEntries = logBook.getLogEntries(null);
        assertLogEntriesCollections(refLogEntries, testLogEntries);
    }

    /**
     * Test of getLogEntriesInTimePeriod method, of class LogBook.
     */
    @Test
    public void testGetLogEntriesInTimePeriod() {
        System.out.println("getLogEntriesInTimePeriod");
        ZoneId zoneId = null;
        
        LogBook logBook = new LogBook();
        
        LogEntry logEntryA = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 5, 12, 0), LocalDateTime.of(2017, 1, 5, 12, 30), zoneId), zoneId, "Entry A");
        logBook.addLogEntry(logEntryA);
        
        LogEntry logEntryB = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 5, 12, 0), LocalDateTime.of(2017, 2, 7, 12, 30), zoneId), zoneId, "Entry B");
        logBook.addLogEntry(logEntryB);
        
        LogEntry logEntryC = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 2, 1, 12, 0), LocalDateTime.of(2017, 2, 4, 12, 30), zoneId), zoneId, "Entry C");
        logBook.addLogEntry(logEntryC);
        
        LogEntry logEntryD = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 2, 3, 12, 0), LocalDateTime.of(2017, 2, 4, 12, 30), zoneId), zoneId, "Entry D");
        logBook.addLogEntry(logEntryD);
        
        TimePeriod timePeriod;
        TreeSet<LogEntry> refLogEntriesStart = new TreeSet<>(new LogEntry.StartComparator());
        Collection<LogEntry> result;
        
        // Before first...
        refLogEntriesStart.clear();
        timePeriod = TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 5, 11, 0), LocalDateTime.of(2017, 1, 5, 12, 0), zoneId);
        result = logBook.getLogEntriesInTimePeriod(timePeriod, null);
        assertLogEntriesCollections(refLogEntriesStart, result);

        // Part of first.
        refLogEntriesStart.clear();
        refLogEntriesStart.add(logEntryA);
        refLogEntriesStart.add(logEntryB);
        timePeriod = TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 5, 11, 0), LocalDateTime.of(2017, 1, 5, 12, 10), zoneId);
        result = logBook.getLogEntriesInTimePeriod(timePeriod, null);
        assertLogEntriesCollections(refLogEntriesStart, result);
        
        // Middle.
        refLogEntriesStart.clear();
        refLogEntriesStart.add(logEntryB);
        refLogEntriesStart.add(logEntryC);
        refLogEntriesStart.add(logEntryD);
        timePeriod = TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 6, 0, 0), LocalDateTime.of(2017, 2, 5, 12, 10), zoneId);
        result = logBook.getLogEntriesInTimePeriod(timePeriod, null);
        assertLogEntriesCollections(refLogEntriesStart, result);

        // Custom sorting...
        TreeSet<LogEntry> refLogEntriesEnd = new TreeSet<>(new LogEntry.EndComparator());
        TreeSet<LogEntry> resultEnd = new TreeSet<>(new LogEntry.EndComparator());
        refLogEntriesEnd.add(logEntryB);
        refLogEntriesEnd.add(logEntryC);
        refLogEntriesEnd.add(logEntryD);
        logBook.getLogEntriesInTimePeriod(timePeriod, resultEnd);
        assertLogEntriesCollections(refLogEntriesEnd, resultEnd);
        
        // End.
        refLogEntriesStart.clear();
        timePeriod = TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 2, 7, 12, 30), LocalDateTime.of(2017, 2, 8, 12, 10), zoneId);
        result = logBook.getLogEntriesInTimePeriod(timePeriod, null);
        assertLogEntriesCollections(refLogEntriesStart, result);
    }

    /**
     * Test of getLogEntriesWithDate method, of class LogBook.
     */
    @Test
    public void testGetLogEntriesWithDate() {
        System.out.println("getLogEntriesWithDate");
        ZoneId zoneId = null;
        
        LogBook logBook = new LogBook();
        
        LogEntry logEntryA = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 5, 12, 0), LocalDateTime.of(2017, 1, 5, 12, 30), zoneId), zoneId, "Entry A");
        logBook.addLogEntry(logEntryA);
        
        LogEntry logEntryB = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 5, 12, 0), LocalDateTime.of(2017, 2, 7, 12, 30), zoneId), zoneId, "Entry B");
        logBook.addLogEntry(logEntryB);
        
        LogEntry logEntryC = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 2, 1, 12, 0), LocalDateTime.of(2017, 2, 4, 12, 30), zoneId), zoneId, "Entry C");
        logBook.addLogEntry(logEntryC);
        
        LogEntry logEntryD = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 2, 3, 12, 0), LocalDateTime.of(2017, 2, 4, 12, 30), zoneId), zoneId, "Entry D");
        logBook.addLogEntry(logEntryD);
        
        LogEntry logEntryE = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 2, 13, 12, 0), LocalDateTime.of(2017, 2, 14, 12, 30), zoneId), zoneId, "Entry E");
        logBook.addLogEntry(logEntryE);
        
        TreeSet<LogEntry> refLogEntriesStart = new TreeSet<>(new LogEntry.StartComparator());
        Collection<LogEntry> result;
        
        // Before everything.
        refLogEntriesStart.clear();
        result = logBook.getLogEntriesWithDate(LocalDate.of(2017, 1, 4), null);
        assertLogEntriesCollections(refLogEntriesStart, result);
        
        refLogEntriesStart.clear();
        refLogEntriesStart.add(logEntryA);
        refLogEntriesStart.add(logEntryB);
        result = logBook.getLogEntriesWithDate(LocalDate.of(2017, 1, 5), null);
        assertLogEntriesCollections(refLogEntriesStart, result);
        
        refLogEntriesStart.clear();
        refLogEntriesStart.add(logEntryB);
        refLogEntriesStart.add(logEntryC);
        refLogEntriesStart.add(logEntryD);
        result = logBook.getLogEntriesWithDate(LocalDate.of(2017, 2, 4), null);
        assertLogEntriesCollections(refLogEntriesStart, result);
        
        // Sort test..
        TreeSet<LogEntry> refLogEntriesEnd = new TreeSet<>(new LogEntry.EndComparator());
        TreeSet<LogEntry> resultEnd = new TreeSet<>(new LogEntry.EndComparator());
        refLogEntriesEnd.add(logEntryB);
        refLogEntriesEnd.add(logEntryC);
        refLogEntriesEnd.add(logEntryD);
        logBook.getLogEntriesWithDate(LocalDate.of(2017, 2, 4), resultEnd);
        assertLogEntriesCollections(refLogEntriesEnd, resultEnd);
        
        /// Between dates.
        refLogEntriesStart.clear();
        result = logBook.getLogEntriesWithDate(LocalDate.of(2017, 12, 4), null);
        assertLogEntriesCollections(refLogEntriesStart, result);

        // Last date
        refLogEntriesStart.clear();
        refLogEntriesStart.add(logEntryE);
        result = logBook.getLogEntriesWithDate(LocalDate.of(2017, 2, 13), null);
        assertLogEntriesCollections(refLogEntriesStart, result);

        result = logBook.getLogEntriesWithDate(LocalDate.of(2017, 2, 14), null);
        assertLogEntriesCollections(refLogEntriesStart, result);

        // After everything.
        refLogEntriesStart.clear();
        result = logBook.getLogEntriesWithDate(LocalDate.of(2017, 2, 15), null);
        assertLogEntriesCollections(refLogEntriesStart, result);
    }

    /**
     * Test of addLogEntry method, of class LogBook.
     */
/*    @Test
    public void testAddLogEntry() {
        System.out.println("addLogEntry");
        LogEntry entry = null;
        LogBook instance = null;
        instance.addLogEntry(entry);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of addLogEntries method, of class LogBook.
     */
    @Test
    public void testAddLogEntries() {
        System.out.println("addLogEntries");
        
        ZoneId zoneId = null;
        LogBook logBook = new LogBook();
        
        TestListener listener = new TestListener();
        logBook.addListener(listener);
        
        LogEntry logEntryA = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 5, 12, 0), LocalDateTime.of(2017, 1, 5, 12, 30), zoneId), zoneId, "Entry A");
        logBook.addLogEntry(logEntryA);
        listener.assertAddedCount(1);
        
        LogEntry logEntryB = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 6, 12, 0), LocalDateTime.of(2017, 1, 6, 12, 30), zoneId), zoneId, "Entry B");
        logBook.addLogEntry(logEntryB);
        
        LogEntry logEntryC = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 7, 12, 0), LocalDateTime.of(2017, 1, 7, 12, 30), zoneId), zoneId, "Entry C");
        logBook.addLogEntry(logEntryC);
        
        LogEntry logEntryD = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 6, 12, 0), LocalDateTime.of(2017, 1, 7, 12, 30), zoneId), zoneId, "Entry D");
        logBook.addLogEntry(logEntryD);
        listener.assertAddedCount(3);


        TreeSet<LogEntry> refSortedLogEntries = new TreeSet<>(new LogEntry.StartComparator());
        refSortedLogEntries.add(logEntryA);
        refSortedLogEntries.add(logEntryB);
        refSortedLogEntries.add(logEntryC);
        refSortedLogEntries.add(logEntryD);
        
        List<LogEntry> testLogEntries = logBook.getLogEntriesByStart();
        assertLogEntriesCollections(refSortedLogEntries, testLogEntries);
        
        // Test add existing...
        logBook.addLogEntry(logEntryB);
        testLogEntries = logBook.getLogEntriesByStart();
        assertLogEntriesCollections(refSortedLogEntries, testLogEntries);

        
        // Test replace existing log entry.
        TimePeriod timePeriod = TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 10, 12, 0), LocalDateTime.of(2017, 1, 10, 12, 30), zoneId);
        LogEntry logEntryB_1 = new LogEntry(logEntryB.getGuid(), timePeriod, zoneId);
        
        logBook.addLogEntry(logEntryB_1);
        refSortedLogEntries.clear();
        refSortedLogEntries.add(logEntryA);
        refSortedLogEntries.add(logEntryB_1);
        refSortedLogEntries.add(logEntryC);
        refSortedLogEntries.add(logEntryD);
        
        testLogEntries = logBook.getLogEntriesByStart();
        assertLogEntriesCollections(refSortedLogEntries, testLogEntries);


        // Verify the entries by date are not screwed up.
        TreeMap<LocalDate, Collection<LogEntry>> refEntriesByDate = new TreeMap<>();
//        LogEntry logEntryA = newLogEntry(TimePeriod.fromEdgeTimes(
//                LocalDateTime.of(2017, 1, 5, 12, 0), LocalDateTime.of(2017, 1, 5, 12, 30), zoneId), zoneId, "Entry A");
        addLogEntryDate(refEntriesByDate, LocalDate.of(2017, 1, 5), logEntryA);
        
//        TimePeriod timePeriod = TimePeriod.fromEdgeTimes(
//                LocalDateTime.of(2017, 1, 10, 12, 0), LocalDateTime.of(2017, 1, 10, 12, 30), zoneId);
        addLogEntryDate(refEntriesByDate, LocalDate.of(2017, 1, 10), logEntryB_1);
        
//        LogEntry logEntryC = newLogEntry(TimePeriod.fromEdgeTimes(
//                LocalDateTime.of(2017, 1, 7, 12, 0), LocalDateTime.of(2017, 1, 7, 12, 30), zoneId), zoneId, "Entry C");
        addLogEntryDate(refEntriesByDate, LocalDate.of(2017, 1, 7), logEntryC);

//        LogEntry logEntryD = newLogEntry(TimePeriod.fromEdgeTimes(
//                LocalDateTime.of(2017, 1, 6, 12, 0), LocalDateTime.of(2017, 1, 7, 12, 30), zoneId), zoneId, "Entry D");
        addLogEntryDate(refEntriesByDate, LocalDate.of(2017, 1, 6), logEntryD);
        addLogEntryDate(refEntriesByDate, LocalDate.of(2017, 1, 7), logEntryD);
        assertLogEntriesByDate(refEntriesByDate, logBook);

        
        listener.assertRemovedCount(1);
        listener.assertAddedCount(1);
    }

    /**
     * Test of removeLogEntries method, of class LogBook.
     */
    @Test
    public void testRemoveLogEntries_StringArr() {
        System.out.println("removeLogEntries");
        
        ZoneId zoneId = null;
        LogBook logBook = new LogBook();
        
        TestListener listener = new TestListener();
        logBook.addListener(listener);
        
        LogEntry logEntryA = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 5, 12, 0), LocalDateTime.of(2017, 1, 5, 12, 30), zoneId), zoneId, "Entry A");
        logBook.addLogEntry(logEntryA);
        listener.assertAddedCount(1);
        
        LogEntry logEntryB = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 6, 12, 0), LocalDateTime.of(2017, 1, 6, 12, 30), zoneId), zoneId, "Entry B");
        logBook.addLogEntry(logEntryB);
        
        LogEntry logEntryC = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 7, 12, 0), LocalDateTime.of(2017, 1, 7, 12, 30), zoneId), zoneId, "Entry C");
        logBook.addLogEntry(logEntryC);
        
        LogEntry logEntryD = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 6, 12, 0), LocalDateTime.of(2017, 1, 7, 12, 30), zoneId), zoneId, "Entry D");
        logBook.addLogEntry(logEntryD);
        listener.assertAddedCount(3);


        TreeSet<LogEntry> refSortedLogEntries = new TreeSet<>(new LogEntry.StartComparator());
        refSortedLogEntries.add(logEntryA);
        refSortedLogEntries.add(logEntryB);
        refSortedLogEntries.add(logEntryC);
        refSortedLogEntries.add(logEntryD);
        
        List<LogEntry> testLogEntries = logBook.getLogEntriesByStart();
        assertLogEntriesCollections(refSortedLogEntries, testLogEntries);
        
        // Remove a middle entry.
        logBook.removeLogEntry(logEntryB.getGuid());
        refSortedLogEntries.clear();
        refSortedLogEntries.add(logEntryA);
        refSortedLogEntries.add(logEntryC);
        refSortedLogEntries.add(logEntryD);
        testLogEntries = logBook.getLogEntriesByStart();
        assertLogEntriesCollections(refSortedLogEntries, testLogEntries);
        listener.assertRemovedCount(1);
        
        // Remove ends.
        String [] guids = new String [] {
            logEntryA.getGuid(),
            logEntryD.getGuid(),
        };
        logBook.removeLogEntries(guids);
        refSortedLogEntries.clear();
        refSortedLogEntries.add(logEntryC);
        testLogEntries = logBook.getLogEntriesByStart();
        assertLogEntriesCollections(refSortedLogEntries, testLogEntries);
        listener.assertRemovedCount(2);
        
    }

    /**
     * Test of removeLogEntry method, of class LogBook.
     */
/*    @Test
    public void testRemoveLogEntry_String() {
        System.out.println("removeLogEntry");
        String guid = "";
        LogBook instance = null;
        instance.removeLogEntry(guid);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of removeLogEntries method, of class LogBook.
     */
    @Test
    public void testRemoveLogEntries_LogEntryArr() {
        System.out.println("removeLogEntries");
        
        ZoneId zoneId = null;
        LogBook logBook = new LogBook();
        
        TestListener listener = new TestListener();
        logBook.addListener(listener);
        
        LogEntry logEntryA = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 5, 12, 0), LocalDateTime.of(2017, 1, 5, 12, 30), zoneId), zoneId, "Entry A");
        logBook.addLogEntry(logEntryA);
        listener.assertAddedCount(1);
        
        LogEntry logEntryB = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 6, 12, 0), LocalDateTime.of(2017, 1, 6, 12, 30), zoneId), zoneId, "Entry B");
        logBook.addLogEntry(logEntryB);
        
        LogEntry logEntryC = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 7, 12, 0), LocalDateTime.of(2017, 1, 7, 12, 30), zoneId), zoneId, "Entry C");
        logBook.addLogEntry(logEntryC);
        
        LogEntry logEntryD = newLogEntry(TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 1, 6, 12, 0), LocalDateTime.of(2017, 1, 7, 12, 30), zoneId), zoneId, "Entry D");
        logBook.addLogEntry(logEntryD);
        listener.assertAddedCount(3);


        TreeSet<LogEntry> refSortedLogEntries = new TreeSet<>(new LogEntry.StartComparator());
        refSortedLogEntries.add(logEntryA);
        refSortedLogEntries.add(logEntryB);
        refSortedLogEntries.add(logEntryC);
        refSortedLogEntries.add(logEntryD);
        
        List<LogEntry> testLogEntries = logBook.getLogEntriesByStart();
        assertLogEntriesCollections(refSortedLogEntries, testLogEntries);
        
        // Remove a middle entry.
        logBook.removeLogEntry(logEntryB);
        refSortedLogEntries.clear();
        refSortedLogEntries.add(logEntryA);
        refSortedLogEntries.add(logEntryC);
        refSortedLogEntries.add(logEntryD);
        testLogEntries = logBook.getLogEntriesByStart();
        assertLogEntriesCollections(refSortedLogEntries, testLogEntries);
        listener.assertRemovedCount(1);
        
        // Remove ends.
        LogEntry [] logEntries = new LogEntry [] {
            logEntryA,
            logEntryD,
        };
        logBook.removeLogEntries(logEntries);
        refSortedLogEntries.clear();
        refSortedLogEntries.add(logEntryC);
        testLogEntries = logBook.getLogEntriesByStart();
        assertLogEntriesCollections(refSortedLogEntries, testLogEntries);
        listener.assertRemovedCount(2);
    }

    /**
     * Test of removeLogEntry method, of class LogBook.
     */
/*    @Test
    public void testRemoveLogEntry_LogEntry() {
        System.out.println("removeLogEntry");
        LogEntry logEntry = null;
        LogBook instance = null;
        instance.removeLogEntry(logEntry);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of addListener method, of class LogBook.
     */
/*    @Test
    public void testAddListener() {
        System.out.println("addListener");
        LogBook.Listener listener = null;
        LogBook instance = null;
        instance.addListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of removeListener method, of class LogBook.
     */
/*    @Test
    public void testRemoveListener() {
        System.out.println("removeListener");
        LogBook.Listener listener = null;
        LogBook instance = null;
        instance.removeListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/  
    /**
     * Test of fromJSON, toJSON, toJSONObject methods, of class LogBook.
     */
    @Test
    public void testJSON() {
        System.out.println("json");
        ZoneId zoneId = ZoneId.of("Europe/Paris");

        LogBook refLogBook = newTestLogBook(zoneId);
        JSONObject jsonObjectDst = refLogBook.toJSONObject();
        
        String jsonText = jsonObjectDst.toString();
        JSONObject jsonObject = new JSONObject(jsonText);
        
        LogBook testLogBook = LogBook.fromJSON(jsonObject);
        assertEquals(refLogBook, testLogBook);
        
        
        refLogBook.setName("New Name");
        assertNotEquals(refLogBook, testLogBook);
        
        refLogBook.removeLogEntry(refLogBook.getLogEntriesByStart().get(0));
        assertNotEquals(refLogBook, testLogBook);
        
        jsonObjectDst = refLogBook.toJSONObject();
        jsonText = jsonObjectDst.toString();
        jsonObject = new JSONObject(jsonText);
        
        testLogBook = LogBook.fromJSON(jsonObject);
        assertEquals(refLogBook, testLogBook);
    }
}
