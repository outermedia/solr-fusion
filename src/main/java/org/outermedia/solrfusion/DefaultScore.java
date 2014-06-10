package org.outermedia.solrfusion;

import lombok.ToString;

import org.outermedia.solrfusion.configuration.ScoreFactory;

/**
 * The score of different search servers may need some correction, in order to
 * be comparable.
 *
 * @author ballmann
 */

@ToString
public class DefaultScore implements ScoreCorrectorIfc
{
    private double factor;

    /**
     * Factory creates instances only.
     */
    private DefaultScore()
    {
    }

    @Override
    public double applyCorrection(double score)
    {
        return score * factor;
    }

    public static class Factory
    {
        public static DefaultScore getInstance()
        {
            return new DefaultScore();
        }
    }

    @Override
    public void init(ScoreFactory config)
    {
        this.factor = config.getFactor();
    }

}
