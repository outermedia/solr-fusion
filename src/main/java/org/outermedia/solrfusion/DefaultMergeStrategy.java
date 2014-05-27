package org.outermedia.solrfusion;

import lombok.ToString;

import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.Merge;

/**
 * Identify response documents which represent the same object and merge them.
 * 
 * @author ballmann
 * 
 */

@ToString
public class DefaultMergeStrategy implements Initiable<Merge>, MergeStrategyIfc
{
	public static class Factory
	{
		public static DefaultMergeStrategy getInstance()
		{
			return new DefaultMergeStrategy();
		}
	}

	@Override
	public void init(Merge config)
	{
		// TODO Auto-generated method stub

	}

}
