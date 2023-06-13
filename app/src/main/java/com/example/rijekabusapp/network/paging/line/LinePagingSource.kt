package com.example.rijekabusapp.network.paging.line

/*
class LinePagingSource(
    private val service: BusService,
    private var direction: String = "A"
) : PagingSource<Int, Line>() {

    private val PAGE_SIZE = 20 // Number of items to load per page
    private var cachedResponse: List<Line>? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Line> {
        /*try {
            Log.d("LinePS", "load")
            val pageNumber = params.key ?: 0

            val response = cachedResponse ?: run {
                // Fetch the entire data set from the backend if not cached
                val freshResponse = service.getAllStations()
                cachedResponse = freshResponse // Cache the response
                freshResponse
            }

            // Apply filtering to remove duplicates based on lineNumber, name, and direction
            val filteredResponse = filterDuplicates(response, direction)

            // Calculate the range of items to return based on the page number and page size
            val startOffset = pageNumber * PAGE_SIZE
            val endOffset = minOf(startOffset + PAGE_SIZE, filteredResponse.size)

            val pageItems = filteredResponse.subList(startOffset, endOffset)

            val prevPageNumber = if (pageNumber > 0) pageNumber - 1 else null
            val nextPageNumber = if (endOffset < filteredResponse.size) pageNumber + 1 else null

            return LoadResult.Page(
                data = pageItems, prevKey = prevPageNumber, nextKey = nextPageNumber
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        } */
        return LoadResult.Error()
    }

    private fun filterDuplicates(lines: List<Line>, direction: String): List<Line> {
        val uniqueLines = mutableSetOf<String>()
        val filteredLines = mutableListOf<Line>()

        for (line in lines) {
            val key = "${line.lineNumber}-${line.name}-${line.direction}"
            if (uniqueLines.add(key)) {
                filteredLines.add(line)
            }
        }
        if (direction != "") {
            for (line in filteredLines) {
                if (line.direction != direction) {
                    filteredLines.remove(line)
                }
            }
        }

        return filteredLines
    }

    fun updateDirection(newDirection: String) {
        Log.d("all_lines", "DIR")
        direction = newDirection
        cachedResponse = null
    }

    override fun getRefreshKey(state: PagingState<Int, Line>): Int? {
        // Invalidate the paging state to force a refresh when invalidating data
        return null
    }
}
 */
