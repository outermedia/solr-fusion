package org.outermedia.solrfusion.configuration;

/**
 * Created by ballmann on 9/22/14.
 */
public enum PostProcessorStatus
{
    CONTINUE, STOP, DO_NOT_SEND_QUERY;

    public boolean doContinue()
    {
        return this == CONTINUE || this == DO_NOT_SEND_QUERY;
    }

}

