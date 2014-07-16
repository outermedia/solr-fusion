package org.outermedia.solrfusion.response.freemarker;

import lombok.Getter;
import org.outermedia.solrfusion.FusionResponse;

/**
 * Data holder class to represent a response error in the freemarker template.
 *
 * @author stephan
 */
public class FreemarkerErrorHeader
{
    @Getter
    private boolean error;

    @Getter
    private String msg;

    @Getter
    private int code;


    public FreemarkerErrorHeader(FusionResponse response)
    {
        this.error = !response.isOk();
        this.msg = response.getErrorMessage();
        this.code = 400;
    }
}
