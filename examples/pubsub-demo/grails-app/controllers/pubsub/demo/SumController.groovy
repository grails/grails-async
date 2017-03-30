package pubsub.demo


class SumController {
	static responseFormats = ['json', 'xml']

    SumService sumService
    TotalService totalService

    def index() {
        int sum = sumService.sum(1, 2)
        int total = totalService.accumulatedTotal
        render "sum: $sum, total: $total"
    }
}
