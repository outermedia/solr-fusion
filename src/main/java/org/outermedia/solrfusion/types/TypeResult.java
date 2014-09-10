package org.outermedia.solrfusion.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * The return type of ScriptType subclasses.
 *
 * Created by ballmann on 8/13/14.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class TypeResult
{
    private List<String> values;

    private List<Integer> wordCounts;
}
