/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "license.md" for more information
*/


package ec.util;

/*
 * RandomChoiceChooser.java
 *
 * Created: Tue Feb  8 15:04:39 2000
 * By: Sean Luke
 */

/**
 * Used by RandomChoice to pick objects by probability from a distribution.
 *
 * @author Sean Luke
 * @version 1.0
 */

public interface RandomChoiceChooser {
    /**
     * Returns obj's probability
     */
    float getProbability(final Object obj);

    /**
     * Sets obj's probability
     */
    void setProbability(final Object obj, final float prob);
}
