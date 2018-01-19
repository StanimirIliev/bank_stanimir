package com.clouway.app.adapter.http.get

import com.clouway.app.core.*
import com.clouway.app.core.httpresponse.HttpResponseMessage
import com.clouway.app.core.httpresponse.HttpResponseTransaction
import com.clouway.app.core.httpresponse.HttpResponseTransactionsCount
import com.clouway.app.core.httpresponse.HttpResponseTransactionsList
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import java.sql.Timestamp
import java.util.*

class TransactionsRoute(
        private val transactionRepository: TransactionRepository,
        private val accountRepository: AccountRepository
) : SecuredRoute {
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
            val selectedList = transactionRepository.getTransactions(session.userId)
                    .cleanTransactionsOfDeletedAccounts()
                    .getFromPage(pageSize, page)
            val accounts = accountRepository.getAllAccounts(session.userId)
            return HttpResponseTransactionsList(selectedList.transformToDTO(accounts))
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

    private fun List<Transaction>.transformToDTO(accounts: List<Account>): List<HttpResponseTransaction> {
        val output = LinkedList<HttpResponseTransaction>()
        this.forEach {
            val accountId = it.accountId
            val account = accounts.find { it.id == accountId }!!
            output.add(com.clouway.app.core.httpresponse.HttpResponseTransaction(
                    it.userId,
                    Timestamp.valueOf(it.onDate),
                    it.operation,
                    account.title,
                    account.currency,
                    it.amount
            ))
        }
        return output
    }

    private fun List<Transaction>.cleanTransactionsOfDeletedAccounts(): List<Transaction> {
        if(this.isEmpty()){
            return this
        }
        val accounts = accountRepository.getAllAccounts(this.first().userId)
        val output = LinkedList<Transaction>()
        this.forEach {
            val transaction = it
            if (accounts.find { it.id == transaction.accountId } == null) {
                transactionRepository.deleteTransaction(transaction)
            } else {
                output.add(transaction)
            }
        }
        return output
    }
}
