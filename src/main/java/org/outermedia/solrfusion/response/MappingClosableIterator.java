package org.outermedia.solrfusion.response;

import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by ballmann on 6/11/14.
 */
@Slf4j
public class MappingClosableIterator implements ClosableIterator<Document, SearchServerResponseInfo>
{
    private List<String> fieldsToMap;
    private Configuration config;
    private SearchServerConfig searchServerConfig;
    private ClosableIterator<Document, SearchServerResponseInfo> documents;

    private boolean ignoreUnmappedFields = true;
    private Document nextDoc;

    /**
     * @param documents
     * @param config
     * @param searchServerConfig
     * @param fieldsToMap        either null (for all) or a list of field names to process
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public MappingClosableIterator(ClosableIterator<Document, SearchServerResponseInfo> documents, Configuration config,
        SearchServerConfig searchServerConfig, List<String> fieldsToMap)
        throws InvocationTargetException, IllegalAccessException
    {
        this.config = config;
        this.documents = documents;
        this.searchServerConfig = searchServerConfig;
        this.fieldsToMap = fieldsToMap;
    }

    @Override
    public boolean hasNext()
    {
        while (documents.hasNext())
        {
            nextDoc = documents.next();
            ScriptEnv env = new ScriptEnv();
            try
            {
                int mappedFieldNr = config.getResponseMapper().mapResponse(config, searchServerConfig, nextDoc, env,
                    fieldsToMap);
                // no field was mapped = no fusion value present -> ignore the empty document
                if (mappedFieldNr == 0)
                {
                    continue;
                }
                if (nextDoc != null)
                {
                    return true;
                }
            }
            catch (Exception e)
            {
                log.error("Mapping failed. Document is ignored. See previous errors in log file for reasons.", e);
            }
        }
        nextDoc = null;
        return false;
    }

    @Override
    public Document next()
    {
        return nextDoc;
    }

    /**
     * Not supported. Throws always a runtime exception.
     */
    @Override
    public void remove()
    {
        throw new RuntimeException("Not supported.");
    }

    @Override
    public void close()
    {
        documents.close();
    }

    @Override
    public int size()
    {
        return documents.size();
    }

    @Override
    public SearchServerResponseInfo getExtraInfo()
    {
        return documents.getExtraInfo();
    }

    @Override
    public void setExtraInfo(SearchServerResponseInfo info)
    {
        documents.setExtraInfo(info);
    }
}
