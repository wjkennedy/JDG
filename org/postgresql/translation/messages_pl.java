// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.translation;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class messages_pl extends ResourceBundle
{
    private static final String[] table;
    
    public Object handleGetObject(final String msgid) throws MissingResourceException {
        final int hash_val = msgid.hashCode() & Integer.MAX_VALUE;
        int idx = hash_val % 173 << 1;
        final Object found = messages_pl.table[idx];
        if (found == null) {
            return null;
        }
        if (msgid.equals(found)) {
            return messages_pl.table[idx + 1];
        }
        final int incr = hash_val % 171 + 1 << 1;
        while (true) {
            idx += incr;
            if (idx >= 346) {
                idx -= 346;
            }
            final Object found2 = messages_pl.table[idx];
            if (found2 == null) {
                return null;
            }
            if (msgid.equals(found2)) {
                return messages_pl.table[idx + 1];
            }
        }
    }
    
    @Override
    public Enumeration getKeys() {
        return new Enumeration() {
            private int idx = 0;
            
            {
                while (this.idx < 346 && messages_pl.table[this.idx] == null) {
                    this.idx += 2;
                }
            }
            
            @Override
            public boolean hasMoreElements() {
                return this.idx < 346;
            }
            
            @Override
            public Object nextElement() {
                final Object key = messages_pl.table[this.idx];
                do {
                    this.idx += 2;
                } while (this.idx < 346 && messages_pl.table[this.idx] == null);
                return key;
            }
        };
    }
    
    public ResourceBundle getParent() {
        return this.parent;
    }
    
    static {
        final String[] t = table = new String[] { "", "Project-Id-Version: head-pl\nReport-Msgid-Bugs-To: \nPO-Revision-Date: 2005-05-22 03:01+0200\nLast-Translator: Jaros\u0142aw Jan Pyszny <jarek@pyszny.net>\nLanguage-Team:  <pl@li.org>\nLanguage: \nMIME-Version: 1.0\nContent-Type: text/plain; charset=UTF-8\nContent-Transfer-Encoding: 8bit\nX-Generator: KBabel 1.10\nPlural-Forms:  nplurals=3; plural=(n==1 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2);\n", "The driver currently does not support COPY operations.", "Sterownik nie obs\u0142uguje aktualnie operacji COPY.", "Internal Query: {0}", "Wewn\u0119trzne Zapytanie: {0}", "There are no rows in this ResultSet.", "Nie ma \u017cadnych wierszy w tym ResultSet.", "Invalid character data was found.  This is most likely caused by stored data containing characters that are invalid for the character set the database was created in.  The most common example of this is storing 8bit data in a SQL_ASCII database.", "Znaleziono nieprawid\u0142owy znak. Najprawdopodobniej jest to spowodowane przechowywaniem w bazie znak\u00f3w, kt\u00f3re nie pasuj\u0105 do zestawu znak\u00f3w wybranego podczas tworzenia bazy danych. Najcz\u0119stszy przyk\u0142ad to przechowywanie 8-bitowych znak\u00f3w w bazie o kodowaniu SQL_ASCII.", null, null, "Fastpath call {0} - No result was returned and we expected an integer.", "Wywo\u0142anie fastpath {0} - Nie otrzymano \u017cadnego wyniku, a oczekiwano liczby ca\u0142kowitej.", "An error occurred while setting up the SSL connection.", "Wyst\u0105pi\u0142 b\u0142\u0105d podczas ustanawiania po\u0142\u0105czenia SSL.", null, null, null, null, "A CallableStatement was declared, but no call to registerOutParameter(1, <some type>) was made.", "Funkcja CallableStatement zosta\u0142a zadeklarowana, ale nie wywo\u0142ano registerOutParameter (1, <jaki\u015b typ>).", null, null, "Unexpected command status: {0}.", "Nieoczekiwany status komendy: {0}.", null, null, null, null, null, null, "A connection could not be made using the requested protocol {0}.", "Nie mo\u017cna by\u0142o nawi\u0105za\u0107 po\u0142\u0105czenia stosuj\u0105c \u017c\u0105dany protoko\u0142u {0}.", null, null, null, null, "Bad value for type {0} : {1}", "Z\u0142a warto\u015b\u0107 dla typu {0}: {1}", "Not on the insert row.", "Nie na wstawianym rekordzie.", "Premature end of input stream, expected {0} bytes, but only read {1}.", "Przedwczesny koniec strumienia wej\u015bciowego, oczekiwano {0} bajt\u00f3w, odczytano tylko {1}.", null, null, null, null, "Unknown type {0}.", "Nieznany typ {0}.", null, null, "The server does not support SSL.", "Serwer nie obs\u0142uguje SSL.", null, null, null, null, null, null, "Cannot call updateRow() when on the insert row.", "Nie mo\u017cna wywo\u0142a\u0107 updateRow() na wstawianym rekordzie.", "Where: {0}", "Gdzie: {0}", null, null, null, null, null, null, null, null, "Cannot call cancelRowUpdates() when on the insert row.", "Nie mo\u017cna wywo\u0142a\u0107 cancelRowUpdates() na wstawianym rekordzie.", null, null, null, null, null, null, null, null, "Server SQLState: {0}", "Serwer SQLState: {0}", null, null, null, null, null, null, null, null, "ResultSet is not updateable.  The query that generated this result set must select only one table, and must select all primary keys from that table. See the JDBC 2.1 API Specification, section 5.6 for more details.", "ResultSet nie jest modyfikowalny (not updateable). Zapytanie, kt\u00f3re zwr\u00f3ci\u0142o ten wynik musi dotyczy\u0107 tylko jednej tabeli oraz musi pobiera\u0107 wszystkie klucze g\u0142\u00f3wne tej tabeli. Zobacz Specyfikacj\u0119 JDBC 2.1 API, rozdzia\u0142 5.6, by uzyska\u0107 wi\u0119cej szczeg\u00f3\u0142\u00f3w.", null, null, null, null, null, null, null, null, "Cannot tell if path is open or closed: {0}.", "Nie mo\u017cna stwierdzi\u0107, czy \u015bcie\u017cka jest otwarta czy zamkni\u0119ta: {0}.", null, null, null, null, "The parameter index is out of range: {0}, number of parameters: {1}.", "Indeks parametru jest poza zakresem: {0}, liczba parametr\u00f3w: {1}.", "Unsupported Types value: {0}", "Nieznana warto\u015b\u0107 Types: {0}", "Currently positioned after the end of the ResultSet.  You cannot call deleteRow() here.", "Aktualna pozycja za ko\u0144cem ResultSet. Nie mo\u017cna wywo\u0142a\u0107 deleteRow().", "This ResultSet is closed.", "Ten ResultSet jest zamkni\u0119ty.", null, null, null, null, "Conversion of interval failed", "Konwersja typu interval nie powiod\u0142a si\u0119", "Unable to load the class {0} responsible for the datatype {1}", "Nie jest mo\u017cliwe za\u0142adowanie klasy {0} odpowiedzialnej za typ danych {1}", null, null, null, null, null, null, null, null, null, null, null, null, null, null, "Error loading default settings from driverconfig.properties", "B\u0142\u0105d podczas wczytywania ustawie\u0144 domy\u015blnych z driverconfig.properties", null, null, "The array index is out of range: {0}", "Indeks tablicy jest poza zakresem: {0}", null, null, "Unknown Types value.", "Nieznana warto\u015b\u0107 Types.", null, null, null, null, null, null, "The maximum field size must be a value greater than or equal to 0.", "Maksymalny rozmiar pola musi by\u0107 warto\u015bci\u0105 dodatni\u0105 lub 0.", null, null, null, null, null, null, null, null, null, null, null, null, "Detail: {0}", "Szczeg\u00f3\u0142y: {0}", "Unknown Response Type {0}.", "Nieznany typ odpowiedzi {0}.", "Maximum number of rows must be a value grater than or equal to 0.", "Maksymalna liczba rekord\u00f3w musi by\u0107 warto\u015bci\u0105 dodatni\u0105 lub 0.", null, null, null, null, null, null, null, null, null, null, "Query timeout must be a value greater than or equals to 0.", "Timeout zapytania musi by\u0107 warto\u015bci\u0105 dodatni\u0105 lub 0.", "Too many update results were returned.", "Zapytanie nie zwr\u00f3ci\u0142o \u017cadnych wynik\u00f3w.", null, null, "The connection attempt failed.", "Pr\u00f3ba nawi\u0105zania po\u0142\u0105czenia nie powiod\u0142a si\u0119.", null, null, null, null, null, null, "Connection has been closed automatically because a new connection was opened for the same PooledConnection or the PooledConnection has been closed.", "Po\u0142\u0105czenie zosta\u0142o zamkni\u0119te automatycznie, poniewa\u017c nowe po\u0142\u0105czenie zosta\u0142o otwarte dla tego samego PooledConnection lub PooledConnection zosta\u0142o zamkni\u0119te.", null, null, null, null, "Protocol error.  Session setup failed.", "B\u0142\u0105d protoko\u0142u. Nie uda\u0142o si\u0119 utworzy\u0107 sesji.", "This PooledConnection has already been closed.", "To PooledConnection zosta\u0142o ju\u017c zamkni\u0119te.", "DataSource has been closed.", "DataSource zosta\u0142o zamkni\u0119te.", null, null, "Method {0} is not yet implemented.", "Metoda {0}nie jest jeszcze obs\u0142ugiwana.", null, null, "Hint: {0}", "Wskaz\u00f3wka: {0}", "No value specified for parameter {0}.", "Nie podano warto\u015bci dla parametru {0}.", null, null, "Position: {0}", "Pozycja: {0}", null, null, "Cannot call deleteRow() when on the insert row.", "Nie mo\u017cna wywo\u0142a\u0107 deleteRow() na wstawianym rekordzie.", null, null, null, null, null, null, null, null, null, null, null, null, "Conversion of money failed.", "Konwersja typu money nie powiod\u0142a si\u0119.", null, null, "Internal Position: {0}", "Wewn\u0119trzna Pozycja: {0}", null, null, "Connection has been closed.", "Po\u0142\u0105czenie zosta\u0142o zamkni\u0119te.", null, null, null, null, "Currently positioned before the start of the ResultSet.  You cannot call deleteRow() here.", "Aktualna pozycja przed pocz\u0105tkiem ResultSet. Nie mo\u017cna wywo\u0142a\u0107 deleteRow().", null, null, "Failed to create object for: {0}.", "Nie powiod\u0142o si\u0119 utworzenie obiektu dla: {0}.", null, null, "Fetch size must be a value greater to or equal to 0.", "Rozmiar pobierania musi by\u0107 warto\u015bci\u0105 dodatni\u0105 lub 0.", null, null, null, null, null, null, "No results were returned by the query.", "Zapytanie nie zwr\u00f3ci\u0142o \u017cadnych wynik\u00f3w.", null, null, null, null, "The authentication type {0} is not supported. Check that you have configured the pg_hba.conf file to include the client''s IP address or subnet, and that it is using an authentication scheme supported by the driver.", "Uwierzytelnienie typu {0} nie jest obs\u0142ugiwane. Upewnij si\u0119, \u017ce skonfigurowa\u0142e\u015b plik pg_hba.conf tak, \u017ce zawiera on adres IP lub podsie\u0107 klienta oraz \u017ce u\u017cyta metoda uwierzytelnienia jest wspierana przez ten sterownik.", null, null, "Conversion to type {0} failed: {1}.", "Konwersja do typu {0} nie powiod\u0142a si\u0119: {1}.", "A result was returned when none was expected.", "Zwr\u00f3cono wynik zapytania, cho\u0107 nie by\u0142 on oczekiwany.", null, null, null, null, null, null, null, null, "Transaction isolation level {0} not supported.", "Poziom izolacji transakcji {0} nie jest obs\u0142ugiwany.", null, null, null, null, null, null, null, null, null, null, null, null, "ResultSet not positioned properly, perhaps you need to call next.", "Z\u0142a pozycja w ResultSet, mo\u017ce musisz wywo\u0142a\u0107 next.", "Location: File: {0}, Routine: {1}, Line: {2}", "Lokalizacja: Plik: {0}, Procedura: {1}, Linia: {2}", null, null, null, null, "An unexpected result was returned by a query.", "Zapytanie zwr\u00f3ci\u0142o nieoczekiwany wynik.", "The column index is out of range: {0}, number of columns: {1}.", "Indeks kolumny jest poza zakresem: {0}, liczba kolumn: {1}.", "Expected command status BEGIN, got {0}.", "Spodziewano si\u0119 statusu komendy BEGIN, otrzymano {0}.", "The fastpath function {0} is unknown.", "Funkcja fastpath {0} jest nieznana.", null, null, "The server requested password-based authentication, but no password was provided.", "Serwer za\u017c\u0105da\u0142 uwierzytelnienia opartego na ha\u015ble, ale \u017cadne has\u0142o nie zosta\u0142o dostarczone.", null, null, null, null, null, null, "The array index is out of range: {0}, number of elements: {1}.", "Indeks tablicy jest poza zakresem: {0}, liczba element\u00f3w: {1}.", null, null, null, null, "Something unusual has occurred to cause the driver to fail. Please report this exception.", "Co\u015b niezwyk\u0142ego spowodowa\u0142o pad sterownika. Prosz\u0119, zg\u0142o\u015b ten wyj\u0105tek.", null, null, "Zero bytes may not occur in string parameters.", "Zerowe bajty nie mog\u0105 pojawia\u0107 si\u0119 w parametrach typu \u0142a\u0144cuch znakowy.", null, null };
    }
}
