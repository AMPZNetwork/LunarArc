package io.papermc.paper.plugin.entrypoint.strategy;

import com.google.common.graph.Graph;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class TopographicGraphSorter {

    public static <N> List<N> sortGraph(Graph<N> graph) throws PluginGraphCycleException {
        List<N> sorted = new ArrayList<>();
        Deque<N> roots = new ArrayDeque<>();
        Object2IntMap<N> nonRoots = new Object2IntOpenHashMap<>();

        for (N node : graph.nodes()) {
            int degree = graph.inDegree(node);
            if (degree == 0) {
                roots.add(node);
            } else {
                nonRoots.put(node, degree);
            }
        }

        N next;
        while ((next = roots.poll()) != null) {
            for (N successor : graph.successors(next)) {
                int newInDegree = nonRoots.removeInt(successor) - 1;

                if (newInDegree == 0) {
                    roots.add(successor);
                } else {
                    nonRoots.put(successor, newInDegree);
                }

            }
            sorted.add(next);
        }

        if (!nonRoots.isEmpty()) {
            throw new GraphCycleException();
        }

        return sorted;
    }

    public static final class GraphCycleException extends RuntimeException {

    }
}
