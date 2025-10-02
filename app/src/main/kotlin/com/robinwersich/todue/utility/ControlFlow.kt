package com.robinwersich.todue.utility

/** Returns a modified copy of this value if [condition] is true, otherwise returns this value. */
inline fun <T> T.letIf(condition: Boolean, block: (T) -> T) = if (condition) block(this) else this
