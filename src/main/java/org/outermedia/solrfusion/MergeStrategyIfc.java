package org.outermedia.solrfusion;

import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.Merge;

/**
 * Identify response documents which represent the same object and merge them.
 * 
 * @author ballmann
 * 
 */

public interface MergeStrategyIfc extends Initiable<Merge>
{
	// TODO define required methods e.g. mergeDocs
}
