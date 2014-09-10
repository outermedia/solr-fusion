package org.outermedia.solrfusion.response.freemarker;

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
