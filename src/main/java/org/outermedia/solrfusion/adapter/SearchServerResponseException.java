package org.outermedia.solrfusion.adapter;

/*
 * #%L
 * SolrFusion
 * %%
 * Copyright (C) 2014 outermedia GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import lombok.Getter;
import lombok.Setter;
import org.outermedia.solrfusion.response.parser.ResponseSection;

import java.io.InputStream;

/**
 * In the case that a Solr server returns a non 200 HTTP status this exception is thrown.
 *
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
