package org.outermedia.solrfusion.configuration;

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

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;

/**
 * XML validation handler to log validation errors.
 */
@Getter
@Slf4j
public class XmlValidationHandler implements ValidationEventHandler
{
	private boolean foundErrors;

	public XmlValidationHandler()
	{
		foundErrors = false;
	}

	@Override
	public boolean handleEvent(ValidationEvent ve)
	{
		if (ve.getSeverity() == ValidationEvent.FATAL_ERROR
			|| ve.getSeverity() == ValidationEvent.ERROR)
		{
			ValidationEventLocator locator = ve.getLocator();
			log.error("{}: {} at line={}, column={}", locator.getURL(),
				ve.getMessage(), locator.getLineNumber(),
				locator.getColumnNumber());
			foundErrors = true;
		}
		return true;
	}
}