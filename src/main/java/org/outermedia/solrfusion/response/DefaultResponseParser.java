package org.outermedia.solrfusion.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.configuration.ResponseParserFactory;
import org.outermedia.solrfusion.response.parser.Result;

import javax.xml.bind.annotation.*;

/**
 * Parses a solr server's xml response into an internal representation.
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="", factoryClass=DefaultResponseParser.Factory.class, factoryMethod="getInstance")
@XmlRootElement(name = "response") //, namespace = "http://solrfusion.outermedia.org/configuration/")
@ToString
public class DefaultResponseParser implements ResponseParserIfc
{
	/**
	 * Factory creates instances only.
	 */
	private DefaultResponseParser()
	{}

	public static class Factory
	{
		public static DefaultResponseParser getInstance()
		{
			return new DefaultResponseParser();
		}
	}

	@Override
	public void init(ResponseParserFactory config)
	{
		// TODO Auto-generated method stub

	}

    @XmlElement(name = "result", required = true) //, namespace = "http://solrfusion.outermedia.org/configuration/"
    @Getter
    @Setter
    private Result result;


}
