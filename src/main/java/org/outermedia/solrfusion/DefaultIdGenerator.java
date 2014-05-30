package org.outermedia.solrfusion;

import lombok.ToString;

import org.outermedia.solrfusion.configuration.IdGeneratorFactory;

/**
 * The unified documents need an id, which are created by this generator.
 * 
 * @author ballmann
 * 
 */

@ToString
public class DefaultIdGenerator implements IdGeneratorIfc
{
	/**
	 * Factory creates instances only.
	 */
	private DefaultIdGenerator()
	{}

	public static class Factory
	{
		public static DefaultIdGenerator getInstance()
		{
			return new DefaultIdGenerator();
		}
	}

	@Override
	public void init(IdGeneratorFactory config)
	{
		// TODO Auto-generated method stub
	}

}
