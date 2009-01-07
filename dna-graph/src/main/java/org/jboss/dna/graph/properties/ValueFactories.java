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
package org.jboss.dna.graph.properties;

import java.math.BigDecimal;
import java.net.URI;

/**
 * The set of standard {@link ValueFactory} instances.
 * 
 * @author Randall Hauch
 */
public interface ValueFactories extends Iterable<ValueFactory<?>> {

    /**
     * Get the value factory that creates values of the supplied {@link PropertyType type}.
     * 
     * @param type the type for the values
     * @return the factory; never null
     * @throws IllegalArgumentException if the property type is null
     */
    ValueFactory<?> getValueFactory( PropertyType type );

    /**
     * Get the value factory that is best able to create values with the most natural type given by the supplied value.
     * 
     * @param prototype the value that should be used to determine the best value factory
     * @return the factory; never null
     * @throws IllegalArgumentException if the prototype value is null
     */
    ValueFactory<?> getValueFactory( Object prototype );

    /**
     * Get the value factory for {@link PropertyType#STRING string} properties.
     * 
     * @return the factory; never null
     */
    ValueFactory<String> getStringFactory();

    /**
     * Get the value factory for {@link PropertyType#BINARY binary} properties.
     * 
     * @return the factory; never null
     */
    ValueFactory<Binary> getBinaryFactory();

    /**
     * Get the value factory for {@link PropertyType#LONG long} properties.
     * 
     * @return the factory; never null
     */
    ValueFactory<Long> getLongFactory();

    /**
     * Get the value factory for {@link PropertyType#DOUBLE double} properties.
     * 
     * @return the factory; never null
     */
    ValueFactory<Double> getDoubleFactory();

    /**
     * Get the value factory for {@link PropertyType#DECIMAL decimal} properties.
     * 
     * @return the factory; never null
     */
    ValueFactory<BigDecimal> getDecimalFactory();

    /**
     * Get the value factory for {@link PropertyType#DATE date} properties.
     * 
     * @return the factory; never null
     */
    DateTimeFactory getDateFactory();

    /**
     * Get the value factory for {@link PropertyType#BOOLEAN boolean} properties.
     * 
     * @return the factory; never null
     */
    ValueFactory<Boolean> getBooleanFactory();

    /**
     * Get the value factory for {@link PropertyType#NAME name} properties.
     * 
     * @return the factory; never null
     */
    NameFactory getNameFactory();

    /**
     * Get the value factory for {@link PropertyType#REFERENCE reference} properties.
     * 
     * @return the factory; never null
     */
    ValueFactory<Reference> getReferenceFactory();

    /**
     * Get the value factory for {@link PropertyType#PATH path} properties.
     * 
     * @return the factory; never null
     */
    PathFactory getPathFactory();

    /**
     * Get the value factory for {@link PropertyType#URI URI} properties.
     * 
     * @return the factory; never null
     */
    ValueFactory<URI> getUriFactory();

    /**
     * Get the value factory for {@link PropertyType#UUID UUID} properties.
     * 
     * @return the factory; never null
     */
    UuidFactory getUuidFactory();

    /**
     * Get the value factory for {@link PropertyType#OBJECT object} properties.
     * 
     * @return the factory; never null
     */
    ValueFactory<Object> getObjectFactory();

}
