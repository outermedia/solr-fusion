package org.outermedia.solrfusion;

/*
 * #%L
 * SolrFusion
 * %%
 * Copyright (C) 2014 outermedia GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
    protected DefaultScore()
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
