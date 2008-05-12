package com.myronmarston.music.settings;

import com.myronmarston.music.Instrument;
import com.myronmarston.music.NoteList;
import com.myronmarston.music.transformers.OctaveTransformer;
import com.myronmarston.music.transformers.RhythmicDurationTransformer;

import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import org.simpleframework.xml.*;

import com.myronmarston.util.Fraction;
import java.util.*;

/**
 * A FractalPiece is composed of several voices.  A Voice is analogous to 
 * a contrapuntal voice in music theory.  It is composed of a series of 
 * sections, each of which is composed of a series of notes and or/rests, 
 * played consecutively.  One voice will never have multiple notes
 * sounded simultaneously.
 * 
 * @author Myron
 */
@Root
public class Voice extends AbstractVoiceOrSection<Voice, Section> {   
    @Attribute
    private int octaveAdjustment = 0;
    
    @Attribute
    private String instrumentName = Instrument.getDefault().getName();
    
    @Element
    private Fraction speedScaleFactor = new Fraction(1, 1);
    private NoteList modifiedGerm;    
    
    /**
     * Constructor.
     * 
     * @param fractalPiece the FractalPiece this voice is a part of
     * @param uniqueIndex the unique index for this voice
     */
    protected Voice(FractalPiece fractalPiece, int uniqueIndex) {
        super(fractalPiece, uniqueIndex);
    }    
    
    /**
     * Provided for xml deserialization.
     */
    private Voice() {
        this(null, 0);
    }

    /**
     * Gets the name of the instrument used by this voice.
     * 
     * @return the name of the instrument used by this voice.
     */
    public String getInstrumentName() {
        return instrumentName;
    }

    /**
     * Sets the name of the instrument used by this voice.  
     * 
     * @param instrumentName
     * @throws IllegalArgumentException thrown if the given instrument is
     *         unavailable
     */
    public void setInstrumentName(String instrumentName) throws IllegalArgumentException {
        if (!Instrument.AVAILABLE_INSTRUMENTS.contains(instrumentName)) {
            throw new IllegalArgumentException(instrumentName + " is not one of the available instruments.");
        }
        
        this.instrumentName = instrumentName;        
    }        
        
    /**
     * Gets how many octaves to adjust the germ for use by this voice.
     * 
     * @return number of octaves to adjust
     */
    public int getOctaveAdjustment() {
        return octaveAdjustment;
    }
    
    /**
     * Sets how many octaves to adjust the germ for use by this voice.
     * 
     * @param val number of octaves to adjust
     */
    public void setOctaveAdjustment(int val) {
        if (this.getOctaveAdjustment() != val) clearModifiedGerm();
        this.octaveAdjustment = val;
    }

    /**
     * Gets the speed scale factor to apply to the germ for use by this voice.
     * 
     * @return the speed scale factor
     */
    public Fraction getSpeedScaleFactor() {
        return speedScaleFactor;
    }
    
    /**
     * Sets the speed scale factor to apply to the germ for use by this voice.
     * 
     * @param val the speed scale factor
     */
    public void setSpeedScaleFactor(Fraction val) {
        if (this.speedScaleFactor != val) clearModifiedGerm();
        this.speedScaleFactor = val;
    }
    
    /**
     * Gets the modified germ, based on the current settings.
     * 
     * @return the modified germ
     */
    public NoteList getModifiedGerm() {
        if (modifiedGerm == null) this.modifiedGerm = this.generateModifiedGerm();
        this.modifiedGerm.setInstrument(Instrument.getInstrument(this.getInstrumentName()));
        return modifiedGerm;
    }
    
    /**
     * Gets a NoteList containing the notes for all sections of this voice.
     * 
     * @return a NoteList for the entire voice
     */
    public NoteList getEntireVoice() {
        NoteList entireVoice = new NoteList();        
        Fraction sectionDuration;
        
        for (VoiceSection vs : this.getVoiceSections()) {
            sectionDuration = vs.getSection().getDuration();
            
            entireVoice.addAll(vs.getLengthenedVoiceSectionResult(sectionDuration));
        }
        
        entireVoice.setInstrument(Instrument.getInstrument(this.getInstrumentName()));
        return entireVoice;
    }   
    
    /**
     * Constructs a midi sequence using the modified germ and saves it to a midi 
     * file.
     * 
     * @param fileName the filename to use
     * @throws java.io.IOException if there is a problem writing to the file
     * @throws javax.sound.midi.InvalidMidiDataException if there is invalid
     *         midi data
     */
    public void saveModifiedGermToMidiFile(String fileName) throws IOException, InvalidMidiDataException {
        this.getFractalPiece().saveNoteListsAsMidiFile(Arrays.asList(this.getModifiedGerm()), fileName);
    }
    
    /**
     * Constructs a midi sequence using the entire voice and saves it to a midi 
     * file.
     * 
     * @param fileName the filename to use
     * @throws java.io.IOException if there is a problem writing to the file
     * @throws javax.sound.midi.InvalidMidiDataException if there is invalid
     *         midi data
     */
    public void saveEntireVoiceToMidiFile(String fileName) throws IOException, InvalidMidiDataException {
        this.getFractalPiece().saveNoteListsAsMidiFile(Arrays.asList(this.getEntireVoice()), fileName);
    }
    
    /**
     * Sets the modifiedGerm field to null.  Should be called anytime a field
     * that affects the modified germ changes.
     */
    private void clearModifiedGerm() {
        this.modifiedGerm = null;
    }
    
    /**
     * Generates the modified germ based on the current settings.
     * 
     * @return the modified germ.
     */
    private NoteList generateModifiedGerm() {
        OctaveTransformer octaveT = new OctaveTransformer(this.getOctaveAdjustment());
        RhythmicDurationTransformer speedT = new RhythmicDurationTransformer(this.getSpeedScaleFactor());
        
        NoteList temp = octaveT.transform(this.getFractalPiece().getGerm());
        return speedT.transform(temp);        
    }
    
    @Override
    protected List<Voice> getListOfMainType() {
        return this.getFractalPiece().getVoices();
    }
    
    @Override
    protected List<Section> getListOfOtherType() {
        return this.getFractalPiece().getSections();
    }

    @Override
    protected VoiceSectionHashMapKey getHashMapKeyForOtherTypeIndex(int index) {
        Section indexedSection = this.getFractalPiece().getSections().get(index);
        return new VoiceSectionHashMapKey(this, indexedSection);
    }

    @Override
    protected VoiceSection instantiateVoiceSection(Section vOrS) {
        return new VoiceSection(this, vOrS);
    }        
}

