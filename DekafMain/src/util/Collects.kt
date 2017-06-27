@file:JvmName("Collects")
package org.jetbrains.dekaf.util

//import java.lang.reflect.Array
import java.util.*


/**
 * Return an immutable (shallow) copy of the given collection.
 * @param collection   the collection to copy.
 * *
 * @param <T>          the type of elements.
 * *
 * @return             just created copy.
</T> */
fun <T> listCopy(collection: Collection<T>?): List<T> {
    if (collection == null || collection.isEmpty()) {
        return emptyList()
    }
    else {
        val n = collection.size
        if (n == 1) {
            return listOf(collection.iterator().next())
        }
        else {
            return Collections.unmodifiableList(ArrayList(collection))
        }
    }
}


fun arrayToString(array: Array<*>?, delimiter: String): String {
    if (array == null) return ""
    val n = array.size
    when (n) {
        0    -> return ""
        1    -> return array[0].toString()
        else -> {
            val b = StringBuilder()
            b.append(array[0].toString())
            for (i in 1..n - 1) b.append(delimiter).append(array!![i])
            return b.toString()
        }
    }
}


@JvmOverloads
fun collectionToString(collection: Iterable<*>?,
                       delimiter: String?,
                       prefix: String? = null,
                       suffix: String? = null,
                       empty: String? = null): String {
    if (collection == null) {
        return empty ?: ""
    }

    val b = StringBuilder()
    for (`object` in collection) {
        if (b.length == 0) {
            if (prefix != null) b.append(prefix)
        }
        else {
            if (delimiter != null) b.append(delimiter)
        }

        b.append(`object`)
    }

    if (b.length > 0) {
        if (suffix != null) b.append(suffix)
    }
    else {
        if (empty != null) b.append(empty)
    }

    return b.toString()
}
