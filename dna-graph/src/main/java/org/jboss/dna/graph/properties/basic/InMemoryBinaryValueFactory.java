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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import net.jcip.annotations.Immutable;
import org.jboss.dna.common.text.TextDecoder;
import org.jboss.dna.common.util.IoUtil;
import org.jboss.dna.graph.GraphI18n;
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
 * Teh standard {@link ValueFactory} for {@link PropertyType#BINARY} values.
 * 
 * @author Randall Hauch
 * @author John Verhaeg
 */
@Immutable
public class InMemoryBinaryValueFactory extends AbstractValueFactory<Binary> {

    private static final String CHAR_SET_NAME = "UTF-8";

    public InMemoryBinaryValueFactory( TextDecoder decoder,
                                       ValueFactory<String> stringValueFactory ) {
        super(PropertyType.BINARY, decoder, stringValueFactory);
    }

    /**
     * {@inheritDoc}
     */
    public Binary create( String value ) {
        if (value == null) return null;
        try {
            return create(value.getBytes(CHAR_SET_NAME));
        } catch (UnsupportedEncodingException err) {
            throw new ValueFormatException(value, getPropertyType(),
                                           GraphI18n.errorConvertingType.text(String.class.getSimpleName(),
                                                                            Binary.class.getSimpleName(),
                                                                            value), err);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Binary create( String value,
                          TextDecoder decoder ) {
        if (value == null) return null;
        return create(getDecoder(decoder).decode(value));
    }

    /**
     * {@inheritDoc}
     */
    public Binary create( int value ) {
        // Convert the value to a string, then to a binary ...
        return create(this.getStringValueFactory().create(value));
    }

    /**
     * {@inheritDoc}
     */
    public Binary create( long value ) {
        // Convert the value to a string, then to a binary ...
        return create(this.getStringValueFactory().create(value));
    }

    /**
     * {@inheritDoc}
     */
    public Binary create( boolean value ) {
        // Convert the value to a string, then to a binary ...
        return create(this.getStringValueFactory().create(value));
    }

    /**
     * {@inheritDoc}
     */
    public Binary create( float value ) {
        // Convert the value to a string, then to a binary ...
        return create(this.getStringValueFactory().create(value));
    }

    /**
     * {@inheritDoc}
     */
    public Binary create( double value ) {
        // Convert the value to a string, then to a binary ...
        return create(this.getStringValueFactory().create(value));
    }

    /**
     * {@inheritDoc}
     */
    public Binary create( BigDecimal value ) {
        // Convert the value to a string, then to a binary ...
        return create(this.getStringValueFactory().create(value));
    }

    /**
     * {@inheritDoc}
     */
    public Binary create( Calendar value ) {
        // Convert the value to a string, then to a binary ...
        return create(this.getStringValueFactory().create(value));
    }

    /**
     * {@inheritDoc}
     */
    public Binary create( Date value ) {
        // Convert the value to a string, then to a binary ...
        return create(this.getStringValueFactory().create(value));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.jboss.dna.graph.properties.ValueFactory#create(org.jboss.dna.graph.properties.DateTime)
     */
    public Binary create( DateTime value ) throws ValueFormatException {
        // Convert the value to a string, then to a binary ...
        return create(this.getStringValueFactory().create(value));
    }

    /**
     * {@inheritDoc}
     */
    public Binary create( Name value ) {
        // Convert the value to a string, then to a binary ...
        return create(this.getStringValueFactory().create(value));
    }

    /**
     * {@inheritDoc}
     */
    public Binary create( Path value ) {
        // Convert the value to a string, then to a binary ...
        return create(this.getStringValueFactory().create(value));
    }

    /**
     * {@inheritDoc}
     */
    public Binary create( Reference value ) {
        // Convert the value to a string, then to a binary ...
        return create(this.getStringValueFactory().create(value));
    }

    /**
     * {@inheritDoc}
     */
    public Binary create( URI value ) {
        // Convert the value to a string, then to a binary ...
        return create(this.getStringValueFactory().create(value));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.jboss.dna.graph.properties.ValueFactory#create(java.util.UUID)
     */
    public Binary create( UUID value ) {
        // Convert the value to a string, then to a binary ...
        return create(this.getStringValueFactory().create(value));
    }

    /**
     * {@inheritDoc}
     */
    public Binary create( byte[] value ) {
        return new InMemoryBinary(value);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.jboss.dna.graph.properties.ValueFactory#create(org.jboss.dna.graph.properties.Binary)
     */
    public Binary create( Binary value ) throws ValueFormatException, IoException {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    public Binary create( InputStream stream,
                          long approximateLength ) throws IoException {
        if (stream == null) return null;
        try {
            byte[] value = IoUtil.readBytes(stream);
            return create(value);
        } catch (IOException err) {
            throw new IoException(
                                  GraphI18n.errorConvertingIo.text(InputStream.class.getSimpleName(), Binary.class.getSimpleName()),
                                  err);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Binary create( Reader reader,
                          long approximateLength ) throws IoException {
        if (reader == null) return null;
        // Convert the value to a string, then to a binary ...
        try {
            String value = IoUtil.read(reader);
            return create(this.getStringValueFactory().create(value));
        } catch (IOException err) {
            throw new IoException(GraphI18n.errorConvertingIo.text(Reader.class.getSimpleName(), Binary.class.getSimpleName()), err);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Binary[] createEmptyArray( int length ) {
        return new Binary[length];
    }
}
