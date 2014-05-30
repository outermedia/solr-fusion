package org.outermedia.solrfusion;

import lombok.ToString;

import org.outermedia.solrfusion.configuration.ScoreFactory;

/**
 * The score of different search servers may need some correction, in order to
 * be comparable.
 * 
 * @author ballmann
 * 
 */

@ToString
public class DefaultScore implements ScoreCorrectorIfc
{
	/**
	 * Factory creates instances only.
	 */
	private DefaultScore()
	{}

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
