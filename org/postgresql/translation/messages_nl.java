// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.translation;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class messages_nl extends ResourceBundle
{
    private static final String[] table;
    
    public Object handleGetObject(final String msgid) throws MissingResourceException {
        final int hash_val = msgid.hashCode() & Integer.MAX_VALUE;
        final int idx = hash_val % 18 << 1;
        final Object found = messages_nl.table[idx];
        if (found != null && msgid.equals(found)) {
            return messages_nl.table[idx + 1];
        }
        return null;
    }
    
    @Override
    public Enumeration getKeys() {
        return new Enumeration() {
            private int idx = 0;
            
            {
                while (this.idx < 36 && messages_nl.table[this.idx] == null) {
                    this.idx += 2;
                }
            }
            
            @Override
            public boolean hasMoreElements() {
                return this.idx < 36;
            }
            
            @Override
            public Object nextElement() {
                final Object key = messages_nl.table[this.idx];
                do {
                    this.idx += 2;
                } while (this.idx < 36 && messages_nl.table[this.idx] == null);
                return key;
            }
        };
    }
    
    public ResourceBundle getParent() {
        return this.parent;
    }
    
    static {
        final String[] t = table = new String[] { "", "Project-Id-Version: PostgreSQL JDBC Driver 8.0\nReport-Msgid-Bugs-To: \nPO-Revision-Date: 2004-10-11 23:55-0700\nLast-Translator: Arnout Kuiper <ajkuiper@wxs.nl>\nLanguage-Team: Dutch <ajkuiper@wxs.nl>\nLanguage: nl\nMIME-Version: 1.0\nContent-Type: text/plain; charset=UTF-8\nContent-Transfer-Encoding: 8bit\n", "Something unusual has occurred to cause the driver to fail. Please report this exception.", "Iets ongewoons is opgetreden, wat deze driver doet falen. Rapporteer deze fout AUB: {0}", null, null, null, null, "Unknown Types value.", "Onbekende Types waarde.", null, null, "Fastpath call {0} - No result was returned and we expected an integer.", "Fastpath aanroep {0} - Geen resultaat werd teruggegeven, terwijl we een integer verwacht hadden.", null, null, null, null, null, null, "The fastpath function {0} is unknown.", "De fastpath functie {0} is onbekend.", "No results were returned by the query.", "Geen resultaten werden teruggegeven door de query.", null, null, "An unexpected result was returned by a query.", "Een onverwacht resultaat werd teruggegeven door een query", null, null, null, null, null, null, null, null };
    }
}
