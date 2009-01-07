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
package org.jboss.dna.connector.jbosscache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.transaction.xa.XAResource;
import org.jboss.cache.Cache;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.dna.graph.DnaLexicon;
import org.jboss.dna.graph.ExecutionContext;
import org.jboss.dna.graph.Location;
import org.jboss.dna.graph.cache.CachePolicy;
import org.jboss.dna.graph.connectors.RepositoryConnection;
import org.jboss.dna.graph.connectors.RepositorySourceException;
import org.jboss.dna.graph.connectors.RepositorySourceListener;
import org.jboss.dna.graph.properties.Name;
import org.jboss.dna.graph.properties.Path;
import org.jboss.dna.graph.properties.PathFactory;
import org.jboss.dna.graph.properties.PathNotFoundException;
import org.jboss.dna.graph.properties.Property;
import org.jboss.dna.graph.properties.PropertyFactory;
import org.jboss.dna.graph.properties.ValueFactory;
import org.jboss.dna.graph.properties.Path.Segment;
import org.jboss.dna.graph.requests.CopyBranchRequest;
import org.jboss.dna.graph.requests.CreateNodeRequest;
import org.jboss.dna.graph.requests.DeleteBranchRequest;
import org.jboss.dna.graph.requests.MoveBranchRequest;
import org.jboss.dna.graph.requests.ReadAllChildrenRequest;
import org.jboss.dna.graph.requests.ReadAllPropertiesRequest;
import org.jboss.dna.graph.requests.Request;
import org.jboss.dna.graph.requests.UpdatePropertiesRequest;
import org.jboss.dna.graph.requests.processor.RequestProcessor;

/**
 * The repository connection to a JBoss Cache instance.
 * 
 * @author Randall Hauch
 */
public class JBossCacheConnection implements RepositoryConnection {

    protected static final RepositorySourceListener NO_OP_LISTENER = new RepositorySourceListener() {

        /**
         * {@inheritDoc}
         */
        public void notify( String sourceName,
                            Object... events ) {
            // do nothing
        }
    };

    private final JBossCacheSource source;
    private final Cache<Name, Object> cache;
    private RepositorySourceListener listener = NO_OP_LISTENER;

    JBossCacheConnection( JBossCacheSource source,
                          Cache<Name, Object> cache ) {
        assert source != null;
        assert cache != null;
        this.source = source;
        this.cache = cache;
    }

    /**
     * @return cache
     */
    /*package*/Cache<Name, Object> getCache() {
        return cache;
    }

    /**
     * {@inheritDoc}
     */
    public String getSourceName() {
        return source.getName();
    }

    /**
     * {@inheritDoc}
     */
    public CachePolicy getDefaultCachePolicy() {
        return source.getDefaultCachePolicy();
    }

    /**
     * {@inheritDoc}
     */
    public XAResource getXAResource() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean ping( long time,
                         TimeUnit unit ) {
        this.cache.getRoot();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void setListener( RepositorySourceListener listener ) {
        this.listener = listener != null ? listener : NO_OP_LISTENER;
    }

    /**
     * {@inheritDoc}
     */
    public void close() {
        // do nothing
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.jboss.dna.graph.connectors.RepositoryConnection#execute(org.jboss.dna.graph.ExecutionContext,
     *      org.jboss.dna.graph.requests.Request)
     */
    public void execute( final ExecutionContext context,
                         final Request request ) throws RepositorySourceException {
        final PathFactory pathFactory = context.getValueFactories().getPathFactory();
        final PropertyFactory propertyFactory = context.getPropertyFactory();
        final ValueFactory<UUID> uuidFactory = context.getValueFactories().getUuidFactory();
        RequestProcessor processor = new RequestProcessor(getSourceName(), context) {
            @Override
            public void process( ReadAllChildrenRequest request ) {
                Path nodePath = request.of().getPath();
                Node<Name, Object> node = getNode(context, nodePath);
                // Get the names of the children, using the child list ...
                Path.Segment[] childList = (Path.Segment[])node.get(JBossCacheLexicon.CHILD_PATH_SEGMENT_LIST);
                for (Path.Segment child : childList) {
                    // We have the child segment, but we need the UUID property ...
                    Node<Name, Object> childNode = node.getChild(child);
                    Object uuid = childNode.get(DnaLexicon.UUID);
                    if (uuid == null) {
                        uuid = generateUuid();
                        childNode.put(DnaLexicon.UUID, uuid);
                    } else {
                        uuid = uuidFactory.create(uuid);
                    }
                    Property uuidProperty = propertyFactory.create(DnaLexicon.UUID, uuid);
                    request.addChild(pathFactory.create(nodePath, child), uuidProperty);
                }
                UUID uuid = uuidFactory.create(node.get(DnaLexicon.UUID));
                request.setActualLocationOfNode(new Location(nodePath, uuid));
            }

            @Override
            public void process( ReadAllPropertiesRequest request ) {
                Path nodePath = request.at().getPath();
                Node<Name, Object> node = getNode(context, nodePath);
                Map<Name, Object> dataMap = node.getData();
                for (Map.Entry<Name, Object> data : dataMap.entrySet()) {
                    Name propertyName = data.getKey();
                    // Don't allow the child list property to be accessed
                    if (propertyName.equals(JBossCacheLexicon.CHILD_PATH_SEGMENT_LIST)) continue;
                    Object values = data.getValue();
                    Property property = propertyFactory.create(propertyName, values);
                    request.addProperty(property);
                }
                UUID uuid = uuidFactory.create(node.get(DnaLexicon.UUID));
                request.setActualLocationOfNode(new Location(nodePath, uuid));
            }

            @Override
            public void process( CreateNodeRequest request ) {
                Path parent = request.under().getPath();
                // Look up the parent node, which must exist ...
                Node<Name, Object> parentNode = getNode(context, parent);

                // Update the children to account for same-name siblings.
                // This not only updates the FQN of the child nodes, but it also sets the property that stores the
                // the array of Path.Segment for the children (since the cache doesn't maintain order).
                Path.Segment newSegment = updateChildList(parentNode, request.named(), getExecutionContext(), true);
                Node<Name, Object> node = parentNode.addChild(Fqn.fromElements(newSegment));
                assert checkChildren(parentNode);

                // Add the UUID property (if required), which may be overwritten by a supplied property ...
                node.put(DnaLexicon.UUID, generateUuid());
                // Now add the properties to the supplied node ...
                for (Property property : request.properties()) {
                    if (property.size() == 0) continue;
                    Name propName = property.getName();
                    Object value = null;
                    if (property.size() == 1) {
                        value = property.iterator().next();
                    } else {
                        value = property.getValuesAsArray();
                    }
                    node.put(propName, value);
                }
                UUID uuid = uuidFactory.create(node.get(DnaLexicon.UUID));
                Path nodePath = pathFactory.create(parent, newSegment);
                request.setActualLocationOfNode(new Location(nodePath, uuid));
            }

            @Override
            public void process( UpdatePropertiesRequest request ) {
                Path nodePath = request.on().getPath();
                Node<Name, Object> node = getNode(context, nodePath);
                // Now set (or remove) the properties to the supplied node ...
                for (Property property : request.properties()) {
                    Name propName = property.getName();
                    // Don't allow the child list property to be removed or changed
                    if (propName.equals(JBossCacheLexicon.CHILD_PATH_SEGMENT_LIST)) continue;
                    if (property.size() == 0) {
                        node.remove(propName);
                        continue;
                    }
                    Object value = null;
                    if (property.size() == 1) {
                        value = property.iterator().next();
                    } else {
                        value = property.getValuesAsArray();
                    }
                    node.put(propName, value);
                }
                UUID uuid = uuidFactory.create(node.get(DnaLexicon.UUID));
                request.setActualLocationOfNode(new Location(nodePath, uuid));
            }

            @Override
            public void process( CopyBranchRequest request ) {
                Path nodePath = request.from().getPath();
                Node<Name, Object> node = getNode(context, nodePath);
                // Look up the new parent, which must exist ...
                Path newParentPath = request.into().getPath();
                Node<Name, Object> newParent = getNode(context, newParentPath);
                Path.Segment newSegment = copyNode(node, newParent, true, null, null, getExecutionContext());

                UUID uuid = uuidFactory.create(node.get(DnaLexicon.UUID));
                Path newPath = pathFactory.create(newParentPath, newSegment);
                request.setActualLocations(new Location(nodePath, uuid), new Location(newPath, uuid));
            }

            @Override
            public void process( DeleteBranchRequest request ) {
                Path nodePath = request.at().getPath();
                Node<Name, Object> node = getNode(context, nodePath);
                node.getParent().removeChild(node.getFqn().getLastElement());
                UUID uuid = uuidFactory.create(node.get(DnaLexicon.UUID));
                request.setActualLocationOfNode(new Location(nodePath, uuid));
            }

            @Override
            public void process( MoveBranchRequest request ) {
                Path nodePath = request.from().getPath();
                Node<Name, Object> node = getNode(context, nodePath);
                boolean recursive = true;
                // Look up the new parent, which must exist ...
                Path newParentPath = request.into().getPath();
                Node<Name, Object> newParent = getNode(context, newParentPath);
                Path.Segment newSegment = copyNode(node, newParent, recursive, DnaLexicon.UUID, null, getExecutionContext());

                // Now delete the old node ...
                Node<Name, Object> oldParent = node.getParent();
                boolean removed = oldParent.removeChild(node.getFqn().getLastElement());
                assert removed;

                UUID uuid = uuidFactory.create(node.get(DnaLexicon.UUID));
                Path newPath = pathFactory.create(newParentPath, newSegment);
                request.setActualLocations(new Location(nodePath, uuid), new Location(newPath, uuid));
            }
        };
        processor.process(request);
    }

    /**
     * @return listener
     */
    protected RepositorySourceListener getListener() {
        return this.listener;
    }

    protected Fqn<?> getFullyQualifiedName( Path path ) {
        assert path != null;
        return Fqn.fromList(path.getSegmentsList());
    }

    /**
     * Get a relative fully-qualified name that consists only of the supplied segment.
     * 
     * @param pathSegment the segment from which the fully qualified name is to be created
     * @return the relative fully-qualified name
     */
    protected Fqn<?> getFullyQualifiedName( Path.Segment pathSegment ) {
        assert pathSegment != null;
        return Fqn.fromElements(pathSegment);
    }

    @SuppressWarnings( "unchecked" )
    protected Path getPath( PathFactory factory,
                            Fqn<?> fqn ) {
        List<Path.Segment> segments = (List<Path.Segment>)fqn.peekElements();
        return factory.create(factory.createRootPath(), segments);
    }

    protected Node<Name, Object> getNode( ExecutionContext context,
                                          Path path ) {
        // Look up the node with the supplied path ...
        Fqn<?> fqn = getFullyQualifiedName(path);
        Node<Name, Object> node = cache.getNode(fqn);
        if (node == null) {
            String nodePath = path.getString(context.getNamespaceRegistry());
            Path lowestExisting = null;
            while (fqn != null) {
                fqn = fqn.getParent();
                node = cache.getNode(fqn);
                if (node != null) {
                    lowestExisting = getPath(context.getValueFactories().getPathFactory(), fqn);
                    fqn = null;
                }
            }
            throw new PathNotFoundException(new Location(path), lowestExisting,
                                            JBossCacheConnectorI18n.nodeDoesNotExist.text(nodePath));
        }
        return node;

    }

    protected UUID generateUuid() {
        return UUID.randomUUID();
    }

    protected Path.Segment copyNode( Node<Name, Object> original,
                                     Node<Name, Object> newParent,
                                     boolean recursive,
                                     Name uuidProperty,
                                     AtomicInteger count,
                                     ExecutionContext context ) {
        assert original != null;
        assert newParent != null;
        // Get or create the new node ...
        Segment name = (Segment)original.getFqn().getLastElement();

        // Update the children to account for same-name siblings.
        // This not only updates the FQN of the child nodes, but it also sets the property that stores the
        // the array of Path.Segment for the children (since the cache doesn't maintain order).
        Path.Segment newSegment = updateChildList(newParent, name.getName(), context, true);
        Node<Name, Object> copy = newParent.addChild(getFullyQualifiedName(newSegment));
        assert checkChildren(newParent);
        // Copy the properties ...
        copy.clearData();
        copy.putAll(original.getData());
        if (uuidProperty != null) {
            // Generate a new UUID for the new node, overwriting any existing value from the original ...
            copy.put(uuidProperty, generateUuid());
        }
        if (count != null) count.incrementAndGet();
        if (recursive) {
            // Loop over each child and call this method ...
            for (Node<Name, Object> child : original.getChildren()) {
                copyNode(child, copy, true, uuidProperty, count, context);
            }
        }
        return newSegment;
    }

    /**
     * Update (or create) the array of {@link Path.Segment path segments} for the children of the supplied node. This array
     * maintains the ordered list of children (since the {@link Cache} does not maintain the order). Invoking this method will
     * change any existing children that a {@link Path.Segment#getName() name part} that matches the supplied
     * <code>changedName</code> to have the appropriate {@link Path.Segment#getIndex() same-name sibling index}.
     * 
     * @param parent the parent node; may not be null
     * @param changedName the name that should be compared to the existing node siblings to determine whether the same-name
     *        sibling indexes should be updated; may not be null
     * @param context the execution context; may not be null
     * @param addChildWithName true if a new child with the supplied name is to be added to the children (but which does not yet
     *        exist in the node's children)
     * @return the path segment for the new child, or null if <code>addChildWithName</code> was false
     */
    protected Path.Segment updateChildList( Node<Name, Object> parent,
                                            Name changedName,
                                            ExecutionContext context,
                                            boolean addChildWithName ) {
        assert parent != null;
        assert changedName != null;
        assert context != null;
        Set<Node<Name, Object>> children = parent.getChildren();
        if (children.isEmpty() && !addChildWithName) return null;

        // Go through the children, looking for any children with the same name as the 'changedName'
        List<ChildInfo> childrenWithChangedName = new LinkedList<ChildInfo>();
        Path.Segment[] childNames = (Path.Segment[])parent.get(JBossCacheLexicon.CHILD_PATH_SEGMENT_LIST);
        int index = 0;
        if (childNames != null) {
            for (Path.Segment childName : childNames) {
                if (childName.getName().equals(changedName)) {
                    ChildInfo info = new ChildInfo(childName, index);
                    childrenWithChangedName.add(info);
                }
                index++;
            }
        }
        if (addChildWithName) {
            // Make room for the new child at the end of the array ...
            if (childNames == null) {
                childNames = new Path.Segment[1];
            } else {
                int numExisting = childNames.length;
                Path.Segment[] newChildNames = new Path.Segment[numExisting + 1];
                System.arraycopy(childNames, 0, newChildNames, 0, numExisting);
                childNames = newChildNames;
            }

            // And add a child info for the new node ...
            ChildInfo info = new ChildInfo(null, index);
            childrenWithChangedName.add(info);
            Path.Segment newSegment = context.getValueFactories().getPathFactory().createSegment(changedName);
            childNames[index++] = newSegment;
        }
        assert childNames != null;

        // Now process the children with the same name, which may include a child info for the new node ...
        assert childrenWithChangedName.isEmpty() == false;
        if (childrenWithChangedName.size() == 1) {
            // The child should have no indexes ...
            ChildInfo child = childrenWithChangedName.get(0);
            if (child.segment != null && child.segment.hasIndex()) {
                // The existing child needs to have a new index ..
                Path.Segment newSegment = context.getValueFactories().getPathFactory().createSegment(changedName);
                // Replace the child with the correct FQN ...
                changeNodeName(parent, child.segment, newSegment, context);
                // Change the segment in the child list ...
                childNames[child.childIndex] = newSegment;
            }
        } else {
            // There is more than one child with the same name ...
            int i = 0;
            for (ChildInfo child : childrenWithChangedName) {
                if (child.segment != null) {
                    // Determine the new name and index ...
                    Path.Segment newSegment = context.getValueFactories().getPathFactory().createSegment(changedName, i + 1);
                    // Replace the child with the correct FQN ...
                    changeNodeName(parent, child.segment, newSegment, context);
                    // Change the segment in the child list ...
                    childNames[child.childIndex] = newSegment;
                } else {
                    // Determine the new name and index ...
                    Path.Segment newSegment = context.getValueFactories().getPathFactory().createSegment(changedName, i + 1);
                    childNames[child.childIndex] = newSegment;
                }
                ++i;
            }
        }

        // Record the list of children as a property on the parent ...
        // (Do this last, as it doesn't need to be done if there's an exception in the above logic)
        context.getLogger(getClass()).trace("Updating child list of {0} to: {1}", parent.getFqn(), Arrays.asList(childNames));
        parent.put(JBossCacheLexicon.CHILD_PATH_SEGMENT_LIST, childNames); // replaces any existing value

        if (addChildWithName) {
            // Return the segment for the new node ...
            return childNames[childNames.length - 1];
        }
        return null;
    }

    protected boolean checkChildren( Node<Name, Object> parent ) {
        Path.Segment[] childNamesProperty = (Path.Segment[])parent.get(JBossCacheLexicon.CHILD_PATH_SEGMENT_LIST);
        Set<Object> childNames = parent.getChildrenNames();
        boolean result = true;
        if (childNamesProperty.length != childNames.size()) result = false;
        for (int i = 0; i != childNamesProperty.length; ++i) {
            if (!childNames.contains(childNamesProperty[i])) result = false;
        }
        if (!result) {
            List<Path.Segment> names = new ArrayList<Path.Segment>();
            for (Object name : childNames) {
                names.add((Path.Segment)name);
            }
            Collections.sort(names);
            // Logger.getLogger(getClass()).trace("Child list on {0} is: {1}",
            // parent.getFqn(),
            // StringUtil.readableString(childNamesProperty));
            // Logger.getLogger(getClass()).trace("Children of {0} is: {1}", parent.getFqn(), StringUtil.readableString(names));
        }
        return result;
    }

    /**
     * Utility class used by the {@link JBossCacheConnection#updateChildList(Node, Name, ExecutionContext, boolean)} method.
     * 
     * @author Randall Hauch
     */
    private static class ChildInfo {
        protected final Path.Segment segment;
        protected final int childIndex;

        protected ChildInfo( Path.Segment childSegment,
                             int childIndex ) {
            this.segment = childSegment;
            this.childIndex = childIndex;
        }

    }

    /**
     * Changes the name of the node in the cache (but does not update the list of child segments stored on the parent).
     * 
     * @param parent
     * @param existing
     * @param newSegment
     * @param context
     */
    protected void changeNodeName( Node<Name, Object> parent,
                                   Path.Segment existing,
                                   Path.Segment newSegment,
                                   ExecutionContext context ) {
        assert parent != null;
        assert existing != null;
        assert newSegment != null;
        assert context != null;

        if (existing.equals(newSegment)) return;
        context.getLogger(getClass()).trace("Renaming {0} to {1} under {2}", existing, newSegment, parent.getFqn());
        Node<Name, Object> existingChild = parent.getChild(existing);
        assert existingChild != null;

        // JBoss Cache can move a node from one node to another node, but the move doesn't change the name;
        // since you provide the FQN of the parent location, the name of the node cannot be changed.
        // Therefore, to compensate, we need to create a new child, copy all of the data, move all of the child
        // nodes of the old node, then remove the old node.

        // Create the new node ...
        Node<Name, Object> newChild = parent.addChild(Fqn.fromElements(newSegment));
        Fqn<?> newChildFqn = newChild.getFqn();

        // Copy the data ...
        newChild.putAll(existingChild.getData());

        // Move the children ...
        for (Node<Name, Object> grandChild : existingChild.getChildren()) {
            cache.move(grandChild.getFqn(), newChildFqn);
        }

        // Remove the existing ...
        parent.removeChild(existing);
    }
}
