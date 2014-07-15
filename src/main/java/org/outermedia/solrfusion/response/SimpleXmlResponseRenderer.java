package org.outermedia.solrfusion.response;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.FusionField;
import org.outermedia.solrfusion.configuration.ResponseRendererFactory;
import org.outermedia.solrfusion.configuration.Util;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.FieldVisitor;
import org.outermedia.solrfusion.response.parser.SolrMultiValuedField;
import org.outermedia.solrfusion.response.parser.SolrSingleValuedField;
import org.outermedia.solrfusion.types.ScriptEnv;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ballmann on 6/12/14.
 */
@Slf4j
@Getter
@Setter
public class SimpleXmlResponseRenderer implements ResponseRendererIfc
{
    private Map<String, String> fusionTypeToResponseKey;
    private String multiValueKey;

    /**
     * Factory creates instances only.
     */
    private SimpleXmlResponseRenderer()
    {
        fusionTypeToResponseKey = new HashMap<>();
    }

    @Override
    public String getResponseString(Configuration configuration, ClosableIterator<Document, SearchServerResponseInfo> docStream, FusionRequest request)
    {
        String query = request.getQuery();
        String filterQueryStr = request.getFilterQuery();
        String sort = request.getSolrFusionSortField();
        String fields = request.getFieldsToReturn();

        final StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<response>\n");
        sb.append("<lst name=\"responseHeader\">\n");
        sb.append("  <int name=\"status\">0</int>\n");
        sb.append("  <int name=\"QTime\">0</int>\n");
        sb.append("  <lst name=\"params\">\n");
        sb.append("    <str name=\"indent\">on</str>\n");
        sb.append("    <str name=\"start\">0</str>\n");
        sb.append("    <str name=\"q\"><![CDATA[" + query + "]]></str>\n");
        if(filterQueryStr != null)
        {
            sb.append("    <str name=\"fq\"><![CDATA[" + filterQueryStr + "]]></str>\n");
        }
        if(sort != null)
        {
            sb.append("    <str name=\"sort\"><![CDATA[" + sort + "]]></str>\n");
        }
        if(fields != null)
        {
            sb.append("    <str name=\"fl\"><![CDATA[" + fields + "]]></str>\n");
        }
        sb.append("    <str name=\"version\">2.2</str>\n");
        sb.append("    <str name=\"rows\">" + docStream.size() + "</str>\n");
        sb.append("  </lst>\n");
        sb.append("</lst>\n");
        int totalHitNumber = docStream.getExtraInfo().getTotalNumberOfHits();
        sb.append("<result name=\"response\" numFound=\"" + totalHitNumber + "\" start=\"0\">\n");
        Document d;
        FieldVisitor xmlVisitor = new FieldVisitor()
        {
            @Override
            public boolean visitField(SolrSingleValuedField sf, ScriptEnv env)
            {
                Term t = sf.getTerm();
                String v = null;
                List<String> fusionFieldValues = t.getFusionFieldValue();
                if (fusionFieldValues != null && !fusionFieldValues.isEmpty())
                {
                    v = fusionFieldValues.get(0);
                }
                writeTerm("    ", t.isWasMapped(), t.isRemoved(), t.getFusionFieldName(), v, t.getFusionField());
                return true;
            }

            private void writeTerm(String indent, boolean wasMapped, boolean wasRemoved, String fusionFieldName,
                    String fusionValue, FusionField fusionField)
            {
                if (wasMapped && !wasRemoved && fusionValue != null)
                {
                    String typeTag = fusionTypeToResponseKey.get(fusionField.getType());
                    if (typeTag == null)
                    {
                        typeTag = "str";
                        log.error(
                                "Please define a response key for fusion type '{}' (fusion field: {}) in the configuration of {}.",
                                fusionField.getType(), fusionField.getFieldName(), getClass().getName());
                    }
                    sb.append(indent);
                    sb.append("<");
                    sb.append(typeTag);
                    if (fusionFieldName != null)
                    {
                        sb.append(" name=\"");
                        sb.append(fusionFieldName);
                        sb.append("\"");
                    }
                    sb.append("><![CDATA[");
                    sb.append(fusionValue);
                    sb.append("]]>");
                    sb.append("</");
                    sb.append(typeTag);
                    sb.append(">\n");
                }
                // TODO sf.getTerm().getNewResponseValues();
            }

            @Override
            public boolean visitField(SolrMultiValuedField msf, ScriptEnv env)
            {
                Term t = msf.getTerm();
                if (t != null)
                {
                    boolean printNone = true;
                    if (t.isWasMapped() && !t.isRemoved())
                    {
                        printNone = false;
                    }
                    if (!printNone)
                    {
                        String fusionFieldName = t.getFusionFieldName();
                        sb.append("    <");
                        sb.append(multiValueKey);
                        sb.append(" name=\"" + fusionFieldName + "\">\n");
                        for (String v : t.getFusionFieldValue())
                        {
                            writeTerm("      ", t.isWasMapped(), t.isRemoved(), null, v, t.getFusionField());
                        }
                        sb.append("    </");
                        sb.append(multiValueKey);
                        sb.append(">\n");
                    }
                }
                return true;
            }
        };

        while (docStream.hasNext())
        {
            sb.append("  <doc>\n");
            d = docStream.next();
            d.accept(xmlVisitor, null);
            sb.append("  </doc>\n");
        }
        sb.append("</result>\n");
        sb.append("</response>\n");
        String result = sb + "\n";
        log.trace("Created response:\n{}", result);
        return result;
    }

    @Override
    public void init(ResponseRendererFactory config)
    {
        Util xmlUtil = new Util();
        try
        {
            List<Node> nodes = xmlUtil.xpath("//:map-type", config.getFactoryConfig());
            for (Node n : nodes)
            {
                Element elem = (Element) n;
                String fusionType = elem.getAttribute("fusion-type");
                String key = elem.getAttribute(("key"));
                if (fusionType != null && key != null)
                {
                    fusionTypeToResponseKey.put(fusionType, key);
                }
                else
                {
                    log.error("{}: Please specify a fusion-type ({}) and a key ({})", getClass().getName(), fusionType,
                            key);
                }
            }
        }
        catch (XPathExpressionException e)
        {
            log.error("Caught exception while reading 'map-type' configuration.", e);
        }
        try
        {
            multiValueKey = xmlUtil.getValueOfXpath("//:map-multi-value-type/@key", config.getFactoryConfig());
        }
        catch (XPathExpressionException e)
        {
            log.error("Caught exception while reading 'map-multi-value-type' configuration.", e);
        }
    }

    public static SimpleXmlResponseRenderer getInstance()
    {
        return new SimpleXmlResponseRenderer();
    }
}

