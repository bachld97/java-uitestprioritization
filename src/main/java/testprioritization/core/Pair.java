package testprioritization.core;

class Pair<F, S> {
    F first;
    S second;

    static <F, S> Pair<F, S> of(F first, S second) {
        Pair<F, S> pair = new Pair<>();
        pair.first = first;
        pair.second = second;
        return pair;
    }
}
