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

package com.myronmarston.music;

import com.myronmarston.music.settings.*;
import com.myronmarston.music.scales.KeySignature;
import com.myronmarston.music.scales.Scale;
import com.myronmarston.util.Fraction;
import com.myronmarston.util.MathHelper;
import java.io.*;
import java.lang.reflect.UndeclaredThrowableException;
import javax.sound.midi.*;
import java.util.*;

/**
 * Class that manages the outputs from FractalComposer, such as Midi files, 
 * PDF files of scores, etc.
 * 
 * @author Myron
 */
public class OutputManager {
    private StringBuilder guidoNotation = new StringBuilder();
    private Sequence sequence;
    private final FractalPiece fractalPiece;
    private final int tempo;
    private Collection<NoteList> noteLists;
    private SheetMusicCreator sheetMusicCreator;
    private AudioFileCreator audioFileCreator;
    private boolean includeTempoOnSheetMusic;
    private boolean includeInstrumentOnSheetMusic;
    private String lastMidiFileName;
    private String lastGuidoFileName;
    private String lastWavFileName;
    private String lastMp3FileName;
    private String lastGifFileName;
    
    private static final int MIDI_FILE_TYPE_FOR_MULTI_TRACK_SEQUENCE = 1;
        
    /**
     * Gets the guido notation representing this piece of music.
     * 
     * @return the guido notation string
     */
    public String getGuidoNotation() {        
        return guidoNotation.toString();
    }

    /**
     * Gets the Midi sequence.
     * 
     * @return the midi sequence
     */
    public Sequence getSequence() {        
        return sequence;
    }
    
    /**
     * Gets the tempo of the music.
     * 
     * @return the tempo, in beats per minute
     */
    public int getTempo() {
        return tempo;
    }

    /**
     * Gets the sheet music creator.
     * 
     * @return the sheet music creator
     */
    protected SheetMusicCreator getSheetMusicCreator() {
        if (sheetMusicCreator == null) sheetMusicCreator = new SheetMusicCreator(this);
        return sheetMusicCreator;
    }

    /**
     * Gets the audio file creator.
     * 
     * @return the audio file creator
     */
    public AudioFileCreator getAudioFileCreator() {
        if (audioFileCreator == null) audioFileCreator = new AudioFileCreator(this);
        return audioFileCreator;
    }        
    
    /**
     * Gets the collection of note lists that was used to generate the output.
     * 
     * @return the collection of note lists
     */
    public Collection<NoteList> getNoteLists() {
        return this.noteLists;
    }

    /**
     * Gets the fractal piece to generate the output.
     * 
     * @return the fractal piece
     */
    public FractalPiece getFractalPiece() {
        return fractalPiece;
    }        
    
    /**
     * Constructor.  This automatically constructs the midi sequence and the
     * guido notation.  All aspects of the guido notation are included.
     * 
     * @param fractalPiece the fractal piece
     * @param noteLists collection of noteLists containing music
     * @throws com.myronmarston.music.GermIsEmptyException if the germ is empty
     */
    public OutputManager(FractalPiece fractalPiece, Collection<NoteList> noteLists) throws GermIsEmptyException {
        this(fractalPiece, noteLists, true, true);
    }

    /**
     * Constructor.  This automatically constructs the midi sequence and the
     * guido notation.
     * 
     * @param fractalPiece the fractal piece 
     * @param noteLists collection of noteLists containing music     
     * @param includeTempoOnSheetMusic whether or not to include a tempo marking
     *        on the produced sheet music
     * @param includeInstrumentOnSheetMusic whether or not to include 
     *        instrument markings on the produced sheet music
     * @throws com.myronmarston.music.GermIsEmptyException if the germ is empty
     */
    public OutputManager(FractalPiece fractalPiece, Collection<NoteList> noteLists, boolean includeTempoOnSheetMusic, boolean includeInstrumentOnSheetMusic) throws GermIsEmptyException {
        this.fractalPiece = fractalPiece;
        this.noteLists = noteLists;
        this.includeTempoOnSheetMusic = includeTempoOnSheetMusic;
        this.includeInstrumentOnSheetMusic = includeInstrumentOnSheetMusic;
        this.tempo = this.fractalPiece.getTempo();
        constructMidiSequence();
    }   
    
    /**
     * Creates the midi sequence and the guido notation
     *      
     * @throws com.myronmarston.music.GermIsEmptyException if the germ is empty
     */           
    private void constructMidiSequence() throws GermIsEmptyException {
        // this is only meant to be called once, to construct the sequence...
        assert this.sequence == null : sequence;
        
        // We can't create any midi sequence if we don't have a germ from which to "grow" our piece...
        if (this.fractalPiece.getGerm() == null || this.fractalPiece.getGerm().size() == 0) throw new GermIsEmptyException();
        
        this.guidoNotation.append("{"); // start of the guido notation...
                
        try {
            this.sequence = new Sequence(Sequence.PPQ, this.getMidiTickResolution());
        } catch (InvalidMidiDataException ex) {
            // our logic should prevent this exception from ever occurring, 
            // so we transform this to an unchecked exception instead of 
            // having to declare it on our method.
            throw new UndeclaredThrowableException(ex, "Error while creating sequence.  This indicates a programming error of some sort.");                
        }  

        // next, use the first track to set key signature, time signature and tempo...
        Track track1 = sequence.createTrack();        
        track1.add(this.fractalPiece.getScale().getKeySignature().getKeySignatureMidiEvent(0));                  
        addSectionKeySigEventsToTrack(track1, sequence.getResolution());
        track1.add(this.fractalPiece.getTimeSignature().getMidiTimeSignatureEvent());
        track1.add(Tempo.getMidiTempoEvent(this.getTempo()));

        // finally, create and fill our midi tracks...
        for (NoteList nl : noteLists) {                       
            this.constructMidiTrack(nl);             
        }
        
        // remove the trailing comma...
        this.guidoNotation.deleteCharAt(this.guidoNotation.length() - 1);        
        this.guidoNotation.append("}"); // end of the guido notation...                
    }
    
    /**
     * Adds key signature events to the given track for each section, as needed.
     * 
     * @param track the track to add the events to 
     * @param sequenceResolution the midi sequence resolution
     */
    private void addSectionKeySigEventsToTrack(Track track, int sequenceResolution) {
        // add key signatures for each section that uses a different one...
        Fraction durationSoFar = new Fraction(0, 1);
        KeySignature lastKeySignature = this.fractalPiece.getScale().getKeySignature();
        KeySignature sectionKeySignature;
        for (Section s : this.fractalPiece.getSections()) { 
            sectionKeySignature = s.getSectionKeySignature();
            if (!lastKeySignature.equals(sectionKeySignature)) {
                Fraction tickCount = durationSoFar.times(sequenceResolution);
        
                // our tick count should be an integral value...
                assert tickCount.denominator() == 1L : tickCount.denominator();
                long tickValue = convertMidiTickUnitFromQuarterNotesToWholeNotes((long) tickCount.asDouble());
                track.add(sectionKeySignature.getKeySignatureMidiEvent(tickValue));
            }
            lastKeySignature = sectionKeySignature;            
            durationSoFar = durationSoFar.plus(s.getDuration());
        }
    }    
    
    /**
     * Constructs a midi track based on the given note list.
     * 
     * @param noteList the note list     
     */
    protected void constructMidiTrack(NoteList noteList) {
        MidiNote thisMidiNote, lastMidiNote = null;
        Note lastNote = null;
        Fraction startTime = new Fraction(0, 1);
        Scale scale = fractalPiece.getScale();        
        
        // get a default instrument if we we're not passed one...
        Instrument instrument = (noteList.getInstrument() == null ? Instrument.DEFAULT : noteList.getInstrument());
        
        this.guidoNotation.append("[ ");
        //If a pageFromat should be specified, put it here, such as \pageFormat<"A4",10pt,10pt,10pt,10pt>
        if (this.includeInstrumentOnSheetMusic) this.guidoNotation.append(instrument.toGuidoString() + " ");
        this.guidoNotation.append(scale.getKeySignature().toGuidoString() + " ");
        this.guidoNotation.append(fractalPiece.getTimeSignature().toGuidoString() + " ");
        if (this.includeTempoOnSheetMusic) this.guidoNotation.append(Tempo.toGuidoString(fractalPiece.getTempo()) + " ");
        
        // make each track be on a different channel, but make sure we don't go over our total number of channels...
        int numTracks = sequence.getTracks().length;
        // The 1st track should be the tempo/key sig/time sig track        
        assert numTracks > 0 : numTracks; 
        int midiChannel = numTracks - 1;
        assert midiChannel < MidiNote.MAX_CHANNEL;
        
        Track track = sequence.createTrack();
        track.add(instrument.getProgramChangeMidiEvent(midiChannel));
        
        // in Midi, the tick resolution is based on quarter notes, but we use whole notes...
        int midiTicksPerWholeNote = convertMidiTickUnitFromQuarterNotesToWholeNotesInt(sequence.getResolution());
        
        for (Note thisNote : noteList.getListWithNormalizedRests()) {
            // TODO: add some stuff to the guidoNotation if the note is the 
            // first or last note of a voice section            
            
            thisMidiNote = thisNote.convertToMidiNote(startTime, midiTicksPerWholeNote, midiChannel, true);                        
            
            if (lastMidiNote != null) {
                assert lastNote != null;
                                
                if (thisMidiNote.getPitch() == lastMidiNote.getPitch() && lastNote.getNormalizedNote().getScaleStep() != thisNote.getNormalizedNote().getScaleStep()) {               
                    // the notes are different scale steps and should have different pitches.
                    // This can happen with notes like B# and C in the key of C.

                    if (lastNote.getChromaticAdjustment() != 0) {
                        lastMidiNote = lastNote.convertToMidiNote(startTime.minus(thisNote.getDuration()), midiTicksPerWholeNote, midiChannel, false);
                    } else if (thisNote.getChromaticAdjustment() != 0) {
                        thisMidiNote = thisNote.convertToMidiNote(startTime, midiTicksPerWholeNote, midiChannel, false);
                    } else {
                        // one of these notes should always have a chromatic 
                        // adjustment--otherwise, how do they have the same pitches
                        // but different scale steps?
                        assert false : "Neither last note '" + lastNote.toString() + "' nor this note '" + thisNote.toString() + "' have a chromatic adjustment.";
                    }
                    
                    assert thisMidiNote.getPitch() != lastMidiNote.getPitch() : "The midi notes have the same pitch and should not: " + thisMidiNote.getPitch();
                }              
                addMidiNoteEventsToTrack(track, lastMidiNote, lastNote);                
            }                                      
            
            //The next note start time will be the end of this note...
            startTime = startTime.plus(thisNote.getDuration());
            
            lastMidiNote = thisMidiNote;
            lastNote = thisNote;
        }           
        addMidiNoteEventsToTrack(track, lastMidiNote, lastNote);
        
        this.guidoNotation.append(" ],");           
    }        
    
    /**
     * Adds the midi note on and note off events to a track.
     * 
     * @param track the track
     * @param midiNote the midi note
     */
    private void addMidiNoteEventsToTrack(Track track, MidiNote midiNote, Note note) {
        try {
            track.add(midiNote.getNoteOnEvent());
            track.add(midiNote.getNoteOffEvent());
        } catch (InvalidMidiDataException ex) {
            // our logic should prevent this exception from ever occurring, 
            // so we transform this to an unchecked exception instead of 
            // having to declare it on our method.
            throw new UndeclaredThrowableException(ex, "MidiNote's note on and note off events could not be created.  This indicates a programming error of some sort.");                
        }        
        
        // add the guido notation...
        guidoNotation.append(" " + note.toGuidoString(midiNote) + " ");
    }

    /**
     * Calculates the optimal midi tick resolution for the given collection of 
     * noteLists, based on the duration of the notes.
     *      
     * @return the midi tick resolution
     */
    protected int getMidiTickResolution() {        
        // next, figure out the resolution of our Midi sequence...
        ArrayList<Long> uniqueDurationDenominators = new ArrayList<Long>();
        for (NoteList nl : noteLists) {
            for (Note n : nl) {
                if (!uniqueDurationDenominators.contains(n.getDuration().denominator())) {
                    uniqueDurationDenominators.add(n.getDuration().denominator());
                }                
            }
        }        
        
        long resolution = MathHelper.leastCommonMultiple(uniqueDurationDenominators);
        assert resolution < Integer.MAX_VALUE;
        return (int) resolution;
    }
    
    /**
     * Converts the midi tick unit from quarter notes to whole notes, using 
     * longs.
     * 
     * @param ticksInWholeNotes ticks in whole notes
     * @return ticks in quarter notes
     */
    private static long convertMidiTickUnitFromQuarterNotesToWholeNotes(long ticksInWholeNotes) {
        return ticksInWholeNotes * 4;
    }
    
    /**
     * Converts the midi tick unit from quarter notes to whole notes, using 
     * ints.
     * 
     * @param ticksInWholeNotes ticks in whole notes
     * @return ticks in quarter notes
     */
    private static int convertMidiTickUnitFromQuarterNotesToWholeNotesInt(int ticksInWholeNotes) {
        return ticksInWholeNotes * 4;
    }
    
    /**
     * Saves the midi sequence to a file.
     * 
     * @param fileName the name of the file
     * @throws java.io.IOException if an I/O error occurs
     */
    public void saveMidiFile(String fileName) throws IOException {
        File outputFile = new File(fileName);                        
        MidiSystem.write(this.getSequence(), MIDI_FILE_TYPE_FOR_MULTI_TRACK_SEQUENCE, outputFile);        
        this.lastMidiFileName = fileName;
    }    
    
    /**
     * Saves the guido notation to file.
     * 
     * @param fileName the name of the file to save to
     * @throws java.io.IOException if an I/O error occurs
     */
    public void saveGuidoFile(String fileName) throws IOException {
        this.getSheetMusicCreator().saveGuidoFile(fileName);
        this.lastGuidoFileName = fileName;
    }
    
    /**
     * Saves the music as a sheet music image in gif format.
     * 
     * @param fileName the file name to save to
     * @throws java.lang.Exception if an error occurs
     */
    public void saveGifImage(String fileName) throws Exception {
        this.getSheetMusicCreator().saveAsGifImage(fileName);    
        this.lastGifFileName = fileName;
    }
    
    /**
     * Saves the music as a wav file.
     * 
     * @param fileName the file name to save to
     * @throws javax.sound.midi.MidiUnavailableException if there is a midi 
     *         problem
     * @throws java.io.IOException if there is an i/o error
     */
    public void saveWavFile(String fileName) throws MidiUnavailableException, IOException {
        this.getAudioFileCreator().saveWavFile(fileName);    
        this.lastWavFileName = fileName;
    }
    
    /**
     * Saves the music to an mp3 file.
     * 
     * @param fileName the file name to save to
     * @throws java.lang.Exception if there is an error
     */
    public void saveMp3File(String fileName) throws Exception {
        this.getAudioFileCreator().saveMp3File(fileName);  
        this.lastMp3FileName = fileName;
    }

    /**
     * Gets the file name of the last gif file saved using this output manager.
     * 
     * @return the last gif file
     */
    public String getLastGifFileName() {
        return lastGifFileName;
    }

    /**
     * Gets the file name of the last guido file saved using this output manager.
     * 
     * @return the last guido file
     */
    public String getLastGuidoFileName() {
        return lastGuidoFileName;
    }

    /**
     * Gets the file name of the last midi file saved using this output manager.
     * 
     * @return the last midi file
     */
    public String getLastMidiFileName() {
        return lastMidiFileName;
    }
    
    /**
     * Gets the file name of the last mp3 file saved using this output manager.
     * 
     * @return the last mp3 file
     */
    public String getLastMp3FileName() {
        return lastMp3FileName;
    }
    
    /**
     * Gets the file name of the last wav file saved using this output manager.
     * 
     * @return the last wav file
     */
    public String getLastWavFileName() {
        return lastWavFileName;
    }        
}
