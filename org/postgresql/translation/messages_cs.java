// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.translation;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class messages_cs extends ResourceBundle
{
    private static final String[] table;
    
    public Object handleGetObject(final String msgid) throws MissingResourceException {
        final int hash_val = msgid.hashCode() & Integer.MAX_VALUE;
        int idx = hash_val % 157 << 1;
        final Object found = messages_cs.table[idx];
        if (found == null) {
            return null;
        }
        if (msgid.equals(found)) {
            return messages_cs.table[idx + 1];
        }
        final int incr = hash_val % 155 + 1 << 1;
        while (true) {
            idx += incr;
            if (idx >= 314) {
                idx -= 314;
            }
            final Object found2 = messages_cs.table[idx];
            if (found2 == null) {
                return null;
            }
            if (msgid.equals(found2)) {
                return messages_cs.table[idx + 1];
            }
        }
    }
    
    @Override
    public Enumeration getKeys() {
        return new Enumeration() {
            private int idx = 0;
            
            {
                while (this.idx < 314 && messages_cs.table[this.idx] == null) {
                    this.idx += 2;
                }
            }
            
            @Override
            public boolean hasMoreElements() {
                return this.idx < 314;
            }
            
            @Override
            public Object nextElement() {
                final Object key = messages_cs.table[this.idx];
                do {
                    this.idx += 2;
                } while (this.idx < 314 && messages_cs.table[this.idx] == null);
                return key;
            }
        };
    }
    
    public ResourceBundle getParent() {
        return this.parent;
    }
    
    static {
        final String[] t = table = new String[] { "", "Project-Id-Version: PostgreSQL JDBC Driver 8.0\nReport-Msgid-Bugs-To: \nPO-Revision-Date: 2005-08-21 20:00+0200\nLast-Translator: Petr Dittrich <bodyn@medoro.org>\nLanguage-Team: \nLanguage: \nMIME-Version: 1.0\nContent-Type: text/plain; charset=UTF-8\nContent-Transfer-Encoding: 8bit\n", "A connection could not be made using the requested protocol {0}.", "Spojen\u00ed nelze vytvo\u0159it s pou\u017eit\u00edm \u017e\u00e1dan\u00e9ho protokolu {0}.", "Malformed function or procedure escape syntax at offset {0}.", "Po\u0161kozen\u00e1 funkce nebo opu\u0161t\u011bn\u00ed procedury na pozici {0}.", null, null, "Cannot cast an instance of {0} to type {1}", "Nemohu p\u0159etypovat instanci {0} na typ {1}", null, null, "ResultSet is not updateable.  The query that generated this result set must select only one table, and must select all primary keys from that table. See the JDBC 2.1 API Specification, section 5.6 for more details.", "ResultSet nen\u00ed aktualizavateln\u00fd. Dotaz mus\u00ed vyb\u00edrat pouze z jedn\u00e9 tabulky a mus\u00ed obsahovat v\u0161echny prim\u00e1rn\u00ed kl\u00ed\u010de tabulky. Koukni do JDBC 2.1 API Specifikace, sekce 5.6 pro v\u00edce podrobnost\u00ed.", "The JVM claims not to support the {0} encoding.", "JVM tvrd\u00ed, \u017ee nepodporuje kodov\u00e1n\u00ed {0}.", "An I/O error occurred while sending to the backend.", "Vystupn\u011b/v\u00fdstupn\u00ed chyba p\u0159i odes\u00edl\u00e1n\u00ed k backend.", "Statement has been closed.", "Statement byl uzav\u0159en.", "Unknown Types value.", "Nezn\u00e1m\u00e1 hodnota typu.", "ResultSets with concurrency CONCUR_READ_ONLY cannot be updated.", "ResultSets se soub\u011b\u017enost\u00ed CONCUR_READ_ONLY nem\u016f\u017ee b\u00fdt aktualizov\u00e1no", null, null, "You must specify at least one column value to insert a row.", "Mus\u00edte vyplnit alespo\u0148 jeden sloupec pro vlo\u017een\u00ed \u0159\u00e1dku.", null, null, null, null, "No primary key found for table {0}.", "Nenalezen prim\u00e1rn\u00ed kl\u00ed\u010d pro tabulku {0}.", "Cannot establish a savepoint in auto-commit mode.", "Nemohu vytvo\u0159it savepoint v auto-commit modu.", null, null, "Can''t use relative move methods while on the insert row.", "Nem\u016f\u017eete pou\u017e\u00edvat relativn\u00ed p\u0159esuny p\u0159i vkl\u00e1d\u00e1n\u00ed \u0159\u00e1dku.", null, null, null, null, "The column name {0} was not found in this ResultSet.", "Sloupec pojmenovan\u00fd {0} nebyl nalezen v ResultSet.", "This statement has been closed.", "P\u0159\u00edkaz byl uzav\u0159en.", "The SSLSocketFactory class provided {0} could not be instantiated.", "T\u0159\u00edda SSLSocketFactory poskytla {0} co\u017e nem\u016f\u017ee b\u00fdt instancionizov\u00e1no.", "Multiple ResultSets were returned by the query.", "V\u00edcen\u00e1sobn\u00fd ResultSet byl vr\u00e1cen dotazem.", "DataSource has been closed.", "DataSource byl uzav\u0159en.", null, null, "Error loading default settings from driverconfig.properties", "Chyba na\u010d\u00edt\u00e1n\u00ed standardn\u00edho nastaven\u00ed z driverconfig.properties", null, null, null, null, "Bad value for type {0} : {1}", "\u0160patn\u00e1 hodnota pro typ {0} : {1}", null, null, "Method {0} is not yet implemented.", "Metoda {0} nen\u00ed implementov\u00e1na.", "The array index is out of range: {0}", "Index pole mimo rozsah: {0}", "Unexpected command status: {0}.", "Neo\u010dek\u00e1van\u00fd stav p\u0159\u00edkazu: {0}.", null, null, "Expected command status BEGIN, got {0}.", "O\u010dek\u00e1v\u00e1n p\u0159\u00edkaz BEGIN, obdr\u017een {0}.", "Cannot retrieve the id of a named savepoint.", "Nemohu z\u00edskat id nepojmenovan\u00e9ho savepointu.", "Unexpected error writing large object to database.", "Neo\u010dek\u00e1van\u00e1 chyba p\u0159i zapisov\u00e1n\u00ed velk\u00e9ho objektu do datab\u00e1ze.", null, null, null, null, "Not on the insert row.", "Ne na vkl\u00e1dan\u00e9m \u0159\u00e1dku.", "Returning autogenerated keys is not supported.", "Vr\u00e1cen\u00ed automaticky generovan\u00fdch kl\u00ed\u010d\u016f nen\u00ed podporov\u00e1no.", "The server requested password-based authentication, but no password was provided.", "Server vy\u017eaduje ov\u011b\u0159en\u00ed heslem, ale \u017e\u00e1dn\u00e9 nebylo posl\u00e1no.", null, null, null, null, null, null, null, null, "Unable to load the class {0} responsible for the datatype {1}", "Nemohu na\u010d\u00edst t\u0159\u00eddu {0} odpov\u011bdnou za typ {1}", "Invalid fetch direction constant: {0}.", "\u0160patn\u00fd sm\u011br \u010dten\u00ed: {0}.", "Conversion of money failed.", "P\u0159evod pen\u011bz selhal.", "Connection has been closed.", "Spojeni bylo uzav\u0159eno.", "Cannot retrieve the name of an unnamed savepoint.", "Nemohu z\u00edskat n\u00e1zev nepojmenovan\u00e9ho savepointu.", "Large Objects may not be used in auto-commit mode.", "Velk\u00e9 objecky nemohou b\u00fdt pou\u017eity v auto-commit modu.", "This ResultSet is closed.", "Tento ResultSet je uzav\u0159en\u00fd.", null, null, null, null, "Something unusual has occurred to cause the driver to fail. Please report this exception.", "N\u011bco neobvykl\u00e9ho p\u0159inutilo ovlada\u010d selhat. Pros\u00edm nahlaste tuto vyj\u00edmku.", "The server does not support SSL.", "Server nepodporuje SSL.", "Invalid stream length {0}.", "Vadn\u00e1 d\u00e9lka proudu {0}.", null, null, null, null, "The maximum field size must be a value greater than or equal to 0.", "Maxim\u00e1ln\u00ed velikost pole mus\u00ed b\u00fdt nez\u00e1porn\u00e9 \u010d\u00edslo.", null, null, "Cannot call updateRow() when on the insert row.", "Nemohu volat updateRow() na vlk\u00e1dan\u00e9m \u0159\u00e1dku.", "A CallableStatement was executed with nothing returned.", "CallableStatement byl spu\u0161t\u011bn, le\u010d nic nebylo vr\u00e1ceno.", "Provided Reader failed.", "Selhal poskytnut\u00fd Reader.", null, null, null, null, null, null, null, null, null, null, "Cannot call deleteRow() when on the insert row.", "Nem\u016f\u017eete volat deleteRow() p\u0159i vkl\u00e1d\u00e1n\u00ed \u0159\u00e1dku.", null, null, null, null, null, null, null, null, "Where: {0}", "Kde: {0}", "An unexpected result was returned by a query.", "Obdr\u017een neo\u010dek\u00e1van\u00fd v\u00fdsledek dotazu.", "The connection attempt failed.", "Pokus o p\u0159ipojen\u00ed selhal.", "Too many update results were returned.", "Bylo vr\u00e1ceno p\u0159\u00edli\u0161 mnoho v\u00fdsledk\u016f aktualizac\u00ed.", "Unknown type {0}.", "Nezn\u00e1m\u00fd typ {0}.", "{0} function takes two and only two arguments.", "Funkce {0} bere pr\u00e1v\u011b dva argumenty.", "{0} function doesn''t take any argument.", "Funkce {0} nebere \u017e\u00e1dn\u00fd argument.", null, null, "Unable to find name datatype in the system catalogs.", "Nemohu naj\u00edt n\u00e1zev typu v syst\u00e9mov\u00e9m katalogu.", "Protocol error.  Session setup failed.", "Chyba protokolu. Nastaven\u00ed relace selhalo.", "{0} function takes one and only one argument.", "Funkce {0} bere jeden argument.", null, null, null, null, null, null, null, null, "The driver currently does not support COPY operations.", "Ovlada\u010d nyn\u00ed nepodporuje p\u0159\u00edkaz COPY.", null, null, "Invalid character data was found.  This is most likely caused by stored data containing characters that are invalid for the character set the database was created in.  The most common example of this is storing 8bit data in a SQL_ASCII database.", "Nalezena vada ve znakov\u00fdch datech. Toto m\u016f\u017ee b\u00fdt zp\u016fsobeno ulo\u017een\u00fdmi daty obsahuj\u00edc\u00edmi znaky, kter\u00e9 jsou z\u00e1vadn\u00e9 pro znakovou sadu nastavenou p\u0159i zakl\u00e1d\u00e1n\u00ed datab\u00e1ze. Nejzn\u00e1mej\u0161\u00ed p\u0159\u00edklad je ukl\u00e1d\u00e1n\u00ed 8bitov\u00fdch dat vSQL_ASCII datab\u00e1zi.", null, null, null, null, "Fetch size must be a value greater to or equal to 0.", "Nabran\u00e1 velikost mus\u00ed b\u00fdt nez\u00e1porn\u00e1.", null, null, null, null, null, null, "Unsupported Types value: {0}", "Nepodporovan\u00e1 hodnota typu: {0}", "Can''t refresh the insert row.", "Nemohu obnovit vkl\u00e1dan\u00fd \u0159\u00e1dek.", null, null, "Maximum number of rows must be a value grater than or equal to 0.", "Maxim\u00e1ln\u00ed po\u010det \u0159\u00e1dek mus\u00ed b\u00fdt nez\u00e1porn\u00e9 \u010d\u00edslo.", null, null, null, null, "No value specified for parameter {0}.", "Nespecifikov\u00e1na hodnota parametru {0}.", "The array index is out of range: {0}, number of elements: {1}.", "Index pole mimo rozsah: {0}, po\u010det prvk\u016f: {1}.", "Provided InputStream failed.", "Selhal poskytnut\u00fd InputStream.", null, null, null, null, null, null, "Cannot reference a savepoint after it has been released.", "Nemohu z\u00edskat odkaz na savepoint, kdy\u017e byl uvoln\u011bn.", null, null, "An error occurred while setting up the SSL connection.", "Nastala chyba p\u0159i nastaven\u00ed SSL spojen\u00ed.", null, null, null, null, null, null, null, null, null, null, null, null, "Detail: {0}", "Detail: {0}", "This PooledConnection has already been closed.", "Tento PooledConnection byl uzav\u0159en.", "A result was returned when none was expected.", "Obdr\u017een v\u00fdsledek, ikdy\u017e \u017e\u00e1dn\u00fd nebyl o\u010dek\u00e1v\u00e1n.", null, null, "The JVM claims not to support the encoding: {0}", "JVM tvrd\u00ed, \u017ee nepodporuje kodov\u00e1n\u00ed: {0}", "The parameter index is out of range: {0}, number of parameters: {1}.", "Index parametru mimo rozsah: {0}, po\u010det parametr\u016f {1}.", "LOB positioning offsets start at 1.", "Za\u010d\u00e1tek pozicov\u00e1n\u00ed LOB za\u010d\u00edna na 1.", "{0} function takes two or three arguments.", "Funkce {0} bere dva nebo t\u0159i argumenty.", "Currently positioned after the end of the ResultSet.  You cannot call deleteRow() here.", "Pr\u00e1v\u011b jste za pozic\u00ed konce ResultSetu. Zde nem\u016f\u017eete volat deleteRow().s", null, null, "Server SQLState: {0}", "Server SQLState: {0}", null, null, "{0} function takes four and only four argument.", "Funkce {0} bere p\u0159esn\u011b \u010dty\u0159i argumenty.", "Failed to create object for: {0}.", "Selhalo vytvo\u0159en\u00ed objektu: {0}.", "No results were returned by the query.", "Neobdr\u017een \u017e\u00e1dn\u00fd v\u00fdsledek dotazu.", "Position: {0}", "Pozice: {0}", "The column index is out of range: {0}, number of columns: {1}.", "Index sloupece je mimo rozsah: {0}, po\u010det sloupc\u016f: {1}.", "Unknown Response Type {0}.", "Nezn\u00e1m\u00fd typ odpov\u011bdi {0}.", null, null, "Hint: {0}", "Rada: {0}", "Location: File: {0}, Routine: {1}, Line: {2}", "Poloha: Soubor: {0}, Rutina: {1}, \u0158\u00e1dek: {2}", "Query timeout must be a value greater than or equals to 0.", "\u010casov\u00fd limit dotazu mus\u00ed b\u00fdt nez\u00e1porn\u00e9 \u010d\u00edslo.", null, null, "Unable to translate data into the desired encoding.", "Nemohu p\u0159elo\u017eit data do po\u017eadovan\u00e9ho k\u00f3dov\u00e1n\u00ed.", null, null, "Cannot call cancelRowUpdates() when on the insert row.", "Nem\u016f\u017eete volat cancelRowUpdates() p\u0159i vkl\u00e1d\u00e1n\u00ed \u0159\u00e1dku.", "The authentication type {0} is not supported. Check that you have configured the pg_hba.conf file to include the client''s IP address or subnet, and that it is using an authentication scheme supported by the driver.", "Ov\u011b\u0159en\u00ed typu {0} nen\u00ed podporov\u00e1no. Zkontrolujte zda konfigura\u010dn\u00ed soubor pg_hba.conf obsahuje klientskou IP adresu \u010di pods\u00ed\u0165 a zda je pou\u017eit\u00e9 ov\u011b\u0159enovac\u00ed sch\u00e9ma podporov\u00e1no ovlada\u010dem.", null, null, null, null, null, null, null, null, "There are no rows in this ResultSet.", "\u017d\u00e1dn\u00fd \u0159\u00e1dek v ResultSet.", null, null, null, null };
    }
}
