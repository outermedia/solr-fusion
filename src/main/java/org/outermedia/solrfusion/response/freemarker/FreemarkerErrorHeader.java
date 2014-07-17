package org.outermedia.solrfusion.response.freemarker;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.FusionResponse;

/**
 * Data holder class to represent a response error in the freemarker template.
 *
 * @author sballmann
 */
@Slf4j
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
        // log.debug("RESPONSE IS OK: " + response.isOk());
        this.error = !response.isOk();
        this.msg = response.getErrorMessage();
        this.code = 400;
    }
}
