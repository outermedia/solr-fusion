package org.outermedia.solrfusion;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ballmann on 04.06.14.
 */
@Setter
@Getter
public class FusionRequest
{
    private String query;

    public Map<String, Float> getBoosts()
    {
        return new HashMap<>(); // TODO from request params
    }
}
