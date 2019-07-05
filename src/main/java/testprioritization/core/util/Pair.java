package testprioritization.core.util;

public class Pair<F, S> {
    public F first;
    public S second;

    public static <F, S> Pair<F, S> of(F first, S second) {
        Pair<F, S> pair = new Pair<>();
        pair.first = first;
        pair.second = second;
        return pair;
    }
}
