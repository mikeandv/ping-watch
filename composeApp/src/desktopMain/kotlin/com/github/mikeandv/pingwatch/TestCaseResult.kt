package com.github.mikeandv.pingwatch

class TestCaseResult(data: List<ResponseData>) {
    val results: List<ServiceResult>

    init {
        val resultCalc = mutableListOf<ServiceResult>()
        val parsedData = data.groupBy { it.url }
            .mapValues { (_, results) ->
                results.groupBy { it.statusCode }
                    .mapValues { (_, reqs) -> reqs.map { it.duration } }
            }
        for ((key, value) in parsedData) {
            resultCalc.add(ServiceResult.create(key, value))
        }
        results = resultCalc.toList()
    }

    override fun toString(): String {
        return "TestCaseResult(results=$results)"
    }


}