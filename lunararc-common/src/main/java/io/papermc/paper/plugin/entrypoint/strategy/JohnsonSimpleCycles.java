/*
 * (C) Copyright 2013-2021, by Nikolay Ognyanov and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */

// MODIFICATIONS:
// - Modified to use a guava graph directly

package io.papermc.paper.plugin.entrypoint.strategy;

import com.google.common.base.Preconditions;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.mojang.datafixers.util.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class JohnsonSimpleCycles<V>
{
    private Graph<V> graph;

    // The main state of the algorithm.
    private Consumer<List<V>> cycleConsumer = null;
    private BiConsumer<V, V> cycleVertexSuccessorConsumer = null; // Paper
    private V[] iToV = null;
    private Map<V, Integer> vToI = null;
    private Set<V> blocked = null;
    private Map<V, Set<V>> bSets = null;
    private ArrayDeque<V> stack = null;

    // The state of the embedded Tarjan SCC algorithm.
    private List<Set<V>> foundSCCs = null;
    private int index = 0;
    private Map<V, Integer> vIndex = null;
    private Map<V, Integer> vLowlink = null;
    private ArrayDeque<V> path = null;
    private Set<V> pathSet = null;

    public JohnsonSimpleCycles(Graph<V> graph)
    {
        Preconditions.checkState(graph.isDirected(), "Graph must be directed");
        this.graph = graph;
    }

    public List<List<V>> findAndRemoveSimpleCycles()
    {
        List<List<V>> result = new ArrayList<>();
        findSimpleCycles(result::add, (v, s) -> ((MutableGraph<V>) graph).removeEdge(v, s)); // Paper
        return result;
    }

    public void findSimpleCycles(Consumer<List<V>> consumer, BiConsumer<V, V> vertexSuccessorConsumer) // Paper
    {
        if (graph == null) {
            throw new IllegalArgumentException("Null graph.");
        }
        cycleVertexSuccessorConsumer = vertexSuccessorConsumer; // Paper
        initState(consumer);

        int startIndex = 0;
        int size = graph.nodes().size();
        while (startIndex < size) {
            Pair<Graph<V>, Integer> minSCCGResult = findMinSCSG(startIndex);
            if (minSCCGResult != null) {
                startIndex = minSCCGResult.getSecond();
                Graph<V> scg = minSCCGResult.getFirst();
                V startV = toV(startIndex);
                for (V v : scg.successors(startV)) {
                    blocked.remove(v);
                    getBSet(v).clear();
                }
                findCyclesInSCG(startIndex, startIndex, scg);
                startIndex++;
            } else {
                break;
            }
        }

        clearState();
    }

    private Pair<Graph<V>, Integer> findMinSCSG(int startIndex)
    {
        initMinSCGState();

        List<Set<V>> foundSCCs = findSCCS(startIndex);

        int minIndexFound = Integer.MAX_VALUE;
        Set<V> minSCC = null;
        for (Set<V> scc : foundSCCs) {
            for (V v : scc) {
                int t = toI(v);
                if (t < minIndexFound) {
                    minIndexFound = t;
                    minSCC = scc;
                }
            }
        }
        if (minSCC == null) {
            return null;
        }

        MutableGraph<V> dependencyGraph = GraphBuilder.directed().allowsSelfLoops(true).build();

        for (V v : minSCC) {
            for (V w : minSCC) {
                if (graph.hasEdgeConnecting(v, w)) {
                    dependencyGraph.putEdge(v, w);
                }
            }
        }

        Pair<Graph<V>, Integer> result = Pair.of(dependencyGraph, minIndexFound);
        clearMinSCCState();
        return result;
    }

    private List<Set<V>> findSCCS(int startIndex)
    {
        for (V v : graph.nodes()) {
            int vI = toI(v);
            if (vI < startIndex) {
                continue;
            }
            if (!vIndex.containsKey(v)) {
                getSCCs(startIndex, vI);
            }
        }
        List<Set<V>> result = foundSCCs;
        foundSCCs = null;
        return result;
    }

    private void getSCCs(int startIndex, int vertexIndex)
    {
        V vertex = toV(vertexIndex);
        vIndex.put(vertex, index);
        vLowlink.put(vertex, index);
        index++;
        path.push(vertex);
        pathSet.add(vertex);

        Set<V> edges = graph.successors(vertex);
        for (V successor : edges) {
            int successorIndex = toI(successor);
            if (successorIndex < startIndex) {
                continue;
            }
            if (!vIndex.containsKey(successor)) {
                getSCCs(startIndex, successorIndex);
                vLowlink.put(vertex, Math.min(vLowlink.get(vertex), vLowlink.get(successor)));
            } else if (pathSet.contains(successor)) {
                vLowlink.put(vertex, Math.min(vLowlink.get(vertex), vIndex.get(successor)));
            }
        }
        if (vLowlink.get(vertex).equals(vIndex.get(vertex))) {
            Set<V> result = new HashSet<>();
            V temp;
            do {
                temp = path.pop();
                pathSet.remove(temp);
                result.add(temp);
            } while (!vertex.equals(temp));
            if (result.size() == 1) {
                V v = result.iterator().next();
                if (graph.edges().contains(vertex)) {
                    foundSCCs.add(result);
                }
            } else {
                foundSCCs.add(result);
            }
        }
    }

    private boolean findCyclesInSCG(int startIndex, int vertexIndex, Graph<V> scg)
    {
        boolean foundCycle = false;
        V vertex = toV(vertexIndex);
        stack.push(vertex);
        blocked.add(vertex);

        for (V successor : scg.successors(vertex)) {
            int successorIndex = toI(successor);
            if (successorIndex == startIndex) {
                List<V> cycle = new ArrayList<>(stack.size());
                stack.descendingIterator().forEachRemaining(cycle::add);
                cycleConsumer.accept(cycle);
                cycleVertexSuccessorConsumer.accept(vertex, successor); // Paper
                //foundCycle = true; // Paper
            } else if (!blocked.contains(successor)) {
                boolean gotCycle = findCyclesInSCG(startIndex, successorIndex, scg);
                foundCycle = foundCycle || gotCycle;
            }
        }
        if (foundCycle) {
            unblock(vertex);
        } else {
            for (V w : scg.successors(vertex)) {
                Set<V> bSet = getBSet(w);
                bSet.add(vertex);
            }
        }
        stack.pop();
        return foundCycle;
    }

    private void unblock(V vertex)
    {
        blocked.remove(vertex);
        Set<V> bSet = getBSet(vertex);
        while (bSet.size() > 0) {
            V w = bSet.iterator().next();
            bSet.remove(w);
            if (blocked.contains(w)) {
                unblock(w);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void initState(Consumer<List<V>> consumer)
    {
        cycleConsumer = consumer;
        iToV = (V[]) graph.nodes().toArray();
        vToI = new HashMap<>();
        blocked = new HashSet<>();
        bSets = new HashMap<>();
        stack = new ArrayDeque<>();

        for (int i = 0; i < iToV.length; i++) {
            vToI.put(iToV[i], i);
        }
    }

    private void clearState()
    {
        cycleConsumer = null;
        iToV = null;
        vToI = null;
        blocked = null;
        bSets = null;
        stack = null;
    }

    private void initMinSCGState()
    {
        index = 0;
        foundSCCs = new ArrayList<>();
        vIndex = new HashMap<>();
        vLowlink = new HashMap<>();
        path = new ArrayDeque<>();
        pathSet = new HashSet<>();
    }

    private void clearMinSCCState()
    {
        index = 0;
        foundSCCs = null;
        vIndex = null;
        vLowlink = null;
        path = null;
        pathSet = null;
    }

    private Integer toI(V vertex)
    {
        return vToI.get(vertex);
    }

    private V toV(Integer i)
    {
        return iToV[i];
    }

    private Set<V> getBSet(V v)
    {
        // B sets typically not all needed,
        // so instantiate lazily.
        return bSets.computeIfAbsent(v, k -> new HashSet<>());
    }
}
