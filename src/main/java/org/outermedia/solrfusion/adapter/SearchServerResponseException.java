package org.outermedia.solrfusion.adapter;

import lombok.Getter;
import lombok.Setter;
import org.outermedia.solrfusion.response.parser.ResponseSection;

import java.io.InputStream;

/**
 * Created by ballmann on 7/7/14.
 */
@Getter
@Setter
public class SearchServerResponseException extends RuntimeException
{
    private int httpErrorCode;
    private String httpReason;
    private ResponseSection responseError;
    private InputStream httpContent;

    public SearchServerResponseException(int httpErrorCode, String httpReason, InputStream httpContent)
    {
        this.httpErrorCode = httpErrorCode;
        this.httpReason = httpReason;
        this.httpContent = httpContent;
    }

    @Override public String getMessage()
    {
        String code = String.valueOf(httpErrorCode);
        String msg = httpReason;
        if (responseError != null)
        {
            code = responseError.getErrorCode();
            msg = responseError.getErrorMsg();
        }
        return "ERROR " + code + ": " + msg;
    }
}
