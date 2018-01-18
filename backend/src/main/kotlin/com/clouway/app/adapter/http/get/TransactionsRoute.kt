package com.clouway.app.adapter.http.get

import com.clouway.app.core.SecuredRoute
import com.clouway.app.core.Session
import com.clouway.app.core.Transaction
import com.clouway.app.core.TransactionRepository
import com.clouway.app.core.httpresponse.HttpResponseMessage
import com.clouway.app.core.httpresponse.HttpResponseTransactionsCount
import com.clouway.app.core.httpresponse.HttpResponseTransactionsList
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response

class TransactionsRoute(private val transactionRepository: TransactionRepository) : SecuredRoute {
    override fun handle(req: Request, resp: Response, session: Session): Any {
        val parameter = req.params("param")
        if (parameter == "count") {
            return HttpResponseTransactionsCount(transactionRepository.getTransactions(session.userId).count())
        } else {//    use parameter to show from which page to get transactions
            val page = parameter.toIntOrNull()
            val pageSize = req.queryParams("pageSize").toIntOrNull()
            if (page == null || pageSize == null) {
                resp.status(HttpStatus.BAD_REQUEST_400)
                return HttpResponseMessage("pageSize or page parameter is missing.")
            }
            return HttpResponseTransactionsList(
                    transactionRepository.getTransactions(session.userId).getFromPage(pageSize, page))
        }
    }

    private fun List<Transaction>.getFromPage(pageSize: Int, page: Int): List<Transaction> {
        if (pageSize <= 0 || page <= 0) {
            return emptyList()
        }
        val elements = this.count()
        val fromIndex = (page - 1) * pageSize
        val toIndex = if (fromIndex + pageSize > elements) elements else fromIndex + pageSize
        if (fromIndex > toIndex || toIndex > elements) {
            return emptyList()
        }
        return this.subList(fromIndex, toIndex)
    }
}
