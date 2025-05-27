package com.example.rijekabusapp.network.response

data class BusLinesResponse(
    val err: Boolean,
    val msg: String,
    val res: Map<String, LineInfo>,
)

data class LineInfo(
    val brojLinije: String? = null,
    val id: Int? = null,
    val naziv: String? = null,
    val polazakList: List<Any>? = emptyList(),
    val smjerId: Int? = null,
    val smjerNaziv: String? = null,
    val varijantaId: Int? = null,
)
