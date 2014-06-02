package org.outermedia.solrfusion.configuration;

import javax.xml.bind.annotation.XmlTransient;

/**
 * The default fusion field types.
 * 
 * @author ballmann
 * 
 */

@XmlTransient
public enum DefaultFieldType
{

	INT(true, false, false), LONG(true, false, false), DOUBLE(true, false,
		false), FLOAT(true, false, false), BOOLEAN(false, false, true), DATE(
		false, true, false), STRING(false, false, false);

	private boolean numeric;
	private boolean date;
	private boolean bool;

	private DefaultFieldType(boolean numeric, boolean date, boolean bool)
	{
		this.numeric = numeric;
		this.bool = bool;
		this.date = date;
	}

	public boolean isDate()
	{
		return date;
	}

	public boolean isNumeric()
	{
		return numeric;
	}

	public boolean isBool()
	{
		return bool;
	}
}