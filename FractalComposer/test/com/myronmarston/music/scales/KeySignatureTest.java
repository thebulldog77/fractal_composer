package com.myronmarston.music.scales;

import com.myronmarston.music.NoteName;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author Myron
 */
public class KeySignatureTest {

    public KeySignatureTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Test
    public void getKeySignatureMidiEvent() throws InvalidMidiDataException {
        KeySignature ks = new KeySignature(4, KeySignature.MajorOrMinor.Major);
        assertKeySignatureEventEqual(ks.getKeySignatureMidiEvent(), (byte) 4, (byte) 0);
        
        ks.setNumberOfFlatsOrSharps(-2);
        assertKeySignatureEventEqual(ks.getKeySignatureMidiEvent(), (byte) -2, (byte) 0);
        
        ks.setMajorOrMinor(KeySignature.MajorOrMinor.Minor);
        assertKeySignatureEventEqual(ks.getKeySignatureMidiEvent(), (byte) -2, (byte) 1);
    }
    
    @Test(expected=InvalidKeySignatureException.class)
    public void majorScaleInvalidKeySignature() throws InvalidKeySignatureException {
        MajorScale s = new MajorScale(NoteName.A_SHARP);
    }
    
    @Test(expected=InvalidKeySignatureException.class)
    public void minorScaleInvalidKeySignature() throws InvalidKeySignatureException {
        MinorScale s = new MinorScale(NoteName.G_FLAT);
    }
    
    @Test
    public void majorScaleGetKeySignature() throws InvalidMidiDataException, InvalidKeySignatureException {
        MajorScale s = new MajorScale(NoteName.D);
        assertKeySignatureEventEqual(s.getKeySignature().getKeySignatureMidiEvent(), (byte) 2, (byte) 0);
        
        s.setKeyName(NoteName.A_FLAT);
        assertKeySignatureEventEqual(s.getKeySignature().getKeySignatureMidiEvent(), (byte) -4, (byte) 0);
    }
    
    @Test
    public void minorScaleGetKeySignature() throws InvalidMidiDataException, InvalidKeySignatureException {
        MinorScale s = new MinorScale(NoteName.D);
        assertKeySignatureEventEqual(s.getKeySignature().getKeySignatureMidiEvent(), (byte) -1, (byte) 1);
        
        s.setKeyName(NoteName.G_SHARP);
        assertKeySignatureEventEqual(s.getKeySignature().getKeySignatureMidiEvent(), (byte) 5, (byte) 1);
    }

    static public void assertKeySignatureEventEqual(MidiEvent keySignatureEvent, byte byte1, byte byte2) {
        assertEquals(0L, keySignatureEvent.getTick());
        javax.sound.midi.MidiMessage msg = keySignatureEvent.getMessage();
        assertEquals(5, msg.getLength());
        assertEquals((byte) 0xFF, msg.getMessage()[0]); // 0xFF for a meta message
        assertEquals((byte) 89, msg.getMessage()[1]);   // 89 is key signature type
        assertEquals((byte) 2, msg.getMessage()[2]);    // the size of the rest of the message     
        assertEquals(byte1, msg.getMessage()[3]);        
        assertEquals(byte2, msg.getMessage()[4]);        
    }
}