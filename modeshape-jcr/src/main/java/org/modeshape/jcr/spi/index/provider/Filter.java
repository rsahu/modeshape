/*
 * ModeShape (http://www.modeshape.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.modeshape.jcr.spi.index.provider;

import java.util.Iterator;
import org.modeshape.common.collection.EmptyIterator;
import org.modeshape.jcr.cache.NodeKey;
import org.modeshape.jcr.spi.index.IndexConstraints;

/**
 * A simple filter that takes constraints as input and returns a results object that lazily returns (in batches) those nodes that
 * satisfy the constraints.
 * 
 * @author Randall Hauch (rhauch@redhat.com)
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
public interface Filter {

    /**
     * Return a {@link Results} instance from which ModeShape can obtain the nodes that satisfy the supplied constraints. This
     * method should return quickly, since no work (or at least very little work) should be done. All of the work should be
     * performed when the {@link Results#getNextBatch(int)} method is called on the results.
     * 
     * @param constraints the constraints to be applied by this index; never null
     * @param cardinalityEstimate the total number of elements which this index reported during the planning phase
     * @return a {@link Results} instance; never null
     */
    Results filter(IndexConstraints constraints, long cardinalityEstimate);

    /**
     * The results of a {@link Filter#filter(IndexConstraints, long)} operation that contains the nodes that satisfy the
     * {@link Filter#filter(IndexConstraints, long) supplied constraints}.
     * <p>
     * ModeShape will periodically call {@link #getNextBatch(int)} as (additional) results are needed to answer the
     * query. Generally, each {@link Results} instance will have enough state to execute the filtering steps.
     * </p>
     * <p>
     * ModeShape may call this method zero or more times, based upon whether results are actually needed and in specific batch
     * sizes. And every time this method is called, implementations should write the next batch of nodes that satisfies the
     * criteria, where the number of nodes in the batch should be roughly equal to <code>batchSize</code>. If the index
     * implementation is interacting with a remote system, then each method invocation might correspond to a single remote
     * request.
     * </p>
     * 
     * @author Randall Hauch (rhauch@redhat.com)
     */
    interface Results extends AutoCloseable {
        /**
         * Obtain the next batch of results for the query.
         * 
         * @param batchSize the ideal number of node keys that are to be included in this batch; always positive
         * @return true if there are additional results after this batch is completed, or false if this batch contains the final
         *         results of the query
         */
        ResultBatch getNextBatch(int batchSize);

        /**
         * Close any and all resources for the operation. This will always be called by ModeShape when the operation is no longer
         * needed, even if {@link #getNextBatch(int)} was never called.
         */
        @Override
        void close();
    }

    /**
     * A batch of results as returned by {@link Results#getNextBatch(int)}
     */
    interface ResultBatch {
        Iterator<?> EMPTY_ITERATOR = new EmptyIterator<>();
        ResultBatch EMPTY = new ResultBatch() {
            @Override
            @SuppressWarnings( "unchecked" )
            public Iterable<NodeKey> keys() {
                return () -> (Iterator<NodeKey>) EMPTY_ITERATOR;
            }
    
            @Override
            @SuppressWarnings( "unchecked" )
            public Iterable<Float> scores() {
                return () -> (Iterator<Float>) EMPTY_ITERATOR;
            }
    
            @Override
            public boolean hasNext() {
                return false;
            }
    
            @Override
            public int size() {
                return 0;
            }
        };

        /**
         * Returns an {@link Iterable} over a number of {@code NodeKey} instances representing matching nodes in an index.
         * This should have the same order as {@link #scores()}
         * 
         * @return an iterable instance, never {@code null}
         */
        Iterable<NodeKey> keys();

        /**
         * Returns an {@link Iterable} over a number of {@code Float} values, representing the scores of the matched documents
         * from {@link #keys()}. This should have the same order as {@link #keys()}
         * 
         * @return an iterable instance, never {@code null}
         */
        Iterable<Float> scores();

        /**
         * Checks if this batch is followed by another batch or is the last batch of the search results.
         * 
         * @return {@code true} if this batch is not the last one, {@code false} otherwise.
         */
        boolean hasNext();

        /**
         * Returns the total number of node keys in this batch.
         * 
         * @return the number of node keys; must be positive. 
         */
        int size();
    }
}
