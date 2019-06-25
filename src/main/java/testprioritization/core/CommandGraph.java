package testprioritization.core;

import com.sun.tools.javac.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class CommandGraph {

    private final List<Edge> edges;

    public static CommandGraph fromEdges(List<Edge> edges) {
        return new CommandGraph(edges);
    }

    public static CommandGraph empty() {
        return new CommandGraph(new ArrayList<>());
    }

    private CommandGraph(List<Edge> edges) {
        this.edges = edges;
    }

    public float getWeightFor(Pair<TestStep, TestStep> pairOfTestSteps) {
        for (Edge edge : edges) {
            if (edge.equalsToPair(pairOfTestSteps)) {
                return edge.weight;
            }
        }
        return 0f;
    }

    public CommandGraph mergeWith(CommandGraph otherGraph) {
        List<Edge> newEdges = edges;
        for (Edge edgeFromOtherGraph : otherGraph.edges) {
            if (newEdges.contains(edgeFromOtherGraph)) {
                int indexToUpdate = newEdges.indexOf(edgeFromOtherGraph);
                newEdges.get(indexToUpdate).mergeEdge(edgeFromOtherGraph);
            } else {
                newEdges.add(edgeFromOtherGraph);
            }
        }
        return CommandGraph.fromEdges(newEdges);
    }

    static final class Edge {

        private final TestStep step1;
        private final TestStep step2;

        float weight;

        Edge(TestStep step1, TestStep step2, float weight) {
            this.step1 = step1;
            this.step2 = step2;
            this.weight = weight;
        }

        public void mergeEdge(Edge otherEdge) {
            if (this.equals(otherEdge)) {
                this.weight += otherEdge.weight;
            }
        }

        public boolean equalsToPair(Pair<TestStep, TestStep> pairOfTestSteps) {
            return pairOfTestSteps.fst == step1 && pairOfTestSteps.snd == step2;
        }

        @Override
        public boolean equals(Object other) {
            boolean incompatibleType = ! (other instanceof Edge);
            if (incompatibleType) {
                return false;
            }

            Edge otherEdge = (Edge) other;
            return step1 == otherEdge.step1 && step2 == otherEdge.step2;
        }
    }
}
