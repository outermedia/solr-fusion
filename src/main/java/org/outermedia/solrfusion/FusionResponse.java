package org.outermedia.solrfusion;

import lombok.Getter;
import lombok.Setter;
import org.outermedia.solrfusion.configuration.Message;
import org.outermedia.solrfusion.response.parser.Document;

import java.util.List;

/**
 * Created by ballmann on 04.06.14.
 */
@Getter
@Setter
public class FusionResponse
{
    private List<Document> resultDocs;

    public void setResponseForQueryParseError()
    {
        // TODO
    }

    public void setResponseForNoSearchServerConfiguredError()
    {
        // TODO
    }

    public void setResponseForTooLessServerAnsweredError(Message disasterMessage)
    {
        // TODO
    }
}
