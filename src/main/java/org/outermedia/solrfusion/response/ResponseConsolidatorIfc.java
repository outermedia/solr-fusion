package org.outermedia.solrfusion.response;

import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.adapter.SearchServerResponseException;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.ResponseConsolidatorFactory;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.parser.Document;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by ballmann on 04.06.14.
 */
public interface ResponseConsolidatorIfc extends Initiable<ResponseConsolidatorFactory>
{

    public void addResultStream(Configuration config, SearchServerConfig searchServerConfig,
        ClosableIterator<Document, SearchServerResponseInfo> docIterator, FusionRequest request);

    public int numberOfResponseStreams();

    public void clear();

    public ClosableIterator<Document, SearchServerResponseInfo> getResponseIterator(Configuration config,
        FusionRequest fusionRequest) throws InvocationTargetException, IllegalAccessException;

    public void addErrorResponse(SearchServerResponseException se);

    public String getErrorMsg();
}
