/*
 * Copyright 2008, Myron Marston <myron DOT marston AT gmail DOT com>
 * 
 * This file is part of Fractal Composer.
 * 
 * Fractal Composer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option any later version.
 * 
 * Fractal Composer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Fractal Composer.  If not, see <http://www.gnu.org/licenses/>. 
 */

package com.myronmarston.music.notation;

import com.myronmarston.music.Instrument;
import com.myronmarston.util.Fraction;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Myron
 */
public class NotationElementListTest {
    
    @Test
    public void getLastIndexForTupletCollection() {        
        testLastIndexForTupletCollection(1, 3, "1/4", "1/6", "1/6", "1/6", "1/8");
        testLastIndexForTupletCollection(1, -1, "1/4", "1/6", "1/6", "1/8");
        testLastIndexForTupletCollection(1, 4, "1/4", "1/6", "1/6", "1/8", "1/6", "1/4");
        testLastIndexForTupletCollection(0, 3, "1/6", "1/6", "1/12", "1/12", "1/6");
        testLastIndexForTupletCollection(0, 7, "1/6", "1/18", "1/18", "1/18", "1/4", "1/8", "1/12", "1/12", "1/10");
    }
    
    private static void testLastIndexForTupletCollection(int firstIndex, int lastIndex, String ... durations) {
        assertEquals(lastIndex, getTestList(durations).getEndIndexForTupletGroup(firstIndex));
    }
    
    @Test
    public void getSmallestNoteDenominator() {
        testGetSmallestNoteDenominator(3L, "1/6", "1/4" ,"1/3");
        testGetSmallestNoteDenominator(4L, "5/6", "3/4" ,"4/9");
    }
    
    private static void testGetSmallestNoteDenominator(long expectedResult, String ... durations) {
        NotationElementList testList = getTestList(durations);        
        assertEquals(expectedResult, testList.getSmallestNoteDurationDenominator());
    }
    
    @Test
    public void totalDurationDenomAddsToPowerOf2() {
        testTotalDurationDenomAddsToPowerOf2(true, "1/6", "1/6", "1/6");
        testTotalDurationDenomAddsToPowerOf2(true, "1/6", "1/4", "1/6", "1/6");
        testTotalDurationDenomAddsToPowerOf2(false, "1/6", "1/4", "1/5", "1/6");        
        testTotalDurationDenomAddsToPowerOf2(true, "1/6", "1/6", "1/10", "1/10", "1/10", "1/10", "1/10", "1/6");
    }
    
    private static void testTotalDurationDenomAddsToPowerOf2(boolean expectedResult, String ... durations) {
        assertEquals(expectedResult, NotationElementList.totalDurationDenomAddsToPowerOf2(getTestList(durations)));
    }
    
    @Test
    public void removeConsecutiveNotesWhoseDenomsAddToPowerOf2() {
        testRemoveConsecutiveNotesWhoseDenomsAddToPowerOf2(
            new String[] {"1/6", "1/6", "1/6"}, 
            new String[] {"1/6", "1/6", "1/6"});
        
        testRemoveConsecutiveNotesWhoseDenomsAddToPowerOf2(
            new String[] {"1/6", "1/6", "1/8", "1/6"}, 
            new String[] {"1/6", "1/6", "1/6"});
        
        testRemoveConsecutiveNotesWhoseDenomsAddToPowerOf2(
            new String[] {"1/6", "1/8", "1/6", "1/8", "1/6", "1/4"}, 
            new String[] {"1/6", "1/6", "1/6"});
        
        testRemoveConsecutiveNotesWhoseDenomsAddToPowerOf2(
            new String[] {"1/6", "1/6", "1/10", "1/10", "1/10", "1/10", "1/10", "1/6"}, 
            new String[] {"1/6", "1/6", "1/6"});
        
        testRemoveConsecutiveNotesWhoseDenomsAddToPowerOf2(
            new String[] {"1/6", "1/6", "1/10", "1/10", "1/10", "1/10", "1/6"}, 
            new String[] {"1/6", "1/6", "1/10", "1/10", "1/10", "1/10", "1/6"});
    }
    
    private static void testRemoveConsecutiveNotesWhoseDenomsAddToPowerOf2(String[] beforeDurations, String[] afterDurations) {
        NotationElementList beforeList = getTestList(beforeDurations);
        NotationElementList afterList = getTestList(afterDurations);
        beforeList.removeConsecutiveNotesWhoseDenomsAddToPowerOf2();
        
        assertEquals(afterList.size(), beforeList.size());
        for (int i = 0; i < afterList.size(); i++) {
            NotationNote beforeNote = (NotationNote) beforeList.get(i);
            NotationNote afterNote = (NotationNote) afterList.get(i);
            
            assertEquals(afterNote.getDuration(), beforeNote.getDuration());
        }
    }
    
    public static NotationElementList getTestList(String[] durations) {
        NotationElementList list = new NotationElementList();
        for (String duration : durations) {
            list.add(NotationNoteTest.instantiateTestNote(duration));
        }   
        
        return list;
    }
    
    public static NotationElementList getTestList2(String ... durations) {
        return getTestList(durations);
    }
    
    @Test
    public void groupTuplets() throws Exception {
        NotationElementList list, tupletList;
        Tuplet tuplet;
        
        // nested tuplets...
        list = getTestList2("1/4", "1/12", "1/8", "1/8", "1/12", "1/36", "1/36", "1/36", "1/8", "1/2", "1/5", "1/5", "1/5", "1/5", "1/5", "3/8", "1/7", "7/16");
        list.groupTuplets();
        
        assertEquals(8, list.size());
        assertNotationElementDurationEquals("1/4", list.get(0));
        assertNotationElementDurationEquals("1/8", list.get(2));
        assertNotationElementDurationEquals("1/2", list.get(3));
        assertNotationElementDurationEquals("3/8", list.get(5));
        assertNotationElementDurationEquals("1/7", list.get(6));
        assertNotationElementDurationEquals("7/16", list.get(7));
        
        assertTrue(list.get(1) instanceof Tuplet);
        tuplet = (Tuplet) list.get(1);
        assertEquals(new Fraction(2, 3), tuplet.getTupletMultiplier());
        tupletList = (tuplet).getNotes();                            
        assertEquals(5, tupletList.size());
        assertNotationElementDurationEquals("1/8", tupletList.get(0));
        assertNotationElementDurationEquals("3/16", tupletList.get(1));
        assertNotationElementDurationEquals("3/16", tupletList.get(2));
        assertNotationElementDurationEquals("1/8", tupletList.get(3));
        
        assertTrue(tupletList.get(4) instanceof Tuplet);
        tuplet = (Tuplet) tupletList.get(4);
        assertEquals(new Fraction(2, 3), tuplet.getTupletMultiplier());
        tupletList = (tuplet).getNotes();                            
        assertEquals(3, tupletList.size());
        assertNotationElementDurationEquals("1/16", tupletList.get(0));
        assertNotationElementDurationEquals("1/16", tupletList.get(1));
        assertNotationElementDurationEquals("1/16", tupletList.get(2));
        
        assertTrue(list.get(4) instanceof Tuplet);
        tuplet = (Tuplet) list.get(4);
        assertEquals(new Fraction(4, 5), tuplet.getTupletMultiplier());
        tupletList = (tuplet).getNotes();                            
        assertEquals(5, tupletList.size());
        assertNotationElementDurationEquals("1/4", tupletList.get(0));
        assertNotationElementDurationEquals("1/4", tupletList.get(1));
        assertNotationElementDurationEquals("1/4", tupletList.get(2));
        assertNotationElementDurationEquals("1/4", tupletList.get(3));
        assertNotationElementDurationEquals("1/4", tupletList.get(4));
    }
    
    public void assertNotationElementDurationEquals(String duration, NotationElement element) {
        assertTrue(element instanceof NotationNote);
        NotationNote note = (NotationNote) element;
        assertEquals(new Fraction(duration), note.getDuration());
    }
    
    @Test
    public void getLargestDurationDenominator() throws Exception {
        testGetLargestDurationDenominator(32, "1/16", "3/32", "7/9");
    }
    
    private static void testGetLargestDurationDenominator(long expected, String ... durations) throws Exception {
        NotationElementList list = getTestList(durations);
        assertEquals(expected, list.getLargestDurationDenominator());
    }
    
    @Test
    public void scaleDurations() throws Exception {
        NotationElementList list = getTestList2("1/4", "3/8", "5/9");
        list.scaleDurations(4);
        assertElementListDurations(list, "1/1", "3/2", "20/9");
    }
    
    private static void assertElementListDurations(NotationElementList list, String ... expectedDurations) throws Exception {
        assertEquals(list.size(), expectedDurations.length);                
        for (int i = 0; i < list.size(); i++) {
            NotationNote actualNote = (NotationNote) list.get(i);
            Fraction expectedDuration = new Fraction(expectedDurations[i]);
            assertEquals(expectedDuration, actualNote.getDuration());
        }                
    }
    
    @Test
    public void getNotationNotes() {
        NotationNote n1 = NotationNoteTest.instantiateTestNote("1/4");
        NotationNote n2 = NotationNoteTest.instantiateTestNote("3/8");
        NotationNote n3 = NotationNoteTest.instantiateTestNote("1/2");
        
        NotationElementList list1 = new NotationElementList();
        NotationElementList list2 = new NotationElementList();
        
        list1.add(n1);
        list1.add(n2);
        list2.add(n3);
        list1.add(list2);
        
        List<NotationNote> notationNotes1 = list1.getNotationNotes();
        List<NotationNote> notationNotes2 = list2.getNotationNotes();
        
        assertEquals(3, notationNotes1.size());
        assertTrue(n1 == notationNotes1.get(0));
        assertTrue(n2 == notationNotes1.get(1));
        assertTrue(n3 == notationNotes1.get(2));
        
        assertEquals(1, notationNotes2.size());
        assertTrue(n3 == notationNotes2.get(0));
    }
}