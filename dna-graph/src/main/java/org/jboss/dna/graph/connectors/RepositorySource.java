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
package org.jboss.dna.graph.connectors;

import java.io.Serializable;
import javax.naming.Referenceable;
import org.jboss.dna.graph.ExecutionContext;
import org.jboss.dna.graph.requests.CacheableRequest;

/**
 * A repository source is a description of a resource that can be used to access or store repository information. This class
 * serves as a factory for {@link RepositoryConnection} instances and provides some basic configuration information.
 * <p>
 * Typically this interface is implemented by classes that provide standard-style getters and setters for the various properties
 * necessary for proper configuration via reflection or introspection. This interface expects nor defines any such properties,
 * leaving that entirely to the implementation classes.
 * </p>
 * <p>
 * Implementations should also provide a no-arg constructor so that it is possible to easily create instances and initialize using
 * the standard getters and setters. One example where this is required is when a RepositorySource instance is recorded in a
 * repository (e.g., in a configuration area), and needs to be reinstantiated.
 * </p>
 * <p>
 * Objects that implement this <code>RepositorySource</code> interface are typically registered with a naming service such as Java
 * Naming and Directory Interface<sup><font size=-3>TM</font></sup> (JNDI). This interface extends both {@link Referenceable} and
 * {@link Serializable} so that such objects can be stored in any JNDI naming context and enable proper system recovery,
 * </p>
 * <h3>Pooling connections</h3>
 * <p>
 * If the connections created by a RepositorySource are expensive to create, then connection pooling is recommended. DNA provides
 * this capability with a powerful and flexible {@link RepositoryConnectionPool} class. This is the pooling mechanism used by
 * JBoss DNA, but you are free to use your own pools.
 * </p>
 * <h3>Cache Policy</h3>
 * <p>
 * Each connector is responsible for determining whether and how long DNA is to cache the content made available by the connector.
 * This is referred to as the caching policy, and consists of a time to live value representing the number of milliseconds that a
 * piece of data may be cached. After the TTL has passed, the information is no longer used.
 * </p>
 * <p>
 * DNA allows a connector to use a flexible and powerful caching policy. First, each connection returns the default caching policy
 * for all information returned by that connection. Often this policy can be configured via properties on the
 * {@link RepositorySource} implementation. This is optional, meaning the connector can return null if it does not wish to have a
 * default caching policy.
 * </p>
 * <p>
 * Second, the connector is able to override its default caching policy on {@link CacheableRequest individual requests}. Again,
 * this is optional, meaning that a null caching policy on a request implies that the request has no overridden caching policy.
 * </p>
 * <p>
 * Third, if the connector has no default caching policy and none is set on the individual requests, DNA uses whatever caching
 * policy is set up for that component using the connector. For example, the federating connector allows a default caching policy
 * to be specified, and this policy is used should the sources being federated not define their own caching policy.
 * </p>
 * <p>
 * In summary, a connector has total control over whether and for how long the information it provides is cached.
 * </p>
 * <h3>Leveraging JNDI</h3>
 * <p>
 * Sometimes it is necessary (or easier) for a RepositorySource implementation to look up an object in JNDI. One example of this
 * is the JBoss Cache connector: while the connector can instantiate a new JBoss Cache instance, more interesting use cases
 * involve JBoss Cache instances that are set up for clustering and replication, something that is generally difficult to
 * configure in a single JavaBean. Therefore the JBossCacheSource has optional JavaBean properties that define how it is to look
 * up a JBoss Cache instance in JNDI.
 * </p>
 * <p>
 * This is a simple pattern that you may find useful in your connector. Basically, if your source implementation can look up an
 * object in JNDI, simply use a single JavaBean String property that defines the full name that should be used to locate that
 * object in JNDI. Usually it's best to include "Jndi" in the JavaBean property name so that administrative users understand the
 * purpose of the property. (And some may suggest that any optional property also use the word "optional" in the property name.)
 * </p>
 * <h3>Capabilities</h3>
 * <p>
 * Each RepositorySource implementation provides some hint as to its capabilities by returning a
 * {@link RepositorySourceCapabilities} object. This class currently provides methods that say whether the connector supports
 * updates, whether it supports same-name-siblings (SNS), and whether the connector supports listeners and events. These may be
 * hard-coded values, or the capabilities object {@link #getCapabilities() returned by the connector} may determine them at
 * runtime based upon the system its connecting to. For example, a connector may interrogate the underlying system to decide
 * whether it can support updates. The only criteria is that the capabilities must remain constant throughout the lifetime of the
 * RepositorySource instance (assuming it doesn't change).
 * </p>
 * <p>
 * The {@link RepositorySourceCapabilities} can be used as is (the class is immutable), or it can be subclassed to provide more
 * complex behavior. Why is this a concrete class and not an interface? By using a concrete class, connectors inherit the default
 * behavior. If additional capabilities need to be added to the class in future releases, connectors may not have to override the
 * defaults. This provides some insulation against future enhancements to the connector framework.
 * </p>
 * <h3>Security and authentication</h3>
 * <p>
 * The main method connectors have to process requests takes an {@link ExecutionContext}, which contains the JAAS security
 * information of the subject performing the request. This means that the connector can use this to determine authentication and
 * authorization information for each request.
 * </p>
 * <p>
 * Sometimes that is not sufficient. For example, it may be that the connector needs its own authorization information so that it
 * can establish a connection (even if user-level privileges still use the {@link ExecutionContext} provided with each request).
 * In this case, the RepositorySource implementation will probably need JavaBean properties that represent the connector's
 * authentication information. This may take the form of a username and password, or it may be properties that are used to
 * delegate authentication to JAAS. Either way, just realize that it's perfectly acceptable for the connector to require its own
 * security properties.
 * </p>
 * 
 * @author Randall Hauch
 */
public interface RepositorySource extends Referenceable, Serializable {

    /**
     * Initialize this source to use the supplied {@link RepositoryContext}, from which this source can obtain
     * {@link RepositoryContext#getRepositoryConnectionFactory() connections} to other {@link RepositorySource sources} as well as
     * {@link RepositoryContext#getExecutionContextFactory() execution contexts}.
     * 
     * @param context
     * @throws RepositorySourceException
     */
    void initialize( RepositoryContext context ) throws RepositorySourceException;

    /**
     * Get the name for this repository source.
     * 
     * @return the name; never null or empty
     */
    String getName();

    /**
     * Get a connection from this source.
     * 
     * @return a connection
     * @throws RepositorySourceException if there is a problem obtaining a connection
     * @throws IllegalStateException if the factory is not in a state to create or return connections
     */
    RepositoryConnection getConnection() throws RepositorySourceException;

    /**
     * Get the maximum number of retries that may be performed on a given operation when using {@link #getConnection()
     * connections} created by this source. This value does not constitute a minimum number of retries; in fact, the connection
     * user is not required to retry any operations.
     * 
     * @return the maximum number of allowable retries, or 0 if the source has no limit
     */
    int getRetryLimit();

    /**
     * Set the maximum number of retries that may be performed on a given operation when using {@link #getConnection()
     * connections} created by this source. This value does not constitute a minimum number of retries; in fact, the connection
     * user is not required to retry any operations.
     * 
     * @param limit the maximum number of allowable retries, or 0 if the source has no limit
     */
    void setRetryLimit( int limit );

    /**
     * Get the capabilities for this source.
     * 
     * @return the capabilities for this source; never null
     */
    RepositorySourceCapabilities getCapabilities();

}
