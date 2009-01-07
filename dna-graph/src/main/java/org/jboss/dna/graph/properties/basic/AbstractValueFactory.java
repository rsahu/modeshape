/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.dna.graph.properties.basic;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;
import net.jcip.annotations.Immutable;
import org.jboss.dna.common.text.TextDecoder;
import org.jboss.dna.common.util.CheckArg;
import org.jboss.dna.graph.properties.Binary;
import org.jboss.dna.graph.properties.DateTime;
import org.jboss.dna.graph.properties.IoException;
import org.jboss.dna.graph.properties.Name;
import org.jboss.dna.graph.properties.Path;
import org.jboss.dna.graph.properties.PropertyType;
import org.jboss.dna.graph.properties.Reference;
import org.jboss.dna.graph.properties.ValueFactory;
import org.jboss.dna.graph.properties.ValueFormatException;

/**
 * Abstract {@link ValueFactory}.
 * 
 * @author Randall Hauch
 * @author John Verhaeg
 * @param <T> the property type
 */
@Immutable
public abstract class AbstractValueFactory<T> implements ValueFactory<T> {

    private final TextDecoder decoder;
    private final PropertyType propertyType;
    private final ValueFactory<String> stringValueFactory;

    protected AbstractValueFactory( PropertyType type,
                                    TextDecoder decoder,
                                    ValueFactory<String> stringValueFactory ) {
        CheckArg.isNotNull(type, "type");
        this.propertyType = type;
        this.decoder = decoder != null ? decoder : DEFAULT_DECODER;
        this.stringValueFactory = stringValueFactory;
    }

    /**
     * @return stringValueFactory
     */
    protected ValueFactory<String> getStringValueFactory() {
        return this.stringValueFactory;
    }

    /**
     * Get the text decoder.
     * 
     * @return the decoder
     */
    public TextDecoder getDecoder() {
        return this.decoder;
    }

    /**
     * Utility method to obtain either the supplied decoder (if not null) or this factory's {@link #getDecoder() decoder}.
     * 
     * @param decoder the decoder, which may be null if this factory's {@link #getDecoder() is to be used}
     * @return the decoder; never null
     */
    protected TextDecoder getDecoder( TextDecoder decoder ) {
        return decoder != null ? decoder : this.getDecoder();
    }

    /**
     * {@inheritDoc}
     */
    public PropertyType getPropertyType() {
        return propertyType;
    }

    /**
     * {@inheritDoc}
     */
    public T create( Object value ) {
        if (value == null) return null;
        if (value instanceof String) return create((String)value);
        if (value instanceof Integer) return create(((Integer)value).intValue());
        if (value instanceof Long) return create(((Long)value).longValue());
        if (value instanceof Double) return create(((Double)value).doubleValue());
        if (value instanceof Float) return create(((Float)value).floatValue());
        if (value instanceof Boolean) return create(((Boolean)value).booleanValue());
        if (value instanceof BigDecimal) return create((BigDecimal)value);
        if (value instanceof Calendar) return create((Calendar)value);
        if (value instanceof Date) return create((Date)value);
        if (value instanceof Name) return create((Name)value);
        if (value instanceof Path) return create((Path)value);
        if (value instanceof Reference) return create((Reference)value);
        if (value instanceof URI) return create((URI)value);
        if (value instanceof byte[]) return create((byte[])value);
        if (value instanceof InputStream) return create((InputStream)value, 0);
        if (value instanceof Reader) return create((Reader)value, 0);
        return create(value.toString());
    }

    protected abstract T[] createEmptyArray( int length );

    /**
     * {@inheritDoc}
     */
    public T[] create( BigDecimal[] values ) {
        if (values == null) return null;
        final int length = values.length;
        T[] result = createEmptyArray(length);
        for (int i = 0; i != length; ++i) {
            result[i] = create(values[i]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public T[] create( boolean[] values ) {
        if (values == null) return null;
        final int length = values.length;
        T[] result = createEmptyArray(length);
        for (int i = 0; i != length; ++i) {
            result[i] = create(values[i]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public T[] create( byte[][] values ) {
        if (values == null) return null;
        final int length = values.length;
        T[] result = createEmptyArray(length);
        for (int i = 0; i != length; ++i) {
            result[i] = create(values[i]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public T[] create( Calendar[] values ) {
        if (values == null) return null;
        final int length = values.length;
        T[] result = createEmptyArray(length);
        for (int i = 0; i != length; ++i) {
            result[i] = create(values[i]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public T[] create( Date[] values ) {
        if (values == null) return null;
        final int length = values.length;
        T[] result = createEmptyArray(length);
        for (int i = 0; i != length; ++i) {
            result[i] = create(values[i]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.jboss.dna.graph.properties.ValueFactory#create(org.jboss.dna.graph.properties.DateTime[])
     */
    public T[] create( DateTime[] values ) throws ValueFormatException {
        if (values == null) return null;
        final int length = values.length;
        T[] result = createEmptyArray(length);
        for (int i = 0; i != length; ++i) {
            result[i] = create(values[i]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public T[] create( double[] values ) {
        if (values == null) return null;
        final int length = values.length;
        T[] result = createEmptyArray(length);
        for (int i = 0; i != length; ++i) {
            result[i] = create(values[i]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public T[] create( float[] values ) {
        if (values == null) return null;
        final int length = values.length;
        T[] result = createEmptyArray(length);
        for (int i = 0; i != length; ++i) {
            result[i] = create(values[i]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public T[] create( int[] values ) {
        if (values == null) return null;
        final int length = values.length;
        T[] result = createEmptyArray(length);
        for (int i = 0; i != length; ++i) {
            result[i] = create(values[i]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public T[] create( long[] values ) {
        if (values == null) return null;
        final int length = values.length;
        T[] result = createEmptyArray(length);
        for (int i = 0; i != length; ++i) {
            result[i] = create(values[i]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public T[] create( Name[] values ) {
        if (values == null) return null;
        final int length = values.length;
        T[] result = createEmptyArray(length);
        for (int i = 0; i != length; ++i) {
            result[i] = create(values[i]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public T[] create( Object[] values ) {
        if (values == null) return null;
        final int length = values.length;
        T[] result = createEmptyArray(length);
        for (int i = 0; i != length; ++i) {
            result[i] = create(values[i]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public T[] create( Path[] values ) {
        if (values == null) return null;
        final int length = values.length;
        T[] result = createEmptyArray(length);
        for (int i = 0; i != length; ++i) {
            result[i] = create(values[i]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public T[] create( Reference[] values ) {
        if (values == null) return null;
        final int length = values.length;
        T[] result = createEmptyArray(length);
        for (int i = 0; i != length; ++i) {
            result[i] = create(values[i]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public T[] create( String[] values,
                       TextDecoder decoder ) {
        if (values == null) return null;
        final int length = values.length;
        T[] result = createEmptyArray(length);
        for (int i = 0; i != length; ++i) {
            result[i] = create(values[i], decoder);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public T[] create( String[] values ) {
        if (values == null) return null;
        final int length = values.length;
        T[] result = createEmptyArray(length);
        for (int i = 0; i != length; ++i) {
            result[i] = create(values[i]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public T[] create( URI[] values ) {
        if (values == null) return null;
        final int length = values.length;
        T[] result = createEmptyArray(length);
        for (int i = 0; i != length; ++i) {
            result[i] = create(values[i]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.jboss.dna.graph.properties.ValueFactory#create(java.util.UUID[])
     */
    public T[] create( UUID[] values ) {
        if (values == null) return null;
        final int length = values.length;
        T[] result = createEmptyArray(length);
        for (int i = 0; i != length; ++i) {
            result[i] = create(values[i]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.jboss.dna.graph.properties.ValueFactory#create(org.jboss.dna.graph.properties.Binary[])
     */
    public T[] create( Binary[] values ) throws ValueFormatException, IoException {
        if (values == null) return null;
        final int length = values.length;
        T[] result = createEmptyArray(length);
        for (int i = 0; i != length; ++i) {
            result[i] = create(values[i]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.jboss.dna.graph.properties.ValueFactory#create(java.util.Iterator)
     */
    public Iterator<T> create( Iterator<?> values ) throws ValueFormatException, IoException {
        return new ConvertingIterator<T>(values, this);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.jboss.dna.graph.properties.ValueFactory#create(java.lang.Iterable)
     */
    public Iterable<T> create( final Iterable<?> valueIterable ) throws ValueFormatException, IoException {
        return new Iterable<T>() {

            public Iterator<T> iterator() {
                return create(valueIterable.iterator());
            }
        };
    }

    protected static class ConvertingIterator<ValueType> implements Iterator<ValueType> {
        private final Iterator<?> delegate;
        private final ValueFactory<ValueType> factory;

        protected ConvertingIterator( Iterator<?> delegate,
                                      ValueFactory<ValueType> factory ) {
            assert delegate != null;
            assert factory != null;
            this.delegate = delegate;
            this.factory = factory;
        }

        /**
         * {@inheritDoc}
         * 
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        /**
         * {@inheritDoc}
         * 
         * @see java.util.Iterator#next()
         */
        public ValueType next() {
            return factory.create(this.delegate.next());
        }

        /**
         * {@inheritDoc}
         * 
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            this.delegate.remove();
        }
    }

}
