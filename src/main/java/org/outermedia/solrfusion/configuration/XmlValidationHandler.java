package org.outermedia.solrfusion.configuration;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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