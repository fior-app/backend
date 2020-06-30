package app.fior.backend.extensions

import reactor.util.function.Tuple2


operator fun <T1, T2> Tuple2<T1, T2>.component1(): T1 {
    return this.t1
}

operator fun <T1, T2> Tuple2<T1, T2>.component2(): T2 {
    return this.t2
}
