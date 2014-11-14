package org.outermedia.solrfusion.mapper;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ballmann on 11/14/14.
 */
@Slf4j
public abstract class AbstractQueryBuilder implements QueryBuilderIfc
{

    protected Pattern escapePhrasePattern = Pattern.compile("([\"\\\\])", Pattern.CASE_INSENSITIVE);

    protected abstract Pattern getEscapePattern();

    /**
     * Handle \? and \* which are not unescaped when parsed.
     *
     * @param p Pattern contains characters to escape with BACKSLASH
     * @param s String to escape
     * @return an escaped String
     */
    public String escape(Pattern p, String s)
    {
        List<String> words = split("(\\\\\\?|\\\\\\*)", s);
        for (int i = 0; i < words.size(); i++)
        {
            String w = words.get(i);
            if (!w.equals("\\?") && !w.equals("\\*"))
            {
                words.set(i, p.matcher(w).replaceAll("\\\\$1"));
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.size(); i++)
        {
            sb.append(words.get(i));
        }
        return sb.toString();
    }

    protected List<String> split(String regexp, String s)
    {
        List<String> result = new ArrayList<>();
        Pattern p = Pattern.compile(regexp);
        int at = 0;
        Matcher m = p.matcher(s);
        while (at < s.length() && m.find(at))
        {
            int start = m.start();
            int end = m.end();
            result.add(s.substring(at, start));
            result.add(s.substring(start, end));
            at = end;
        }
        if (at < s.length())
        {
            result.add(s.substring(at));
        }
        return result;
    }

    public void escapeSearchWord(StringBuilder queryBuilder, boolean quoted, String searchWord)
    {
        Pattern p;
        if(searchWord.contains(" "))
        {
            quoted = true;
        }
        if (quoted)
        {
            queryBuilder.append('"');
            p = escapePhrasePattern;
        }
        else
        {
            p = getEscapePattern();
        }
        String s = escape(p, searchWord);
        queryBuilder.append(s);
        if (quoted)
        {
            queryBuilder.append('"');
        }
    }
}
