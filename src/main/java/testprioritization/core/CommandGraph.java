package testprioritization.core;

import java.util.Set;

public class CommandGraph {

    Set<Edge> edges;


    private final class Edge {

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

        @Override
        public boolean equals(Object obj) {
            Edge otherEdge = (Edge) obj;
            return step1 == otherEdge.step1 && step2 == otherEdge.step2;
        }
    }
}
