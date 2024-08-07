package com.robinwersich.todue.utility

fun <T> requireSame(
  first: T,
  second: T,
  lazyMessage: () -> Any = { "$first and $second are expected to be the same, but are not." },
): T {
  require(first == second, lazyMessage)
  return first
}
