
package org.mitre.opensextant.extraction;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Arrays;

/**
 * For now, what seems useful are catalog, name, terms.

 * @author ubaldino
 */
public class Taxon {

    /**  Catalog, for example "fruit" */
    public String catalog = null;
    
    /** Node name: citrus.tropical */
    public String name = null;
    /** Nod OID:  1.2.3  */
    public String id = null;
    /** Node root: citrus */
    public String rootid = null;
    /** Terms: "pineapple", "mango", ... */
    public String[] terms = null;
    public Set<String> termset = null;
    public Set<String> tagset = null;

    public Taxon() {
    }

    public void setName(String n) {
        name = n;
    }

    public void setId(String i) {
        id = i;
    }

    public void setRootid(String i) {
        rootid = i;
    }

    public void setTerms(String[] t) {
        terms = t;
        if (terms != null) {
            termset = new HashSet<String>();
            termset.addAll(Arrays.asList(terms));
        }
    }

    public void addTags(Collection tlist) {
        if (tlist==null){
            return;
        }
        
        for (Object o : tlist){
            addTag((String)o);
        }
    }
    
    public void addTag(String t) {
        if (t==null){
            return;
        }
        
        if (tagset == null) {
            tagset = new HashSet<String>();
        }
        tagset.add(t);
    }


    public void addTerm(String t) {
        if (termset == null) {
            termset = new HashSet<String>();
        }
        termset.add(t);
    }

    public void addTerms(Collection<String> tlist) {
        if (tlist == null) {
            return;
        }
        if (termset == null) {
            termset = new HashSet<String>();
        }
        termset.addAll(tlist);
    }

    @Override
    public String toString() {
        if (termset == null) {
            return name;
        }
        return name + " " + termset.toString();
    }
}
