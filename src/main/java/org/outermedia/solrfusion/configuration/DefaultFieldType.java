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
		false, true, false), TEXT(false, false, false);

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