package com.university.marathononline.data.api.share

data class Pageable(
    val sort: Sort,
    val offset: Int,
    val pageNumber: Int,
    val pageSize: Int,
    val paged: Boolean,
    val unpaged: Boolean
)