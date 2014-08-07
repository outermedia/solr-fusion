package org.outermedia.solrfusion.response;

import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.ResponseConsolidatorFactory;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.Highlighting;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by ballmann on 04.06.14.
 */
public interface ResponseConsolidatorIfc extends Initiable<ResponseConsolidatorFactory>
{
    public void init(Configuration config) throws InvocationTargetException, IllegalAccessException;

    public void addResultStream(SearchServerConfig searchServerConfig,
        ClosableIterator<Document, SearchServerResponseInfo> docIterator, FusionRequest request,
        List<Highlighting> highlighting);

    public int numberOfResponseStreams();

    public void clear();

    public ClosableIterator<Document, SearchServerResponseInfo> getResponseIterator(FusionRequest fusionRequest)
        throws InvocationTargetException, IllegalAccessException;

    public void addErrorResponse(Exception se);

    public String getErrorMsg();

    public Document completelyMapMergedDoc(Collection<Document> sameDocuments, Map<String, Document> highlighting)
        throws InvocationTargetException, IllegalAccessException;
}
