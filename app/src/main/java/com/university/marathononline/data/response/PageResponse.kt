package com.university.marathononline.data.response

data class PageResponse<T>(
    val content: List<T>,
    val pageable: Pageable,
    val last: Boolean,
    val totalElements: Int,
    val totalPages: Int,
    val size: Int,
    val number: Int,
    val sort: Sort,
    val first: Boolean,
    val numberOfElements: Int,
    val empty: Boolean
)