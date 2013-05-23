
package org.mitre.opensextant.extraction;

import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author Marc C. Ubaldino, MITRE <ubaldino at mitre dot org>
 */
public class TaxonMatch extends org.mitre.flexpat.TextMatch {

    private List<Taxon> taxons = null;

    public List<Taxon> getTaxons(){
        return taxons;
    }
    
    public void addTaxon(Taxon t) {
        if (t == null) {
            return;
        }

        if (taxons == null) {
            taxons = new ArrayList<>();
        }
        taxons.add(t);
    }
    
    public boolean hasTaxons(){
        if (taxons == null){
            return false;
        }
        return ! taxons.isEmpty();
    }
}
