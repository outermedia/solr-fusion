package org.outermedia.solrfusion;

import java.util.*;

/**
 * Created by ballmann on 7/30/14.
 */
public class MultiKeyAndValueMap<K, V>
{
    protected Map<K, SetProxy<V>> entries;

    protected static class SetProxy<V>
    {
        protected Set<V> realSet;
        protected long id;
        protected static Long idCounter = 1L;

        protected SetProxy(Set<V> values)
        {
            realSet = values;
            id = idCounter++;
        }

        @Override public boolean equals(Object obj)
        {
            if (!(obj instanceof SetProxy))
            {
                return false;
            }
            return realSet.equals(((SetProxy) obj).realSet);
        }

        @Override public int hashCode()
        {
            return realSet.hashCode();
        }
    }

    public MultiKeyAndValueMap()
    {
        entries = new HashMap<>();
    }

    public void put(Collection<K> keys, V value)
    {
        // this is the trick; SetProxy delegates equal/hashcode to the real set!
        // this means different SetProxy objects may point to the same set object.
        Set<SetProxy<V>> differentContainers = new HashSet<>();
        for (K k : keys)
        {
            SetProxy<V> currentValue = entries.get(k);
            if (currentValue != null)
            {
                differentContainers.add(currentValue);
            }
        }
        SetProxy<V> oneContainer;
        if (differentContainers.size() == 0)
        {
            oneContainer = new SetProxy(new HashSet<>());
        }
        else
        {
            Iterator<SetProxy<V>> containerIt = differentContainers.iterator();
            oneContainer = containerIt.next();
            // merge containers to one
            while (containerIt.hasNext())
            {
                SetProxy<V> otherContainer = containerIt.next();
                oneContainer.realSet.addAll(otherContainer.realSet);
                // let all keys of otherContainer point to the united container
                otherContainer.realSet = oneContainer.realSet;
                otherContainer.id = oneContainer.id;
            }
        }
        oneContainer.realSet.add(value);
        for (K k : keys)
        {
            entries.put(k, oneContainer);
        }
    }

    public Set<V> get(K key)
    {
        SetProxy proxy = entries.get(key);
        if (proxy == null)
        {
            return null;
        }
        return proxy.realSet;
    }

    public Set<K> keySet()
    {
        return entries.keySet();
    }

    public Collection<Set<V>> values()
    {
        Set<Set<V>> result = new HashSet<>();
        for (SetProxy<V> set : entries.values())
        {
            result.add(set.realSet);
        }
        return result;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for(Map.Entry<K, SetProxy<V>> entry : entries.entrySet())
        {
            sb.append("\t");
            sb.append(entry.getKey());
            SetProxy<V> v = entry.getValue();
            sb.append("=");
            sb.append(v.id);
            sb.append("@");
            sb.append(String.valueOf(v.realSet));
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    public void resetIdCounter()
    {
        SetProxy.idCounter = 1L;
    }
}
