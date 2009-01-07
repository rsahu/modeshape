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
package org.jboss.dna.common.i18n;

import java.net.URL;
import java.util.Locale;

/**
 * Implementation of a {@link LocalizationRepository} that loads a properties file from the classpath of the supplied
 * {@link ClassLoader class loader}.
 * <p>
 * This repository for a property file by building locations of the form "path/to/class_locale.properties", where "path/to/class"
 * is created from the fully-qualified classname and all "." replaced with "/" characters, "locale" is the a variant of the locale
 * (first the full locale, then subsequently with the last segment removed). As soon as a property file is found, its URL is
 * returned immediately.
 * </p>
 * named with a name that matches
 * @author Randall Hauch
 */
public class ClasspathLocalizationRepository implements LocalizationRepository {

    private final ClassLoader classLoader;

    /**
     * Create a repository using the current thread's {@link Thread#getContextClassLoader() context class loader} or, if that is
     * null, the same class loader that loaded this class.
     */
    public ClasspathLocalizationRepository() {
        this(null);
    }

    /**
     * Create a repository using the supplied class loader. Null may be passed if the class loader should be obtained from the
     * current thread's {@link Thread#getContextClassLoader() context class loader} or, if that is null, the same class loader
     * that loaded this class.
     * @param classLoader the class loader to use; may be null
     */
    public ClasspathLocalizationRepository( ClassLoader classLoader ) {
        if (classLoader == null) classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) classLoader = this.getClass().getClassLoader();
        this.classLoader = classLoader;
    }

    /**
     * {@inheritDoc}
     */
    public URL getLocalizationBundle( String bundleName, Locale locale ) {
        URL url = null;
        String pathPfx = bundleName.replaceAll("\\.", "/");
        String variant = '_' + locale.toString();
        do {
            url = this.classLoader.getResource(pathPfx + variant + ".properties");
            if (url == null) {
                int ndx = variant.lastIndexOf('_');
                if (ndx < 0) {
                    break;
                }
                variant = variant.substring(0, ndx);
            }
        } while (url == null);
        return url;
    }

}
