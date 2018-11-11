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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Iterator;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Albert Santos
 */
public class TimePeriodTest {
    
    public TimePeriodTest() {
    }

    /**
     * Test of equals method, of class TimePeriod.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        TimePeriod a = TimePeriod.fromEdgeTimes(LocalDateTime.of(2017, 10, 2, 10, 20), LocalDateTime.of(2017, 10, 2, 10, 21), null);
        TimePeriod b = TimePeriod.fromEdgeTimes(LocalDateTime.of(2017, 10, 2, 10, 20), LocalDateTime.of(2017, 10, 2, 10, 22), null);
        TimePeriod c = TimePeriod.fromEdgeTimes(LocalDateTime.of(2017, 10, 2, 10, 20), LocalDateTime.of(2017, 10, 2, 10, 22), null);
        assertEquals(false, a.equals(b));
        assertEquals(false, b.equals(a));
        assertEquals(true, b.equals(c));
        assertEquals(true, c.equals(b));
        
        a = TimePeriod.fromEdgeDates(LocalDate.of(2018, 11, 9), LocalDate.of(2018, 11, 9));
        b = TimePeriod.fromEdgeDates(LocalDate.of(2018, 11, 9), LocalDate.of(2018, 11, 10));
        c = TimePeriod.fromEdgeDates(LocalDate.of(2018, 11, 9), LocalDate.of(2018, 11, 10));
        assertEquals(false, a.equals(b));
        assertEquals(false, b.equals(a));
        assertEquals(true, b.equals(c));
        assertEquals(true, c.equals(b));
    }

    /**
     * Test of getDuration method, of class TimePeriod.
     */
    @Test
    public void testGetDuration() {
        System.out.println("getDuration");
        TimePeriod a = TimePeriod.fromEdgeTimes(LocalDateTime.of(2017, 10, 2, 10, 20), LocalDateTime.of(2017, 10, 2, 10, 21), null);
        Duration duration = a.getDuration();
        assertEquals(Duration.ofMinutes(1), duration);
        
        TimePeriod b = TimePeriod.fromEdgeDates(LocalDate.of(2018, 11, 9), LocalDate.of(2018, 11, 9));
        duration = b.getDuration();
        assertEquals(Duration.ofDays(1), duration);
        
        b = TimePeriod.fromEdgeDates(LocalDate.of(2018, 11, 11), LocalDate.of(2018, 11, 9));
        duration = b.getDuration();
        assertEquals(Duration.ofDays(3), duration);
    }

    /**
     * Test of compareTo method, of class TimePeriod.
     */
    @Test
    public void testCompareTo() {
        System.out.println("compareTo");
        TimePeriod a = TimePeriod.fromEdgeTimes(LocalDateTime.of(2017, 10, 2, 10, 20), LocalDateTime.of(2017, 10, 3, 10, 21), null);
        TimePeriod b = TimePeriod.fromEdgeTimes(LocalDateTime.of(2017, 10, 2, 10, 20), LocalDateTime.of(2017, 10, 3, 10, 22), null);
        TimePeriod c = TimePeriod.fromEdgeTimes(LocalDateTime.of(2017, 10, 2, 10, 20), LocalDateTime.of(2017, 10, 3, 10, 22), null);
        TimePeriod d = TimePeriod.fromEdgeTimes(LocalDateTime.of(2017, 10, 2, 10, 25), LocalDateTime.of(2017, 10, 3, 10, 21), null);
        assertEquals(-1, a.compareTo(b));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(c));
        assertEquals(0, c.compareTo(b));
        assertEquals(-1, a.compareTo(c));
        assertEquals(-1, a.compareTo(d));
        assertEquals(1, d.compareTo(a));
        
        TimePeriod x = TimePeriod.fromEdgeDates(LocalDate.of(2017, 10, 1), LocalDate.of(2017, 10, 1));
        assertEquals(-1, x.compareTo(a));
        assertEquals(1, a.compareTo(x));
        
        TimePeriod y = TimePeriod.fromEdgeDates(LocalDate.of(2017, 10, 2), LocalDate.of(2017, 10, 3));
        assertEquals(1, y.compareTo(x));
        assertEquals(-1, x.compareTo(y));
        assertEquals(-1, y.compareTo(a));
        assertEquals(1, a.compareTo(y));
        
        TimePeriod z = TimePeriod.fromEdgeDates(LocalDate.of(2017, 10, 2), LocalDate.of(2017, 10, 2));
        assertEquals(-1, z.compareTo(y));
        assertEquals(1, y.compareTo(z));
        
        z = TimePeriod.fromEdgeDates(LocalDate.of(2017, 10, 3), LocalDate.of(2017, 10, 2));
        assertEquals(0, z.compareTo(y));
        assertEquals(0, y.compareTo(z));
    }

    /**
     * Test of getTimeRelation method, of class TimePeriod.
     */
    @Test
    public void testGetTimeRelation_Instant() {
        System.out.println("getTimeRelation-Instant");
        ZoneId zoneId = ZoneId.of("Europe/Paris");
        LocalDateTime dateTimeA = LocalDateTime.of(2017, 3, 12, 12, 34);
        LocalDateTime dateTimeB = LocalDateTime.of(2017, 3, 12, 17, 34);
        LocalDateTime dateTimeC = LocalDateTime.of(2017, 4, 15, 2, 0);
        LocalDateTime dateTimeD = LocalDateTime.of(2017, 5, 1, 3, 0);
        LocalDateTime dateTimeE = LocalDateTime.of(2017, 5, 1, 3, 1);

        Instant instantA = dateTimeA.atZone(zoneId).toInstant();
        Instant instantB = dateTimeB.atZone(zoneId).toInstant();
        Instant instantC = dateTimeC.atZone(zoneId).toInstant();
        Instant instantD = dateTimeD.atZone(zoneId).toInstant();
        Instant instantE = dateTimeE.atZone(zoneId).toInstant();
        
        TimePeriod period = TimePeriod.fromEdgeTimes(instantB, instantD);
        assertEquals(TimePeriod.Relation.BEFORE, period.getTimeRelation(instantA, zoneId));
        assertEquals(TimePeriod.Relation.ON_START_EDGE, period.getTimeRelation(instantB, zoneId));
        assertEquals(TimePeriod.Relation.INSIDE, period.getTimeRelation(instantC, zoneId));
        assertEquals(TimePeriod.Relation.ON_END_EDGE, period.getTimeRelation(instantD, zoneId));
        assertEquals(TimePeriod.Relation.AFTER, period.getTimeRelation(instantE, zoneId));
    }
    
    @Test
    public void testGetTimeRelation_Instant_FullDays() {
        System.out.println("getTimeRelation-Instant-FullDays");
        
        ZoneId zoneId = ZoneId.of("Europe/Paris");
        Instant instantA = LocalDateTime.of(2018, 11, 9, 23, 59).atZone(zoneId).toInstant();
        Instant instantB = LocalDateTime.of(2018, 11, 10, 0, 0).atZone(zoneId).toInstant();
        Instant instantC = LocalDateTime.of(2018, 11, 10, 0, 1).atZone(zoneId).toInstant();
        Instant instantD = LocalDateTime.of(2018, 11, 11, 0, 0).atZone(zoneId).toInstant();
        Instant instantE = LocalDateTime.of(2018, 11, 11, 0, 1).atZone(zoneId).toInstant();
        Instant instantF = LocalDateTime.of(2018, 11, 12, 0, 0).atZone(zoneId).toInstant();
        Instant instantG = LocalDateTime.of(2018, 11, 12, 0, 1).atZone(zoneId).toInstant();
        
        LocalDate firstDate = LocalDate.of(2018, 11, 10);
        LocalDate lastDate = LocalDate.of(2018, 11, 11);
        
        TimePeriod period = TimePeriod.fromEdgeDates(firstDate, firstDate);
        assertEquals(TimePeriod.Relation.BEFORE, period.getTimeRelation(instantA, zoneId));
        assertEquals(TimePeriod.Relation.ON_START_EDGE, period.getTimeRelation(instantB, zoneId));
        assertEquals(TimePeriod.Relation.INSIDE, period.getTimeRelation(instantC, zoneId));
        assertEquals(TimePeriod.Relation.ON_END_EDGE, period.getTimeRelation(instantD, zoneId));
        assertEquals(TimePeriod.Relation.AFTER, period.getTimeRelation(instantE, zoneId));
        
        period = TimePeriod.fromEdgeDates(firstDate, lastDate);
        assertEquals(TimePeriod.Relation.BEFORE, period.getTimeRelation(instantA, zoneId));
        assertEquals(TimePeriod.Relation.ON_START_EDGE, period.getTimeRelation(instantB, zoneId));
        assertEquals(TimePeriod.Relation.INSIDE, period.getTimeRelation(instantC, zoneId));
        assertEquals(TimePeriod.Relation.ON_END_EDGE, period.getTimeRelation(instantF, zoneId));
        assertEquals(TimePeriod.Relation.AFTER, period.getTimeRelation(instantG, zoneId));
    }

    /**
     * Test of getTimeRelation method, of class TimePeriod.
     */
    @Test
    public void testGetTimeRelation_LocalDateTime_ZoneId() {
        System.out.println("getTimeRelation");
        ZoneId zoneId = ZoneId.of("Europe/Paris");
        LocalDateTime dateTimeA = LocalDateTime.of(2017, 3, 12, 12, 34);
        LocalDateTime dateTimeB = LocalDateTime.of(2017, 3, 12, 17, 34);
        LocalDateTime dateTimeC = LocalDateTime.of(2017, 4, 15, 2, 0);
        LocalDateTime dateTimeD = LocalDateTime.of(2017, 5, 1, 3, 0);
        LocalDateTime dateTimeE = LocalDateTime.of(2017, 5, 1, 3, 1);
        
        TimePeriod period = TimePeriod.fromEdgeTimes(dateTimeB, dateTimeD, zoneId);
        assertEquals(TimePeriod.Relation.BEFORE, period.getTimeRelation(dateTimeA, zoneId));
        assertEquals(TimePeriod.Relation.ON_START_EDGE, period.getTimeRelation(dateTimeB, zoneId));
        assertEquals(TimePeriod.Relation.INSIDE, period.getTimeRelation(dateTimeC, zoneId));
        assertEquals(TimePeriod.Relation.ON_END_EDGE, period.getTimeRelation(dateTimeD, zoneId));
        assertEquals(TimePeriod.Relation.AFTER, period.getTimeRelation(dateTimeE, zoneId));
    }

    /**
     * Test of getOverlap method, of class TimePeriod.
     */
    @Test
    public void testGetOverlap() {
        System.out.println("getOverlap");
        ZoneId zoneId = ZoneId.of("Europe/Paris");
        LocalDateTime dateTimeA = LocalDateTime.of(2017, 3, 12, 12, 34);
        LocalDateTime dateTimeB = LocalDateTime.of(2017, 3, 12, 17, 34);
        LocalDateTime dateTimeC = LocalDateTime.of(2017, 4, 15, 2, 0);
        LocalDateTime dateTimeD = LocalDateTime.of(2017, 5, 1, 3, 0);
        
        TimePeriod periodA = TimePeriod.fromEdgeTimes(dateTimeA, dateTimeB, zoneId);
        TimePeriod periodB = TimePeriod.fromEdgeTimes(dateTimeC, dateTimeD, zoneId);
        
        assertEquals(TimePeriod.Overlap.NONE, periodA.getOverlap(periodB, null));
        assertEquals(TimePeriod.Overlap.NONE, periodB.getOverlap(periodA, null));
        
        periodA = TimePeriod.fromEdgeTimes(dateTimeA, dateTimeC, zoneId);
        periodB = TimePeriod.fromEdgeTimes(dateTimeC, dateTimeD, zoneId);
        assertEquals(TimePeriod.Overlap.TOUCH_OTHER_START, periodA.getOverlap(periodB, null));
        assertEquals(TimePeriod.Overlap.TOUCH_OTHER_END, periodB.getOverlap(periodA, null));
        
        periodA = TimePeriod.fromEdgeTimes(dateTimeA, dateTimeC, zoneId);
        periodB = TimePeriod.fromEdgeTimes(dateTimeB, dateTimeD, zoneId);
        assertEquals(TimePeriod.Overlap.OTHER_START, periodA.getOverlap(periodB, null));
        assertEquals(TimePeriod.Overlap.OTHER_END, periodB.getOverlap(periodA, null));
        
        periodA = TimePeriod.fromEdgeTimes(dateTimeA, dateTimeC, zoneId);
        periodB = TimePeriod.fromEdgeTimes(dateTimeA, dateTimeC, zoneId);
        assertEquals(TimePeriod.Overlap.SAME, periodA.getOverlap(periodB, null));
        assertEquals(TimePeriod.Overlap.SAME, periodB.getOverlap(periodA, null));
        
        periodA = TimePeriod.fromEdgeTimes(dateTimeA, dateTimeD, zoneId);
        periodB = TimePeriod.fromEdgeTimes(dateTimeB, dateTimeC, zoneId);
        assertEquals(TimePeriod.Overlap.ENCLOSES_OTHER, periodA.getOverlap(periodB, null));
        assertEquals(TimePeriod.Overlap.INSIDE_OTHER, periodB.getOverlap(periodA, null));
        
        periodA = TimePeriod.fromEdgeTimes(dateTimeA, dateTimeC, zoneId);
        periodB = TimePeriod.fromEdgeTimes(dateTimeB, dateTimeC, zoneId);
        assertEquals(TimePeriod.Overlap.ENCLOSES_OTHER, periodA.getOverlap(periodB, null));
        assertEquals(TimePeriod.Overlap.INSIDE_OTHER, periodB.getOverlap(periodA, null));
        
        periodA = TimePeriod.fromEdgeTimes(dateTimeA, dateTimeC, zoneId);
        periodB = TimePeriod.fromEdgeTimes(dateTimeA, dateTimeD, zoneId);
        assertEquals(TimePeriod.Overlap.INSIDE_OTHER, periodA.getOverlap(periodB, null));
        assertEquals(TimePeriod.Overlap.ENCLOSES_OTHER, periodB.getOverlap(periodA, null));
    }

    /**
     * Test of containsDate method, of class TimePeriod.
     */
    @Test
    public void testContainsDate() {
        System.out.println("containsDate");
        ZoneId zoneId = ZoneId.of("Europe/Paris");
        LocalDateTime dateTimeA = LocalDateTime.of(2017, 3, 12, 0, 0);
        LocalDateTime dateTimeB = LocalDateTime.of(2017, 4, 12, 10, 30);
        TimePeriod timePeriod = TimePeriod.fromEdgeTimes(dateTimeA, dateTimeB, zoneId);

        LocalDate date = LocalDate.of(2017, 3, 12);
        boolean result = timePeriod.containsDate(date, zoneId);
        assertTrue(result);
        
        date = LocalDate.of(2017, 3, 11);
        result = timePeriod.containsDate(date, zoneId);
        assertFalse(result);
        
        date = LocalDate.of(2017, 4, 12);
        result = timePeriod.containsDate(date, zoneId);
        assertTrue(result);
        
        date = LocalDate.of(2017, 4, 13);
        result = timePeriod.containsDate(date, zoneId);
        assertFalse(result);
        
        
        LocalDate firstDate = LocalDate.of(2018, 11, 9);
        timePeriod = TimePeriod.fromEdgeDates(firstDate, firstDate);

        date = LocalDate.of(2018, 11, 8);
        assertFalse(timePeriod.containsDate(date, zoneId));

        date = LocalDate.of(2018, 11, 9);
        assertTrue(timePeriod.containsDate(date, zoneId));
        
        date = LocalDate.of(2018, 11, 10);
        assertFalse(timePeriod.containsDate(date, zoneId));
    }
    
    void checkLocalDates(LocalDate [] refDates, Collection<LocalDate> testDates) {
        assertEquals(refDates.length, testDates.size());
        Iterator<LocalDate> iterator = testDates.iterator();
        for (int i = 0; i < refDates.length; ++i) {
            assertEquals(refDates[i], iterator.next());
            iterator.hasNext();
        }
    }
    
    @Test
    public void testGetDates() {
        System.out.println("getDates");

        ZoneId zoneId = ZoneId.of("Europe/Paris");
        LocalDateTime dateTimeA = LocalDateTime.of(2017, 3, 12, 1, 0);
        LocalDateTime dateTimeB = LocalDateTime.of(2017, 3, 14, 10, 30);
        TimePeriod timePeriod = TimePeriod.fromEdgeTimes(dateTimeA, dateTimeB, zoneId);
        
        Collection<LocalDate> testDates = timePeriod.getDates(null, zoneId);
        checkLocalDates(
                new LocalDate[] { LocalDate.of(2017, 3, 12), LocalDate.of(2017, 3, 13), LocalDate.of(2017, 3, 14) }, 
                testDates);

        // Check the edge times...
        dateTimeA = LocalDateTime.of(2017, 3, 12, 0, 0);
        dateTimeB = LocalDateTime.of(2017, 3, 15, 0, 0);
        timePeriod = TimePeriod.fromEdgeTimes(dateTimeA, dateTimeB, zoneId);
        testDates = timePeriod.getDates(null, zoneId);
        checkLocalDates(
                new LocalDate[] { LocalDate.of(2017, 3, 12), LocalDate.of(2017, 3, 13), LocalDate.of(2017, 3, 14) }, 
                testDates);
        
        // Also testing adding to existing collection, collection is not cleared...
        LocalDate firstDate = LocalDate.of(2018, 11, 9);
        LocalDate lastDate = LocalDate.of(2018, 11, 10);
        timePeriod = TimePeriod.fromEdgeDates(firstDate, lastDate);
        timePeriod.getDates(testDates, zoneId);
        checkLocalDates(
                new LocalDate[] { LocalDate.of(2017, 3, 12), LocalDate.of(2017, 3, 13), LocalDate.of(2017, 3, 14),
                    firstDate, lastDate
                }, 
                testDates);
    }

    /**
     * Test of fromEdgeTimes method, of class TimePeriod.
     */
    @Test
    public void testFromEdgeTimes_Instant_Instant() {
        System.out.println("fromEdgeTimes");
        ZoneId zoneId = ZoneId.of("Europe/Paris");
        Instant instantA = LocalDateTime.of(2017, 3, 2, 14, 0).atZone(zoneId).toInstant();
        Instant instantB = LocalDateTime.of(2016, 3, 2, 14, 0).atZone(zoneId).toInstant();
        TimePeriod result = TimePeriod.fromEdgeTimes(instantA, instantB);
        assertTrue(instantB.equals(result.getStartInstant()));
        assertTrue(instantA.equals(result.getEndInstant()));
        
        result = TimePeriod.fromEdgeTimes(instantB, instantA);
        assertTrue(instantB.equals(result.getStartInstant()));
        assertTrue(instantA.equals(result.getEndInstant()));
    }

    /**
     * Test of fromEdgeAndDuration method, of class TimePeriod.
     */
    @Test
    public void testFromEdgeAndDuration_Instant_Duration() {
        System.out.println("fromEdgeAndDuration");
        ZoneId zoneId = ZoneId.of("Europe/Paris");
        Instant instantA = LocalDateTime.of(2017, 3, 2, 14, 0).atZone(zoneId).toInstant();
        Duration duration = Duration.ofHours(10);
        Instant instantB = instantA.plus(duration);
        
        TimePeriod result = TimePeriod.fromEdgeAndDuration(instantA, duration);
        assertTrue(instantA.equals(result.getStartInstant()));
        assertTrue(instantB.equals(result.getEndInstant()));
        assertTrue(duration.equals(result.getDuration()));
        
        duration = Duration.ofHours(-10);
        instantB = LocalDateTime.of(2017, 3, 2, 4, 0).atZone(zoneId).toInstant();
        result = TimePeriod.fromEdgeAndDuration(instantA, duration);
        assertTrue(instantB.equals(result.getStartInstant()));
        assertTrue(instantA.equals(result.getEndInstant()));
        assertTrue(result.getDuration().equals(duration.abs()));
    }

    /**
     * Test of fromEdgeTimes method, of class TimePeriod.
     */
    @Test
    public void testFromEdgeTimes_3args() {
        System.out.println("fromEdgeTimes");
        ZoneId zoneId = ZoneId.of("Europe/Paris");
        LocalDateTime dateTimeA = LocalDateTime.of(2017, 3, 12, 0, 0);
        LocalDateTime dateTimeB = LocalDateTime.of(2017, 4, 12, 10, 30);
        TimePeriod result = TimePeriod.fromEdgeTimes(dateTimeA, dateTimeB, zoneId);
        
        Instant instantA = dateTimeA.atZone(zoneId).toInstant();
        Instant instantB = dateTimeB.atZone(zoneId).toInstant();
        assertTrue(instantA.equals(result.getStartInstant()));
        assertTrue(instantB.equals(result.getEndInstant()));
    }

    /**
     * Test of fromEdgeAndDuration method, of class TimePeriod.
     */
    @Test
    public void testFromEdgeAndDuration_3args_1() {
        System.out.println("fromEdgeAndDuration");
        ZoneId zoneId = ZoneId.of("Europe/Paris");
        LocalDateTime dateTime = LocalDateTime.of(2017, 3, 12, 0, 0);
        Duration duration = Duration.ofHours(10);
        TimePeriod result = TimePeriod.fromEdgeAndDuration(dateTime, duration, zoneId);
        
        Instant instant = dateTime.atZone(zoneId).toInstant();
        assertTrue(instant.equals(result.getStartInstant()));
        assertTrue(result.getDuration().equals(duration));
        
        duration = Duration.ofHours(-10);
        result = TimePeriod.fromEdgeAndDuration(dateTime, duration, zoneId);
        assertTrue(instant.equals(result.getEndInstant()));
        assertTrue(result.getDuration().equals(duration.abs()));
    }

    /**
     * Test of fromEdgeAndDuration method, of class TimePeriod.
     */
    @Test
    public void testFromEdgeAndDuration_3args_2() {
        System.out.println("fromEdgeAndDuration");
        ZoneId zoneId = ZoneId.of("Europe/Paris");
        
        LocalDate date = LocalDate.of(2017, 4, 5);
        Duration duration = Duration.ofHours(3);
        TimePeriod result = TimePeriod.fromEdgeAndDuration(date, duration, zoneId);
        
        Instant instant = date.atStartOfDay(zoneId).toInstant();
        assertTrue(instant.equals(result.getStartInstant()));
        assertTrue(result.getDuration().equals(duration));
        
        duration = Duration.ofHours(-3);
        result = TimePeriod.fromEdgeAndDuration(date, duration, zoneId);
        assertTrue(instant.equals(result.getEndInstant()));
        assertTrue(result.getDuration().equals(duration.abs()));
    }

    /**
     * Test of fromEdgeDates method, of class TimePeriod.
     */
    @Test
    public void testFromEdgeDates() {
        System.out.println("fromEdgeDates");
        LocalDate dateA = LocalDate.of(2017, 2, 10);
        LocalDate dateB = LocalDate.of(2017, 2, 11);
        ZoneId zoneId = ZoneId.of("Europe/Paris");

        TimePeriod result = TimePeriod.fromEdgeDates(dateA, dateB);
        assertTrue(dateA.equals(result.getFirstFullDay()));
        assertTrue(dateB.equals(result.getLastFullDay()));
        assertEquals(2, result.getFullDayCount());

        result = TimePeriod.fromEdgeDates(dateB, dateA);
        assertTrue(dateA.equals(result.getFirstFullDay()));
        assertTrue(dateB.equals(result.getLastFullDay()));
        
        result = TimePeriod.fromEdgeDates(dateA, dateA);
        assertTrue(dateA.equals(result.getFirstFullDay()));
        assertTrue(dateA.equals(result.getLastFullDay()));
        assertEquals(1, result.getFullDayCount());
    }

    /**
     * Test of joinPeriods method, of class TimePeriod.
     */
    @Test
    public void testJoinPeriods() {
        System.out.println("joinPeriods");
        ZoneId zoneId = ZoneId.of("Europe/Paris");
        LocalDateTime dateTimeA = LocalDateTime.of(2017, 5, 4, 10, 30);
        LocalDateTime dateTimeB = LocalDateTime.of(2017, 6, 4, 0, 30);
        LocalDateTime dateTimeC = LocalDateTime.of(2017, 7, 10, 14, 0);
        LocalDateTime dateTimeD = LocalDateTime.of(2017, 7, 20, 20, 10);
        
        Instant instantA = dateTimeA.atZone(zoneId).toInstant();
        Instant instantB = dateTimeB.atZone(zoneId).toInstant();
        Instant instantC = dateTimeC.atZone(zoneId).toInstant();
        Instant instantD = dateTimeD.atZone(zoneId).toInstant();
        
        TimePeriod periodA = TimePeriod.fromEdgeTimes(dateTimeA, dateTimeB, zoneId);
        TimePeriod periodB = TimePeriod.fromEdgeTimes(dateTimeC, dateTimeD, zoneId);

        TimePeriod result = TimePeriod.joinPeriods(periodA, periodB);
        assertTrue(instantA.equals(result.getStartInstant()));
        assertTrue(instantD.equals(result.getEndInstant()));
        
        periodA = TimePeriod.fromEdgeTimes(dateTimeA, dateTimeC, zoneId);
        result = TimePeriod.joinPeriods(periodA, periodB);
        assertTrue(instantA.equals(result.getStartInstant()));
        assertTrue(instantD.equals(result.getEndInstant()));
        
        periodA = TimePeriod.fromEdgeTimes(dateTimeB, dateTimeC, zoneId);
        result = TimePeriod.joinPeriods(periodA, periodB);
        assertTrue(instantB.equals(result.getStartInstant()));
        assertTrue(instantD.equals(result.getEndInstant()));
        
        periodB = TimePeriod.fromEdgeTimes(dateTimeA, dateTimeD, zoneId);
        result = TimePeriod.joinPeriods(periodA, periodB);
        assertTrue(instantA.equals(result.getStartInstant()));
        assertTrue(instantD.equals(result.getEndInstant()));
        
        
        LocalDate dateA = LocalDate.of(2018, 11, 8);
        LocalDate dateB = LocalDate.of(2018, 11, 9);
        LocalDate dateC = LocalDate.of(2018, 11, 10);
        LocalDate dateD = LocalDate.of(2018, 11, 11);
        LocalDate dateE = LocalDate.of(2018, 11, 12);
        
        periodA = TimePeriod.fromEdgeDates(dateB, dateD);
        periodB = TimePeriod.fromEdgeDates(dateA, dateA);
        result = TimePeriod.joinPeriods(periodA, periodB);
        assertEquals(dateA, result.getFirstFullDay());
        assertEquals(dateD, result.getLastFullDay());
        
        periodB = TimePeriod.fromEdgeDates(dateA, dateB);
        result = TimePeriod.joinPeriods(periodA, periodB);
        assertEquals(dateA, result.getFirstFullDay());
        assertEquals(dateD, result.getLastFullDay());
        
        periodB = TimePeriod.fromEdgeDates(dateA, dateC);
        result = TimePeriod.joinPeriods(periodA, periodB);
        assertEquals(dateA, result.getFirstFullDay());
        assertEquals(dateD, result.getLastFullDay());
        
        periodB = TimePeriod.fromEdgeDates(dateA, dateE);
        result = TimePeriod.joinPeriods(periodA, periodB);
        assertEquals(dateA, result.getFirstFullDay());
        assertEquals(dateE, result.getLastFullDay());
        
        periodA = TimePeriod.fromEdgeDates(dateA, dateA);
        periodB = TimePeriod.fromEdgeDates(dateE, dateE);
        result = TimePeriod.joinPeriods(periodA, periodB);
        assertEquals(dateA, result.getFirstFullDay());
        assertEquals(dateE, result.getLastFullDay());
}

    
    /**
     * Test of zoneIdToJSON, optZoneIdFromJSON methods, of class TimePeriod.
     */
    @Test
    public void testZoneIdJSONHelpers() {
        System.out.println("zoneIdToJSON/optZoneIdFromJSON");
        
        ZoneId refZoneIdA = ZoneId.of("Europe/Paris");
        ZoneId refZoneIdB = null;
        
        JSONObject dstJSONObject = new JSONObject();
        TimePeriod.zoneIdToJSON(dstJSONObject, "refZoneIdA", refZoneIdA);
        TimePeriod.zoneIdToJSON(dstJSONObject, "refZoneIdB", refZoneIdB);
        
        String jsonText = dstJSONObject.toString();
        JSONObject jsonObject = new JSONObject(jsonText);
        ZoneId testZoneIdA = TimePeriod.optZoneIdFromJSON(jsonObject, "refZoneIdA", null);
        assertEquals(refZoneIdA, testZoneIdA);
        
        ZoneId testZoneIdB = TimePeriod.optZoneIdFromJSON(jsonObject, "refZoneIdB", refZoneIdB);
        assertEquals(refZoneIdB, testZoneIdB);
        
        assertEquals(null, TimePeriod.optZoneIdFromJSON(jsonObject, "refZoneIdB", null));
    }

    
    /**
     * Test of instantToJSON, instantFromJSON methods, of class TimePeriod.
     */
    @Test
    public void testInstantJSONHelpers() {
        System.out.println("instantToJSON/instantFromJSON");
        
        ZoneId zoneId = ZoneId.of("Europe/Paris");
        
        JSONObject dstJSONObject = new JSONObject();
        Instant instantA = LocalDateTime.of(2017, 3, 17, 14, 31, 21).atZone(zoneId).toInstant();
        TimePeriod.instantToJSON(dstJSONObject, "instantA", instantA);
        
        Instant instantB = LocalDateTime.of(2017, 12, 7, 4, 1, 2).atZone(zoneId).toInstant();
        TimePeriod.instantToJSON(dstJSONObject, "instantB", instantB);

        String jsonText = dstJSONObject.toString();
        JSONObject jsonObject = new JSONObject(jsonText);
        
        Instant testInstant = TimePeriod.instantFromJSON(jsonObject, "instantA");
        assertEquals(instantA, testInstant);
        
        testInstant = TimePeriod.instantFromJSON(jsonObject, "instantB");
        assertEquals(instantB, testInstant);
    }

    
    /**
     * Test of toJSON, fromJSON methods, of class TimePeriod.
     */
    @Test
    public void testJSON() {
        System.out.println("toJSON/fromJSON");
    
        ZoneId zoneId = null;

        JSONObject dstJSONObject = new JSONObject();

        TimePeriod timePeriodA = TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 11, 23, 4, 5), LocalDateTime.of(2017, 8, 17, 14, 24), zoneId);
        timePeriodA.toJSON(dstJSONObject, "timePeriodA");

        TimePeriod timePeriodB = TimePeriod.fromEdgeTimes(
                LocalDateTime.of(2017, 10, 23, 4, 5), LocalDateTime.of(2017, 8, 14, 14, 24), zoneId);
        timePeriodB.toJSON(dstJSONObject, "timePeriodB");
        
        TimePeriod timePeriodC = TimePeriod.fromEdgeDates(LocalDate.of(2018, 11, 9), LocalDate.of(2018, 11, 9));
        timePeriodC.toJSON(dstJSONObject, "timePeriodC");
        
        TimePeriod timePeriodD = TimePeriod.fromEdgeDates(LocalDate.of(2018, 11, 9), LocalDate.of(2018, 11, 10));
        timePeriodD.toJSON(dstJSONObject, "timePeriodD");

        String jsonText = dstJSONObject.toString();
        JSONObject jsonObject = new JSONObject(jsonText);
        
        TimePeriod testTimePeriod = TimePeriod.fromJSON(jsonObject, "timePeriodA");
        assertEquals(timePeriodA, testTimePeriod);
        
        testTimePeriod = TimePeriod.fromJSON(jsonObject, "timePeriodB");
        assertEquals(timePeriodB, testTimePeriod);
        
        testTimePeriod = TimePeriod.fromJSON(jsonObject, "timePeriodC");
        assertEquals(timePeriodC, testTimePeriod);
        
        testTimePeriod = TimePeriod.fromJSON(jsonObject, "timePeriodD");
        assertEquals(timePeriodD, testTimePeriod);
    }
}
