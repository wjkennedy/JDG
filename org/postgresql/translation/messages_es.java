// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.translation;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class messages_es extends ResourceBundle
{
    private static final String[] table;
    
    public Object handleGetObject(final String msgid) throws MissingResourceException {
        final int hash_val = msgid.hashCode() & Integer.MAX_VALUE;
        int idx = hash_val % 37 << 1;
        final Object found = messages_es.table[idx];
        if (found == null) {
            return null;
        }
        if (msgid.equals(found)) {
            return messages_es.table[idx + 1];
        }
        final int incr = hash_val % 35 + 1 << 1;
        while (true) {
            idx += incr;
            if (idx >= 74) {
                idx -= 74;
            }
            final Object found2 = messages_es.table[idx];
            if (found2 == null) {
                return null;
            }
            if (msgid.equals(found2)) {
                return messages_es.table[idx + 1];
            }
        }
    }
    
    @Override
    public Enumeration getKeys() {
        return new Enumeration() {
            private int idx = 0;
            
            {
                while (this.idx < 74 && messages_es.table[this.idx] == null) {
                    this.idx += 2;
                }
            }
            
            @Override
            public boolean hasMoreElements() {
                return this.idx < 74;
            }
            
            @Override
            public Object nextElement() {
                final Object key = messages_es.table[this.idx];
                do {
                    this.idx += 2;
                } while (this.idx < 74 && messages_es.table[this.idx] == null);
                return key;
            }
        };
    }
    
    public ResourceBundle getParent() {
        return this.parent;
    }
    
    static {
        final String[] t = table = new String[] { "", "Project-Id-Version: JDBC PostgreSQL Driver\nReport-Msgid-Bugs-To: \nPO-Revision-Date: 2004-10-22 16:51-0300\nLast-Translator: Diego Gil <diego@adminsa.com>\nLanguage-Team: \nLanguage: \nMIME-Version: 1.0\nContent-Type: text/plain; charset=UTF-8\nContent-Transfer-Encoding: 8bit\nX-Poedit-Language: Spanish\n", null, null, "The column index is out of range: {0}, number of columns: {1}.", "El \u00edndice de la columna est\u00e1 fuera de rango: {0}, n\u00famero de columnas: {1}.", null, null, null, null, null, null, "Unknown Response Type {0}.", "Tipo de respuesta desconocida {0}.", null, null, "Protocol error.  Session setup failed.", "Error de protocolo. Fall\u00f3 el inicio de la sesi\u00f3n.", null, null, "The server requested password-based authentication, but no password was provided.", "El servidor requiere autenticaci\u00f3n basada en contrase\u00f1a, pero no se ha provisto ninguna contrase\u00f1a.", null, null, null, null, "A result was returned when none was expected.", "Se retorn\u00f3 un resultado cuando no se esperaba ninguno.", "Server SQLState: {0}", "SQLState del servidor: {0}.", "The array index is out of range: {0}, number of elements: {1}.", "El \u00edndice del arreglo esta fuera de rango: {0}, n\u00famero de elementos: {1}.", "Premature end of input stream, expected {0} bytes, but only read {1}.", "Final prematuro del flujo de entrada, se esperaban {0} bytes, pero solo se leyeron {1}.", null, null, "The connection attempt failed.", "El intento de conexi\u00f3n fall\u00f3.", "Failed to create object for: {0}.", "Fallo al crear objeto: {0}.", null, null, "An error occurred while setting up the SSL connection.", "Ha ocorrido un error mientras se establec\u00eda la conexi\u00f3n SSL.", null, null, null, null, "No value specified for parameter {0}.", "No se ha especificado un valor para el par\u00e1metro {0}.", "The server does not support SSL.", "Este servidor no soporta SSL.", "An unexpected result was returned by a query.", "Una consulta retorn\u00f3 un resultado inesperado.", null, null, null, null, null, null, "Something unusual has occurred to cause the driver to fail. Please report this exception.", "Algo inusual ha ocurrido que provoc\u00f3 un fallo en el controlador. Por favor reporte esta excepci\u00f3n.", null, null, "No results were returned by the query.", "La consulta no retorn\u00f3 ning\u00fan resultado.", null, null, null, null, null, null, null, null };
    }
}
