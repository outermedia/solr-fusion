package org.outermedia.solrfusion;

import lombok.ToString;

import org.outermedia.solrfusion.configuration.IdGeneratorFactory;
import org.outermedia.solrfusion.configuration.Initiable;

/**
 * The unified documents need an id, which are created by this generator.
 * 
 * @author ballmann
 * 
 */

@ToString
public class DefaultIdGenerator implements Initiable<IdGeneratorFactory>,
	IdGeneratorIfc
{

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
