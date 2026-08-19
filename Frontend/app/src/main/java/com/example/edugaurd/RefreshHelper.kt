package com.example.edugaurd

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

object RefreshHelper {

    fun setup(
        swipeRefresh: SwipeRefreshLayout,
        onRefresh: () -> Unit
    ) {

        swipeRefresh.setOnRefreshListener {
            onRefresh()
        }
    }

    fun start(
        swipeRefresh: SwipeRefreshLayout
    ) {
        swipeRefresh.isRefreshing = true
    }

    fun stop(
        swipeRefresh: SwipeRefreshLayout
    ) {
        swipeRefresh.isRefreshing = false
    }
}