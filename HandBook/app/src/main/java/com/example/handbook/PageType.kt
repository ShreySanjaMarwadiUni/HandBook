package com.example.handbook

sealed class PageType(val id: Long) {

    object CoverStart : PageType(0)
    object CoverEnd : PageType(1)

    class NotePage(
        val pageNumber: Int = 0,
        var html: String = ""
    ) : PageType(System.currentTimeMillis())
}

