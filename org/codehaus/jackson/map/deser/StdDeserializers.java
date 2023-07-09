// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser;

import java.util.Iterator;
import org.codehaus.jackson.map.deser.std.JavaTypeDeserializer;
import org.codehaus.jackson.map.deser.std.TokenBufferDeserializer;
import org.codehaus.jackson.map.deser.std.AtomicBooleanDeserializer;
import org.codehaus.jackson.map.deser.std.FromStringDeserializer;
import org.codehaus.jackson.map.deser.std.TimestampDeserializer;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.codehaus.jackson.map.deser.std.DateDeserializer;
import org.codehaus.jackson.map.deser.std.CalendarDeserializer;
import org.codehaus.jackson.map.deser.std.ClassDeserializer;
import org.codehaus.jackson.map.deser.std.StringDeserializer;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.codehaus.jackson.map.deser.std.UntypedObjectDeserializer;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.type.ClassKey;
import java.util.HashMap;

class StdDeserializers
{
    final HashMap<ClassKey, JsonDeserializer<Object>> _deserializers;
    
    private StdDeserializers() {
        this._deserializers = new HashMap<ClassKey, JsonDeserializer<Object>>();
        this.add(new UntypedObjectDeserializer());
        final StdDeserializer<?> strDeser = new StringDeserializer();
        this.add(strDeser, String.class);
        this.add(strDeser, CharSequence.class);
        this.add(new ClassDeserializer());
        this.add(new StdDeserializer.BooleanDeserializer(Boolean.class, null));
        this.add(new StdDeserializer.ByteDeserializer(Byte.class, null));
        this.add(new StdDeserializer.ShortDeserializer(Short.class, null));
        this.add(new StdDeserializer.CharacterDeserializer(Character.class, null));
        this.add(new StdDeserializer.IntegerDeserializer(Integer.class, null));
        this.add(new StdDeserializer.LongDeserializer(Long.class, null));
        this.add(new StdDeserializer.FloatDeserializer(Float.class, null));
        this.add(new StdDeserializer.DoubleDeserializer(Double.class, null));
        this.add(new StdDeserializer.BooleanDeserializer(Boolean.TYPE, Boolean.FALSE));
        this.add(new StdDeserializer.ByteDeserializer(Byte.TYPE, (Byte)0));
        this.add(new StdDeserializer.ShortDeserializer(Short.TYPE, (Short)0));
        this.add(new StdDeserializer.CharacterDeserializer(Character.TYPE, '\0'));
        this.add(new StdDeserializer.IntegerDeserializer(Integer.TYPE, 0));
        this.add(new StdDeserializer.LongDeserializer(Long.TYPE, 0L));
        this.add(new StdDeserializer.FloatDeserializer(Float.TYPE, 0.0f));
        this.add(new StdDeserializer.DoubleDeserializer(Double.TYPE, 0.0));
        this.add(new StdDeserializer.NumberDeserializer());
        this.add(new StdDeserializer.BigDecimalDeserializer());
        this.add(new StdDeserializer.BigIntegerDeserializer());
        this.add(new CalendarDeserializer());
        this.add(new DateDeserializer());
        this.add(new CalendarDeserializer(GregorianCalendar.class), GregorianCalendar.class);
        this.add(new StdDeserializer.SqlDateDeserializer());
        this.add(new TimestampDeserializer());
        for (final StdDeserializer<?> deser : FromStringDeserializer.all()) {
            this.add(deser);
        }
        this.add(new StdDeserializer.StackTraceElementDeserializer());
        this.add(new AtomicBooleanDeserializer());
        this.add(new TokenBufferDeserializer());
        this.add(new JavaTypeDeserializer());
    }
    
    public static HashMap<ClassKey, JsonDeserializer<Object>> constructAll() {
        return new StdDeserializers()._deserializers;
    }
    
    private void add(final StdDeserializer<?> stdDeser) {
        this.add(stdDeser, stdDeser.getValueClass());
    }
    
    private void add(final StdDeserializer<?> stdDeser, final Class<?> valueClass) {
        final JsonDeserializer<Object> deser = (JsonDeserializer<Object>)stdDeser;
        this._deserializers.put(new ClassKey(valueClass), deser);
    }
}
