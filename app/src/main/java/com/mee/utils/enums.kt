package com.mee.utils

enum class SortOrder(val order: Int) {
    NEWEST_DATE_FIRST(1),
    OLDEST_DATE_FIRST(2),
    LARGEST_FIRST(3),
    SMALLEST_FIRST(4),
    NAME_A_TO_Z(5),
    NAME_Z_TO_A(6)
}