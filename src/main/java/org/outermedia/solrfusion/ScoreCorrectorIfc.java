package org.outermedia.solrfusion;

import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.ScoreFactory;

/**
 * The score of different search servers may need some correction, in order to
 * be comparable.
 * 
 * @author ballmann
 * 
 */

public interface ScoreCorrectorIfc extends Initiable<ScoreFactory>
{
    /**
     * Correct the specified score.
     *
     * @param score
     * @return a new score value
     */
    public double applyCorrection(double score);
}
