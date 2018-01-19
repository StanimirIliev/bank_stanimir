import React, { Component } from 'react'
import axios from 'axios'
import Loading from '../../Common/Loading'
import Money from '../../Common/Money'
import './TransactionPage.css'

export default class TransactionPage extends Component {
    constructor(props) {
        super(props)
        this.state = {
            page: this.props.page,
            pageSize: this.props.pageSize,
            loading: false,
            transactions: []
        }
    }

    componentWillMount() {
        this.setState({ loading: true })
        axios.get(`/v1/transactions/${this.state.page}?pageSize=${this.state.pageSize}`)
            .then(response => {
                this.setState({
                    transactions: response.data.transactions,
                    loading: false
                })
            })
            .catch(error => this.setState({
                loading: false
            }))
    }

    render() {
        const { loading, transactions, page } = this.state
        if (loading) {
            return (<Loading />)
        }
        const transactionsRendering = []
        for (let i = 0; i < transactions.length; i++) {
            transactionsRendering.push(
                <tr className="table__transaction__body__rows">
                    <td className="table__transaction__columns">{(page - 1) * 20 + i + 1}</td>
                    <td className="table__transaction__columns">{transactions[i].onDate}</td>
                    <td className="table__transaction__columns">{transactions[i].title}</td>
                    <td className="table__transaction__columns">{transactions[i].operation}</td>
                    <td className="table__transaction__columns"><Money amount={transactions[i].amount} currency={transactions[i].currency} digits={2} /></td>
                </tr>
            )
        }

        return (
            <div className="container__transaction" >
                <table className="table__transaction">
                    <tr className="table__transaction__header__rows">
                        <th className="table__transaction__columns">&#8470;</th>
                        <th className="table__transaction__columns">Date and Time</th>
                        <th className="table__transaction__columns">Account</th>
                        <th className="table__transaction__columns">Operation</th>
                        <th className="table__transaction__columns">Amount</th>
                    </tr>
                    {transactionsRendering}
                </table>
            </div>
        )
    }
}
