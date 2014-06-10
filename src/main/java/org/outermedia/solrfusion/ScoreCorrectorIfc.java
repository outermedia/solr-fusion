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
    public double applyCorrection(double score);
}
