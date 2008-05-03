package com.myronmarston.music.scales;

import com.myronmarston.music.NoteName;

/**
 * A natural minor scale: WHWWHWW.  This is also known as Aeolian mode when 
 * analyzing music modally.
 * 
 * @author Myron
 */
public class MinorScale extends Scale {        
    private final static int[] SCALE_STEPS = new int[] {0, 2, 3, 5, 7, 8, 10};
    private final static int[] LETTER_NUMBERS = new int[] {0, 1, 2, 3, 4, 5, 6};

    /**
     * Constructor.
     * 
     * @param keyName the name of the tonal center
     * @throws com.myronmarston.music.scales.InvalidKeySignatureException thrown
     *         when the key is invalid
     */
    public MinorScale(NoteName keyName) throws InvalidKeySignatureException {        
        super(new KeySignature(Tonality.Minor, keyName));        
    }        

    @Override
    public int[] getScaleStepArray() {
        return MinorScale.SCALE_STEPS;
    }

    @Override
    public int[] getLetterNumberArray() {
        return MinorScale.LETTER_NUMBERS;
    }        
}
