package com.myronmarston.music.settings;

import com.myronmarston.music.*;
import com.myronmarston.music.scales.*;

import EDU.oswego.cs.dl.util.concurrent.misc.Fraction;

import java.io.File;
import java.io.IOException;
import java.util.ConcurrentModificationException;

import javax.sound.midi.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Myron
 */
public class SettingsTest {

    public SettingsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void voiceSectionMapKey_hashCode_equals_Test() {
        FractalPiece fp = new FractalPiece();
        Section s1 = fp.createSection();
        Section s2 = fp.createSection();
        Voice v1 = fp.createVoice();
        Voice v2 = fp.createVoice();
        
        VoiceSectionHashMapKey vsmp1 = new VoiceSectionHashMapKey(v1, s1);
        VoiceSectionHashMapKey vsmp2 = new VoiceSectionHashMapKey(v1, s2);        
        assertFalse(vsmp1.equals(vsmp2));
        assertFalse(vsmp1.hashCode() == vsmp2.hashCode());
                
        VoiceSectionHashMapKey vsmp3 = new VoiceSectionHashMapKey(v1, s1);
        assertEquals(vsmp1, vsmp3);
        assertEquals(vsmp1.hashCode(), vsmp3.hashCode());                
    }     
    
    @Test
    public void creatingOrRemovingVoicesOrSettingsManagesVoiceSections() {
        FractalPiece fp = new FractalPiece();
        Voice v1 = fp.createVoice();
        assertEquals(0, v1.getVoiceSections().size());
        
        Section s1 = fp.createSection();
        assertEquals(1, v1.getVoiceSections().size());
        assertEquals(1, s1.getVoiceSections().size());
        VoiceSection vs1 = v1.getVoiceSections().get(0);
        assertNotNull(vs1);
        assertEquals(vs1, s1.getVoiceSections().get(0));
        
        Voice v2 = fp.createVoice();
        assertEquals(1, v1.getVoiceSections().size());
        assertEquals(1, v2.getVoiceSections().size());
        assertEquals(2, s1.getVoiceSections().size());
        VoiceSection vs2 = v2.getVoiceSections().get(0);
        assertNotNull(vs2);
        assertNotSame(vs2, vs1);
        assertEquals(vs2, s1.getVoiceSections().get(1));
        
        Voice v3 = fp.createVoice();
        assertEquals(1, v1.getVoiceSections().size());
        assertEquals(1, v2.getVoiceSections().size());
        assertEquals(1, v3.getVoiceSections().size());
        assertEquals(3, s1.getVoiceSections().size());
        VoiceSection vs3 = v3.getVoiceSections().get(0);
        assertNotNull(vs3);
        assertNotSame(vs3, vs1);
        assertNotSame(vs3, vs2);
        assertEquals(vs3, s1.getVoiceSections().get(2));
        
        Section s2 = fp.createSection();
        assertEquals(2, v1.getVoiceSections().size());
        assertEquals(2, v2.getVoiceSections().size());
        assertEquals(2, v3.getVoiceSections().size());
        assertEquals(3, s1.getVoiceSections().size());
        assertEquals(3, s2.getVoiceSections().size());
        VoiceSection vs4 = v1.getVoiceSections().get(1);
        VoiceSection vs5 = v2.getVoiceSections().get(1);
        VoiceSection vs6 = v3.getVoiceSections().get(1);
        assertNotNull(vs4);
        assertNotNull(vs5);
        assertNotNull(vs6);
        assertEquals(vs4, s2.getVoiceSections().get(0));
        assertEquals(vs5, s2.getVoiceSections().get(1));
        assertEquals(vs6, s2.getVoiceSections().get(2));   
        
        // now let's remove some Voices and Sections...
        fp.getVoices().remove(v1);        
        assertEquals(0, v1.getVoiceSections().size());
        assertEquals(2, s1.getVoiceSections().size());
        assertEquals(2, s2.getVoiceSections().size());
        assertEquals(vs2, s1.getVoiceSections().get(0));
        assertEquals(vs3, s1.getVoiceSections().get(1));
        assertEquals(vs5, s2.getVoiceSections().get(0));
        assertEquals(vs6, s2.getVoiceSections().get(1));
    }
    
    @Test(expected=ConcurrentModificationException.class)
    public void exceptionIfModifyVoiceSectionListWhileIterating() {
        FractalPiece fp = new FractalPiece();
        Voice v1 = fp.createVoice();
        Voice v2 = fp.createVoice();
        Section s1 = fp.createSection();
        Section s2 = fp.createSection();
        
        boolean sectionCreated = false;
        for (VoiceSection vs : v1.getVoiceSections()) {
            // modify the list be creating another section...
            if (!sectionCreated) fp.createSection();
            sectionCreated = true;
        }
    }
    
    @Test(expected=ConcurrentModificationException.class)
    public void exceptionIfModifyVoiceListWhileIterating() {
        FractalPiece fp = new FractalPiece();
        Voice v1 = fp.createVoice();
        Voice v2 = fp.createVoice();
        Section s1 = fp.createSection();
        Section s2 = fp.createSection();
        
        boolean voiceCreated = false;
        for (Voice v : fp.getVoices()) {
            // modify the list be creating another voice...
            if (!voiceCreated) fp.createVoice();
            voiceCreated = true;            
        }
    }
    
    @Test(expected=ConcurrentModificationException.class)
    public void exceptionIfModifySectionListWhileIterating() {
        FractalPiece fp = new FractalPiece();
        Voice v1 = fp.createVoice();
        Voice v2 = fp.createVoice();
        Section s1 = fp.createSection();
        Section s2 = fp.createSection();
        
        boolean sectionCreated = false;
        for (Section s : fp.getSections()) {
            // modify the list be creating another voice...
            if (!sectionCreated) fp.createSection();
            sectionCreated = true;            
        }
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void addVoiceSectionThrowsException() {
        FractalPiece fp = new FractalPiece();
        Voice v1 = fp.createVoice();
        Voice v2 = fp.createVoice();
        Section s1 = fp.createSection();
        Section s2 = fp.createSection();
        
        v2.getVoiceSections().add(v1.getVoiceSections().get(0));
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void removeVoiceSectionThrowsException() {
        FractalPiece fp = new FractalPiece();
        Voice v1 = fp.createVoice();
        Voice v2 = fp.createVoice();
        Section s1 = fp.createSection();
        Section s2 = fp.createSection();
        
        v2.getVoiceSections().remove(0);
    }
    
    @Test
    public void generateVoiceGerm() {
        FractalPiece fp = new FractalPiece();
        fp.getGerm().add(new Note(0, 4, 0, new Fraction(1, 1), 96));
        fp.getGerm().add(new Note(1, 4, 0, new Fraction(1, 2), 64));
        fp.getGerm().add(new Note(2, 4, 0, new Fraction(1, 2), 64));
        fp.getGerm().add(new Note(0, 4, 0, new Fraction(1, 1), 96));
        
        Voice v1 = fp.createVoice();
        v1.setOctaveAdjustment(2);
        v1.setSpeedScaleFactor(new Fraction(4, 1));
        
        NoteList expected = new NoteList();
        expected.add(new Note(0, 6, 0, new Fraction(1, 4), 96));
        expected.add(new Note(1, 6, 0, new Fraction(1, 8), 64));
        expected.add(new Note(2, 6, 0, new Fraction(1, 8), 64));
        expected.add(new Note(0, 6, 0, new Fraction(1, 4), 96));
        assertNoteListsEqual(expected, v1.getModifiedGerm());
        
        v1.setSpeedScaleFactor(new Fraction(1, 2));
        expected.clear();
        expected.add(new Note(0, 6, 0, new Fraction(2, 1), 96));
        expected.add(new Note(1, 6, 0, new Fraction(1, 1), 64));
        expected.add(new Note(2, 6, 0, new Fraction(1, 1), 64));
        expected.add(new Note(0, 6, 0, new Fraction(2, 1), 96));
        assertNoteListsEqual(expected, v1.getModifiedGerm());
        
        v1.setOctaveAdjustment(-1);
        expected.clear();
        expected.add(new Note(0, 3, 0, new Fraction(2, 1), 96));
        expected.add(new Note(1, 3, 0, new Fraction(1, 1), 64));
        expected.add(new Note(2, 3, 0, new Fraction(1, 1), 64));
        expected.add(new Note(0, 3, 0, new Fraction(2, 1), 96));
        assertNoteListsEqual(expected, v1.getModifiedGerm());
    }
    
    @Test
    public void voiceSectionGetVoiceSectionResult() {
        FractalPiece fp = new FractalPiece();
        fp.getGerm().add(new Note(0, 4, 0, new Fraction(1, 1), 96));
        fp.getGerm().add(new Note(1, 4, 0, new Fraction(1, 2), 64));
        fp.getGerm().add(new Note(2, 4, 0, new Fraction(1, 2), 64));
        fp.getGerm().add(new Note(0, 4, 0, new Fraction(1, 1), 96));
        
        Voice v1 = fp.createVoice();
        v1.setOctaveAdjustment(2);
        v1.setSpeedScaleFactor(new Fraction(4, 1));
        
        Section s1 = fp.createSection();
        VoiceSection vs1 = v1.getVoiceSections().get(0);
        
        NoteList expected = new NoteList();
        expected.add(new Note(0, 6, 0, new Fraction(1, 4), 96));
        expected.add(new Note(1, 6, 0, new Fraction(1, 8), 64));
        expected.add(new Note(2, 6, 0, new Fraction(1, 8), 64));
        expected.add(new Note(0, 6, 0, new Fraction(1, 4), 96));                
        assertNoteListsEqual(expected, vs1.getVoiceSectionResult());
                
        expected.clear();
        vs1.setApplyInversion(true);
        vs1.setApplyRetrograde(true);
        vs1.getSelfSimilaritySettings().setApplyToPitch(true);
        vs1.getSelfSimilaritySettings().setApplyToRhythm(true);
        vs1.getSelfSimilaritySettings().setApplyToVolume(true);
        
        expected.add(new Note(0, 6, 0, new Fraction(1, 4), 96));
        expected.add(new Note(-2, 6, 0, new Fraction(1, 8), 64));
        expected.add(new Note(-1, 6, 0, new Fraction(1, 8), 64));
        expected.add(new Note(0, 6, 0, new Fraction(1, 4), 96));
        
        expected.add(new Note(-2, 6, 0, new Fraction(1, 8), 64));
        expected.add(new Note(-4, 6, 0, new Fraction(1, 16), 43));
        expected.add(new Note(-3, 6, 0, new Fraction(1, 16), 43));
        expected.add(new Note(-2, 6, 0, new Fraction(1, 8), 64));
        
        expected.add(new Note(-1, 6, 0, new Fraction(1, 8), 64));
        expected.add(new Note(-3, 6, 0, new Fraction(1, 16), 43));
        expected.add(new Note(-2, 6, 0, new Fraction(1, 16), 43));
        expected.add(new Note(-1, 6, 0, new Fraction(1, 8), 64));
        
        expected.add(new Note(0, 6, 0, new Fraction(1, 4), 96));
        expected.add(new Note(-2, 6, 0, new Fraction(1, 8), 64));
        expected.add(new Note(-1, 6, 0, new Fraction(1, 8), 64));
        expected.add(new Note(0, 6, 0, new Fraction(1, 4), 96));
        assertNoteListsEqual(expected, vs1.getVoiceSectionResult());
        
        expected.clear();
        vs1.getSelfSimilaritySettings().setApplyToVolume(false);
        expected.add(new Note(0, 6, 0, new Fraction(1, 4), 96));
        expected.add(new Note(-2, 6, 0, new Fraction(1, 8), 64));
        expected.add(new Note(-1, 6, 0, new Fraction(1, 8), 64));
        expected.add(new Note(0, 6, 0, new Fraction(1, 4), 96));
        
        expected.add(new Note(-2, 6, 0, new Fraction(1, 8), 96));
        expected.add(new Note(-4, 6, 0, new Fraction(1, 16), 64));
        expected.add(new Note(-3, 6, 0, new Fraction(1, 16), 64));
        expected.add(new Note(-2, 6, 0, new Fraction(1, 8), 96));
        
        expected.add(new Note(-1, 6, 0, new Fraction(1, 8), 96));
        expected.add(new Note(-3, 6, 0, new Fraction(1, 16), 64));
        expected.add(new Note(-2, 6, 0, new Fraction(1, 16), 64));
        expected.add(new Note(-1, 6, 0, new Fraction(1, 8), 96));
        
        expected.add(new Note(0, 6, 0, new Fraction(1, 4), 96));
        expected.add(new Note(-2, 6, 0, new Fraction(1, 8), 64));
        expected.add(new Note(-1, 6, 0, new Fraction(1, 8), 64));
        expected.add(new Note(0, 6, 0, new Fraction(1, 4), 96));
        assertNoteListsEqual(expected, vs1.getVoiceSectionResult());
        
        vs1.setRest(true);
        expected.clear();
        expected.add(new Note(0, 0, 0, new Fraction(3, 4), 0));
        assertNoteListsEqual(expected, vs1.getVoiceSectionResult());
    }
    
    @Test
    public void getSectionDuration() {
        FractalPiece fp = new FractalPiece();
        fp.getGerm().add(new Note(0, 4, 0, new Fraction(1, 1), 96));
        fp.getGerm().add(new Note(1, 4, 0, new Fraction(1, 2), 64));
        fp.getGerm().add(new Note(2, 4, 0, new Fraction(1, 2), 64));
        fp.getGerm().add(new Note(0, 4, 0, new Fraction(1, 1), 96));
        
        Voice v1 = fp.createVoice();
        v1.setOctaveAdjustment(1);
        v1.setSpeedScaleFactor(new Fraction(2, 1));
        
        Voice v2 = fp.createVoice();        
        
        Voice v3 = fp.createVoice();
        v3.setOctaveAdjustment(-1);
        v3.setSpeedScaleFactor(new Fraction(1, 2));        
        
        Section s1 = fp.createSection();
        s1.getVoiceSections().get(2).setRest(true);
        
        assertEquals(new Fraction(6, 1), s1.getDuration());
    }
    
    @Test(expected=IllegalArgumentException.class)    
    public void getLengthenedVoiceSectionResultErrorIfLengthTooShort() {
        FractalPiece fp = new FractalPiece();
        fp.getGerm().add(new Note(0, 4, 0, new Fraction(1, 1), 96));
        fp.getGerm().add(new Note(1, 4, 0, new Fraction(1, 2), 64));
        fp.getGerm().add(new Note(2, 4, 0, new Fraction(1, 2), 64));
        fp.getGerm().add(new Note(0, 4, 0, new Fraction(1, 1), 96));
        
        Voice v1 = fp.createVoice();                
        Section s1 = fp.createSection();
        VoiceSection vs1 = v1.getVoiceSections().get(0);
        
        vs1.getLengthenedVoiceSectionResult(new Fraction(5, 2));
    }
    
    @Test
    public void getLengthenedVoiceSectionResult() {
        FractalPiece fp = new FractalPiece();
        fp.getGerm().add(new Note(0, 4, 0, new Fraction(1, 1), 96));
        fp.getGerm().add(new Note(1, 4, 0, new Fraction(1, 2), 64));
        fp.getGerm().add(new Note(2, 4, 0, new Fraction(1, 2), 64));
        fp.getGerm().add(new Note(0, 4, 0, new Fraction(1, 1), 96));
        
        Voice v1 = fp.createVoice();                
        Section s1 = fp.createSection();
        VoiceSection vs1 = v1.getVoiceSections().get(0);
                
        assertEquals(new Fraction(3, 1), vs1.getVoiceSectionResult().getDuration());
        
        NoteList expected = new NoteList();
        expected.addAll(vs1.getVoiceSectionResult());        
        assertNoteListsEqual(expected, vs1.getLengthenedVoiceSectionResult(new Fraction(3, 1)));
        
        expected.add(Note.createRest(new Fraction(5, 2)));
        assertNoteListsEqual(expected, vs1.getLengthenedVoiceSectionResult(new Fraction(11, 2)));
        
        expected.clear();
        expected.addAll(vs1.getVoiceSectionResult());
        expected.addAll(vs1.getVoiceSectionResult());
        assertNoteListsEqual(expected, vs1.getLengthenedVoiceSectionResult(new Fraction(6, 1)));
        
        expected.add(Note.createRest(new Fraction(2, 1)));
        assertNoteListsEqual(expected, vs1.getLengthenedVoiceSectionResult(new Fraction(8, 1)));
        
        expected.clear();
        expected.addAll(vs1.getVoiceSectionResult());
        expected.addAll(vs1.getVoiceSectionResult());
        expected.addAll(vs1.getVoiceSectionResult());
        expected.addAll(vs1.getVoiceSectionResult());
        expected.add(Note.createRest(new Fraction(1, 2)));
        assertNoteListsEqual(expected, vs1.getLengthenedVoiceSectionResult(new Fraction(25, 2)));        
    }
    
    @Test
    public void getEntireVoiceResult() {
        FractalPiece fp = new FractalPiece();
        fp.getGerm().add(new Note(0, 4, 0, new Fraction(1, 1), 96));
        fp.getGerm().add(new Note(1, 4, 0, new Fraction(1, 2), 64));
        fp.getGerm().add(new Note(2, 4, 0, new Fraction(1, 2), 64));
        fp.getGerm().add(new Note(0, 4, 0, new Fraction(1, 1), 96));
        
        Voice v1 = fp.createVoice();                
        Voice v2 = fp.createVoice();
        Section s1 = fp.createSection();
        Section s2 = fp.createSection();
        
        v1.setOctaveAdjustment(1);
        v1.setSpeedScaleFactor(new Fraction(2, 1));
        
        v1.getVoiceSections().get(0).getSelfSimilaritySettings().setApplyToPitch(true);
        v1.getVoiceSections().get(0).getSelfSimilaritySettings().setApplyToRhythm(true);
        v1.getVoiceSections().get(1).setApplyInversion(true);
        v1.getVoiceSections().get(1).getSelfSimilaritySettings().setApplyToPitch(true);
        v1.getVoiceSections().get(1).getSelfSimilaritySettings().setApplyToRhythm(true);
        
        v2.getVoiceSections().get(0).getSelfSimilaritySettings().setApplyToPitch(true);
        v2.getVoiceSections().get(0).getSelfSimilaritySettings().setApplyToRhythm(true);
        v2.getVoiceSections().get(1).setApplyInversion(true);
        v2.getVoiceSections().get(1).getSelfSimilaritySettings().setApplyToPitch(true);
        v2.getVoiceSections().get(1).getSelfSimilaritySettings().setApplyToRhythm(true);
        
        NoteList expected = new NoteList();
        expected.add(new Note(0, 5, 0, new Fraction(1, 2), 96));
        expected.add(new Note(1, 5, 0, new Fraction(1, 4), 64));
        expected.add(new Note(2, 5, 0, new Fraction(1, 4), 64));
        expected.add(new Note(0, 5, 0, new Fraction(1, 2), 96));
        
        expected.add(new Note(1, 5, 0, new Fraction(1, 4), 96));
        expected.add(new Note(2, 5, 0, new Fraction(1, 8), 64));
        expected.add(new Note(3, 5, 0, new Fraction(1, 8), 64));
        expected.add(new Note(1, 5, 0, new Fraction(1, 4), 96));
        
        expected.add(new Note(2, 5, 0, new Fraction(1, 4), 96));
        expected.add(new Note(3, 5, 0, new Fraction(1, 8), 64));
        expected.add(new Note(4, 5, 0, new Fraction(1, 8), 64));
        expected.add(new Note(2, 5, 0, new Fraction(1, 4), 96));
        
        expected.add(new Note(0, 5, 0, new Fraction(1, 2), 96));
        expected.add(new Note(1, 5, 0, new Fraction(1, 4), 64));
        expected.add(new Note(2, 5, 0, new Fraction(1, 4), 64));
        expected.add(new Note(0, 5, 0, new Fraction(1, 2), 96));
        
        // repeat that whole thing again since this voice is twice as fast
        expected.add(new Note(0, 5, 0, new Fraction(1, 2), 96));
        expected.add(new Note(1, 5, 0, new Fraction(1, 4), 64));
        expected.add(new Note(2, 5, 0, new Fraction(1, 4), 64));
        expected.add(new Note(0, 5, 0, new Fraction(1, 2), 96));
        
        expected.add(new Note(1, 5, 0, new Fraction(1, 4), 96));
        expected.add(new Note(2, 5, 0, new Fraction(1, 8), 64));
        expected.add(new Note(3, 5, 0, new Fraction(1, 8), 64));
        expected.add(new Note(1, 5, 0, new Fraction(1, 4), 96));
        
        expected.add(new Note(2, 5, 0, new Fraction(1, 4), 96));
        expected.add(new Note(3, 5, 0, new Fraction(1, 8), 64));
        expected.add(new Note(4, 5, 0, new Fraction(1, 8), 64));
        expected.add(new Note(2, 5, 0, new Fraction(1, 4), 96));
        
        expected.add(new Note(0, 5, 0, new Fraction(1, 2), 96));
        expected.add(new Note(1, 5, 0, new Fraction(1, 4), 64));
        expected.add(new Note(2, 5, 0, new Fraction(1, 4), 64));
        expected.add(new Note(0, 5, 0, new Fraction(1, 2), 96));
        
        // now do the inversion...
        expected.add(new Note(0, 5, 0, new Fraction(1, 2), 96));
        expected.add(new Note(-1, 5, 0, new Fraction(1, 4), 64));
        expected.add(new Note(-2, 5, 0, new Fraction(1, 4), 64));
        expected.add(new Note(0, 5, 0, new Fraction(1, 2), 96));
        
        expected.add(new Note(-1, 5, 0, new Fraction(1, 4), 96));
        expected.add(new Note(-2, 5, 0, new Fraction(1, 8), 64));
        expected.add(new Note(-3, 5, 0, new Fraction(1, 8), 64));
        expected.add(new Note(-1, 5, 0, new Fraction(1, 4), 96));
        
        expected.add(new Note(-2, 5, 0, new Fraction(1, 4), 96));
        expected.add(new Note(-3, 5, 0, new Fraction(1, 8), 64));
        expected.add(new Note(-4, 5, 0, new Fraction(1, 8), 64));
        expected.add(new Note(-2, 5, 0, new Fraction(1, 4), 96));
        
        expected.add(new Note(0, 5, 0, new Fraction(1, 2), 96));
        expected.add(new Note(-1, 5, 0, new Fraction(1, 4), 64));
        expected.add(new Note(-2, 5, 0, new Fraction(1, 4), 64));
        expected.add(new Note(0, 5, 0, new Fraction(1, 2), 96));
        
        // and repeat the inversion once more...
        expected.add(new Note(0, 5, 0, new Fraction(1, 2), 96));
        expected.add(new Note(-1, 5, 0, new Fraction(1, 4), 64));
        expected.add(new Note(-2, 5, 0, new Fraction(1, 4), 64));
        expected.add(new Note(0, 5, 0, new Fraction(1, 2), 96));
        
        expected.add(new Note(-1, 5, 0, new Fraction(1, 4), 96));
        expected.add(new Note(-2, 5, 0, new Fraction(1, 8), 64));
        expected.add(new Note(-3, 5, 0, new Fraction(1, 8), 64));
        expected.add(new Note(-1, 5, 0, new Fraction(1, 4), 96));
        
        expected.add(new Note(-2, 5, 0, new Fraction(1, 4), 96));
        expected.add(new Note(-3, 5, 0, new Fraction(1, 8), 64));
        expected.add(new Note(-4, 5, 0, new Fraction(1, 8), 64));
        expected.add(new Note(-2, 5, 0, new Fraction(1, 4), 96));
        
        expected.add(new Note(0, 5, 0, new Fraction(1, 2), 96));
        expected.add(new Note(-1, 5, 0, new Fraction(1, 4), 64));
        expected.add(new Note(-2, 5, 0, new Fraction(1, 4), 64));
        expected.add(new Note(0, 5, 0, new Fraction(1, 2), 96));
        
        assertNoteListsEqual(expected, v1.getEntireVoice());
    }
    
    @Test
    public void sectionSetApplyInversionRetrogradeOnAllVoices() {
        FractalPiece fp = new FractalPiece();
        Voice v1 = fp.createVoice();                
        Voice v2 = fp.createVoice();
        Voice v3 = fp.createVoice();
        Section s1 = fp.createSection();
        VoiceSection vs1 = v1.getVoiceSections().get(0);
        VoiceSection vs2 = v2.getVoiceSections().get(0);
        VoiceSection vs3 = v3.getVoiceSections().get(0);
        
        s1.setApplyInversionOnAllVoiceSections(true);
        assertEquals(true, vs1.getApplyInversion());
        assertEquals(true, vs2.getApplyInversion());
        assertEquals(true, vs3.getApplyInversion());
        
        s1.setApplyInversionOnAllVoiceSections(false);
        assertEquals(false, vs1.getApplyInversion());
        assertEquals(false, vs2.getApplyInversion());
        assertEquals(false, vs3.getApplyInversion());
        
        s1.setApplyInversionOnAllVoiceSections(true);
        assertEquals(true, vs1.getApplyInversion());
        assertEquals(true, vs2.getApplyInversion());
        assertEquals(true, vs3.getApplyInversion());
        
        s1.setApplyRetrogradeOnAllVoiceSections(true);
        assertEquals(true, vs1.getApplyRetrograde());
        assertEquals(true, vs2.getApplyRetrograde());
        assertEquals(true, vs3.getApplyRetrograde());
        
        s1.setApplyRetrogradeOnAllVoiceSections(false);
        assertEquals(false, vs1.getApplyRetrograde());
        assertEquals(false, vs2.getApplyRetrograde());
        assertEquals(false, vs3.getApplyRetrograde());
        
        s1.setApplyRetrogradeOnAllVoiceSections(true);
        assertEquals(true, vs1.getApplyRetrograde());
        assertEquals(true, vs2.getApplyRetrograde());
        assertEquals(true, vs3.getApplyRetrograde());
    }
    
    @Test
    public void setSelfSimilaritySettingsOnAllVoices() {
        FractalPiece fp = new FractalPiece();
        Voice v1 = fp.createVoice();                
        Voice v2 = fp.createVoice();
        Voice v3 = fp.createVoice();
        Section s1 = fp.createSection();
        VoiceSection vs1 = v1.getVoiceSections().get(0);
        VoiceSection vs2 = v2.getVoiceSections().get(0);
        VoiceSection vs3 = v3.getVoiceSections().get(0);
        
        s1.setApplySelfSimilarityToPitchOnAllVoiceSections(true);
        assertEquals(true, vs1.getSelfSimilaritySettings().getApplyToPitch());
        assertEquals(true, vs2.getSelfSimilaritySettings().getApplyToPitch());
        assertEquals(true, vs3.getSelfSimilaritySettings().getApplyToPitch());
        
        s1.setApplySelfSimilarityToPitchOnAllVoiceSections(false);
        assertEquals(false, vs1.getSelfSimilaritySettings().getApplyToPitch());
        assertEquals(false, vs2.getSelfSimilaritySettings().getApplyToPitch());
        assertEquals(false, vs3.getSelfSimilaritySettings().getApplyToPitch());
        
        s1.setApplySelfSimilarityToPitchOnAllVoiceSections(true);
        assertEquals(true, vs1.getSelfSimilaritySettings().getApplyToPitch());
        assertEquals(true, vs2.getSelfSimilaritySettings().getApplyToPitch());
        assertEquals(true, vs3.getSelfSimilaritySettings().getApplyToPitch());
        
        s1.setApplySelfSimilarityToRhythmOnAllVoiceSections(true);
        assertEquals(true, vs1.getSelfSimilaritySettings().getApplyToRhythm());
        assertEquals(true, vs2.getSelfSimilaritySettings().getApplyToRhythm());
        assertEquals(true, vs3.getSelfSimilaritySettings().getApplyToRhythm());
        
        s1.setApplySelfSimilarityToRhythmOnAllVoiceSections(false);
        assertEquals(false, vs1.getSelfSimilaritySettings().getApplyToRhythm());
        assertEquals(false, vs2.getSelfSimilaritySettings().getApplyToRhythm());
        assertEquals(false, vs3.getSelfSimilaritySettings().getApplyToRhythm());
        
        s1.setApplySelfSimilarityToRhythmOnAllVoiceSections(true);
        assertEquals(true, vs1.getSelfSimilaritySettings().getApplyToRhythm());
        assertEquals(true, vs2.getSelfSimilaritySettings().getApplyToRhythm());
        assertEquals(true, vs3.getSelfSimilaritySettings().getApplyToRhythm());
        
        s1.setApplySelfSimilarityToVolumeOnAllVoiceSections(true);
        assertEquals(true, vs1.getSelfSimilaritySettings().getApplyToVolume());
        assertEquals(true, vs2.getSelfSimilaritySettings().getApplyToVolume());
        assertEquals(true, vs3.getSelfSimilaritySettings().getApplyToVolume());
        
        s1.setApplySelfSimilarityToVolumeOnAllVoiceSections(false);
        assertEquals(false, vs1.getSelfSimilaritySettings().getApplyToVolume());
        assertEquals(false, vs2.getSelfSimilaritySettings().getApplyToVolume());
        assertEquals(false, vs3.getSelfSimilaritySettings().getApplyToVolume());
        
        s1.setApplySelfSimilarityToVolumeOnAllVoiceSections(true);
        assertEquals(true, vs1.getSelfSimilaritySettings().getApplyToVolume());
        assertEquals(true, vs2.getSelfSimilaritySettings().getApplyToVolume());
        assertEquals(true, vs3.getSelfSimilaritySettings().getApplyToVolume());
        
        vs1.getSelfSimilaritySettings().setApplyToPitch(true);
        vs2.getSelfSimilaritySettings().setApplyToPitch(false);
        vs3.getSelfSimilaritySettings().setApplyToPitch(true);
        
        s1.setSelfSimilaritySettingsOnAllVoiceSections(null, false, true);
        assertEquals(true, vs1.getSelfSimilaritySettings().getApplyToPitch());
        assertEquals(false, vs2.getSelfSimilaritySettings().getApplyToPitch());
        assertEquals(true, vs3.getSelfSimilaritySettings().getApplyToPitch());
        
        assertEquals(false, vs1.getSelfSimilaritySettings().getApplyToRhythm());
        assertEquals(false, vs2.getSelfSimilaritySettings().getApplyToRhythm());
        assertEquals(false, vs3.getSelfSimilaritySettings().getApplyToRhythm());
        
        assertEquals(true, vs1.getSelfSimilaritySettings().getApplyToVolume());
        assertEquals(true, vs2.getSelfSimilaritySettings().getApplyToVolume());
        assertEquals(true, vs3.getSelfSimilaritySettings().getApplyToVolume());
    }
    
    @Test
    public void createDefaultVoices() {
        FractalPiece fp = new FractalPiece();
        fp.createDefaultVoices();
        
        assertEquals(fp.getVoices().get(0).getOctaveAdjustment(), 1);
        assertEquals(fp.getVoices().get(1).getOctaveAdjustment(), 0);
        assertEquals(fp.getVoices().get(2).getOctaveAdjustment(), -1);
        
        assertEquals(fp.getVoices().get(0).getSpeedScaleFactor(), new Fraction(2, 1));
        assertEquals(fp.getVoices().get(1).getSpeedScaleFactor(), new Fraction(1, 1));
        assertEquals(fp.getVoices().get(2).getSpeedScaleFactor(), new Fraction(1, 2));
        
        Voice v = fp.createVoice(1);
        
        // calling the method again should leave the existing voices alone.
        fp.createDefaultVoices();
        
        assertEquals(fp.getVoices().get(0).getOctaveAdjustment(), 1);
        assertEquals(fp.getVoices().get(1).getOctaveAdjustment(), 0);
        assertEquals(fp.getVoices().get(2).getOctaveAdjustment(), 0);
        assertEquals(fp.getVoices().get(3).getOctaveAdjustment(), -1);
        
        assertEquals(fp.getVoices().get(0).getSpeedScaleFactor(), new Fraction(2, 1));
        assertEquals(fp.getVoices().get(1).getSpeedScaleFactor(), new Fraction(1, 1));
        assertEquals(fp.getVoices().get(2).getSpeedScaleFactor(), new Fraction(1, 1));
        assertEquals(fp.getVoices().get(3).getSpeedScaleFactor(), new Fraction(1, 2));        
    }
    
    @Test
    public void createDefaultSections() {
        FractalPiece fp = new FractalPiece();
        
        Voice v1 = fp.createVoice();
        Voice v2 = fp.createVoice();
        Voice v3 = fp.createVoice();
        
        v1.setSpeedScaleFactor(new Fraction(2, 1));
        v3.setSpeedScaleFactor(new Fraction(1, 2));
        
        fp.createDefaultSections();
        assertEquals(4, fp.getSections().size());
                
        assertVoiceSectionEqual(v1.getVoiceSections().get(0), false, false, false, true, true, true);
        assertVoiceSectionEqual(v2.getVoiceSections().get(0), false, false, false, false, false, false);
        assertVoiceSectionEqual(v3.getVoiceSections().get(0), false, false, false, false, false, false);
        
        assertVoiceSectionEqual(v1.getVoiceSections().get(1), true, false, false, true, true, true);
        assertVoiceSectionEqual(v2.getVoiceSections().get(1), true, false, false, false, false, false);
        assertVoiceSectionEqual(v3.getVoiceSections().get(1), true, false, false, false, false, false);
        
        assertVoiceSectionEqual(v1.getVoiceSections().get(2), true, true, false, true, true, true);
        assertVoiceSectionEqual(v2.getVoiceSections().get(2), true, true, false, false, false, false);
        assertVoiceSectionEqual(v3.getVoiceSections().get(2), true, true, false, false, false, false);
        
        assertVoiceSectionEqual(v1.getVoiceSections().get(3), false, true, false, true, true, true);
        assertVoiceSectionEqual(v2.getVoiceSections().get(3), false, true, false, false, false, false);
        assertVoiceSectionEqual(v3.getVoiceSections().get(3), false, true, false, false, false, false);               
        
        // add another section, so that we can test that it gets stomped when we call createDefaultSections()
        fp.createSection();
        
        // modify the speed scale factor so that a different voice gets the self-similarity...
        v2.setSpeedScaleFactor(new Fraction(4, 1));
        fp.createDefaultSections();
        assertEquals(4, fp.getSections().size());
                
        assertVoiceSectionEqual(v1.getVoiceSections().get(0), false, false, false, false, false, false);
        assertVoiceSectionEqual(v2.getVoiceSections().get(0), false, false, false, true, true, true);
        assertVoiceSectionEqual(v3.getVoiceSections().get(0), false, false, false, false, false, false);
        
        assertVoiceSectionEqual(v1.getVoiceSections().get(1), true, false, false, false, false, false);
        assertVoiceSectionEqual(v2.getVoiceSections().get(1), true, false, false, true, true, true);
        assertVoiceSectionEqual(v3.getVoiceSections().get(1), true, false, false, false, false, false);
        
        assertVoiceSectionEqual(v1.getVoiceSections().get(2), true, true, false, false, false, false);
        assertVoiceSectionEqual(v2.getVoiceSections().get(2), true, true, false, true, true, true);
        assertVoiceSectionEqual(v3.getVoiceSections().get(2), true, true, false, false, false, false);
        
        assertVoiceSectionEqual(v1.getVoiceSections().get(3), false, true, false, false, false, false);
        assertVoiceSectionEqual(v2.getVoiceSections().get(3), false, true, false, true, true, true);
        assertVoiceSectionEqual(v3.getVoiceSections().get(3), false, true, false, false, false, false);        
    }
    
    @Test
    public void introAndOutroTest() throws InvalidMidiDataException, InvalidKeySignatureException {
        FractalPiece fp = new FractalPiece();

        Voice v1 = fp.createVoice();
        Voice v2 = fp.createVoice();
        Voice v3 = fp.createVoice();
        
        v1.setSpeedScaleFactor(new Fraction(2, 1));
        v3.setSpeedScaleFactor(new Fraction(1, 2));
        
        fp.createIntroSections();
        assertVoiceSectionEqual(v1.getVoiceSections().get(0), false, false, true, false, false, false);
        assertVoiceSectionEqual(v1.getVoiceSections().get(1), false, false, true, false, false, false);
        assertVoiceSectionEqual(v1.getVoiceSections().get(2), false, false, false, false, false, false);
        
        assertVoiceSectionEqual(v2.getVoiceSections().get(0), false, false, true, false, false, false);
        assertVoiceSectionEqual(v2.getVoiceSections().get(1), false, false, false, false, false, false);
        assertVoiceSectionEqual(v2.getVoiceSections().get(2), false, false, false, false, false, false);
        
        assertVoiceSectionEqual(v3.getVoiceSections().get(0), false, false, false, false, false, false);
        assertVoiceSectionEqual(v3.getVoiceSections().get(1), false, false, false, false, false, false);
        assertVoiceSectionEqual(v3.getVoiceSections().get(2), false, false, false, false, false, false);
        fp.clearTempIntroOutroSections();
        assertEquals(0, fp.getSections().size());
        
        // change the speed factors; this should change which voices gets rests when...
        v2.setSpeedScaleFactor(new Fraction(1, 4));
        
        fp.createIntroSections();
        assertVoiceSectionEqual(v1.getVoiceSections().get(0), false, false, true, false, false, false);
        assertVoiceSectionEqual(v1.getVoiceSections().get(1), false, false, true, false, false, false);
        assertVoiceSectionEqual(v1.getVoiceSections().get(2), false, false, false, false, false, false);
        
        assertVoiceSectionEqual(v2.getVoiceSections().get(0), false, false, false, false, false, false);
        assertVoiceSectionEqual(v2.getVoiceSections().get(1), false, false, false, false, false, false);
        assertVoiceSectionEqual(v2.getVoiceSections().get(2), false, false, false, false, false, false);
        
        assertVoiceSectionEqual(v3.getVoiceSections().get(0), false, false, true, false, false, false);
        assertVoiceSectionEqual(v3.getVoiceSections().get(1), false, false, false, false, false, false);
        assertVoiceSectionEqual(v3.getVoiceSections().get(2), false, false, false, false, false, false);
        fp.clearTempIntroOutroSections();
        
        fp.createOutroSections();
        assertVoiceSectionEqual(v1.getVoiceSections().get(0), false, false, false, false, false, false);
        assertVoiceSectionEqual(v1.getVoiceSections().get(1), false, false, true, false, false, false);
        assertVoiceSectionEqual(v1.getVoiceSections().get(2), false, false, true, false, false, false);
        
        assertVoiceSectionEqual(v2.getVoiceSections().get(0), false, false, false, false, false, false);
        assertVoiceSectionEqual(v2.getVoiceSections().get(1), false, false, false, false, false, false);
        assertVoiceSectionEqual(v2.getVoiceSections().get(2), false, false, false, false, false, false);
        
        assertVoiceSectionEqual(v3.getVoiceSections().get(0), false, false, false, false, false, false);
        assertVoiceSectionEqual(v3.getVoiceSections().get(1), false, false, false, false, false, false);
        assertVoiceSectionEqual(v3.getVoiceSections().get(2), false, false, true, false, false, false);
        fp.clearTempIntroOutroSections();    
        
        // change the speed scale factor of a voice; this should change where the rests go...
        v1.setSpeedScaleFactor(new Fraction(1, 8));
        
        fp.createOutroSections();
        assertVoiceSectionEqual(v1.getVoiceSections().get(0), false, false, false, false, false, false);
        assertVoiceSectionEqual(v1.getVoiceSections().get(1), false, false, false, false, false, false);
        assertVoiceSectionEqual(v1.getVoiceSections().get(2), false, false, false, false, false, false);
        
        assertVoiceSectionEqual(v2.getVoiceSections().get(0), false, false, false, false, false, false);
        assertVoiceSectionEqual(v2.getVoiceSections().get(1), false, false, false, false, false, false);
        assertVoiceSectionEqual(v2.getVoiceSections().get(2), false, false, true, false, false, false);
        
        assertVoiceSectionEqual(v3.getVoiceSections().get(0), false, false, false, false, false, false);
        assertVoiceSectionEqual(v3.getVoiceSections().get(1), false, false, true, false, false, false);
        assertVoiceSectionEqual(v3.getVoiceSections().get(2), false, false, true, false, false, false);
        fp.clearTempIntroOutroSections();  
    }
    
    @Test
    public void setGerm() throws NoteStringParseException, InvalidKeySignatureException {
        FractalPiece fp = new FractalPiece();
        fp.setScale(new MajorScale(NoteName.G));
        
        NoteList expected = new NoteList();
        expected.add(new Note(0, 4, 0, new Fraction(1, 4), Dynamic.MF.getMidiVolume()));
        expected.add(new Note(1, 4, 0, new Fraction(1, 8), Dynamic.F.getMidiVolume()));
        expected.add(new Note(2, 4, 0, new Fraction(1, 8), Dynamic.F.getMidiVolume()));
        expected.add(new Note(0, 4, 0, new Fraction(1, 4), Dynamic.MF.getMidiVolume()));
        
        fp.setGerm("G4,1/4,MF A4,1/8,F B4,1/8,F G4,1/4,MF");
        assertNoteListsEqual(expected, fp.getGerm());
    }
    
    @Test
    public void createAndSaveMidiFile() throws IOException, InvalidKeySignatureException, NoteStringParseException, InvalidMidiDataException {
        File temp = File.createTempFile("TempMidiFile", ".mid");
        temp.deleteOnExit();
        String fileName = temp.getCanonicalPath();        
                
        FractalPiece fp = new FractalPiece();
        fp.setScale(new MajorScale(NoteName.G));
        fp.setGerm("G4,1/4,MF A4,1/8,F B4,1/8,F G4,1/4,MF");  
        fp.createDefaultSettings();
        fp.createAndSaveMidiFile(fileName);
        
        // this will throw an exception if the midi file was not saved...
        Sequence seq = MidiSystem.getSequence(temp);        
    }
    
    static protected void assertNoteListsEqual(NoteList expected, NoteList actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {            
            assertEquals(expected.get(i), actual.get(i));
        }
    }
    
    static protected void assertVoiceSectionEqual(VoiceSection vs, boolean applyInversion, boolean applyRetrograde, boolean isRest, boolean applySelfSimilarityToPitch, boolean applySelfSimilarityToRhythm, boolean applySelfSimilarityToVolume) {
        assertEquals(applyInversion, vs.getApplyInversion());
        assertEquals(applyRetrograde, vs.getApplyRetrograde());
        assertEquals(isRest, vs.getRest());
        assertEquals(applySelfSimilarityToPitch, vs.getSelfSimilaritySettings().getApplyToPitch());
        assertEquals(applySelfSimilarityToRhythm, vs.getSelfSimilaritySettings().getApplyToRhythm());
        assertEquals(applySelfSimilarityToVolume, vs.getSelfSimilaritySettings().getApplyToVolume());
    }
}