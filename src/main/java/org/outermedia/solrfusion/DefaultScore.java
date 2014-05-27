package org.outermedia.solrfusion;

import lombok.ToString;

import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.ScoreFactory;

/**
 * The score of different search servers may need some correction, in order to
 * be comparable.
 * 
 * @author ballmann
 * 
 */

@ToString
public class DefaultScore implements Initiable<ScoreFactory>, ScoreCorrectorIfc
{
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
		// TODO Auto-generated method stub

	}

}
