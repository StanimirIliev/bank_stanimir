import React, { Component } from 'react'
import './TransactionPage.css'
import Loading from './Loading'
import Message from './Message'
import axios from 'axios'

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
        axios.get(`/v1/transactions/${this.state.page}`, {
            params: {
                pageSize: this.state.pageSize
            }
        })
            .then(response => {
                this.setState({
                    loading: false,
                    transactions: response.data.transactions
                })
            })
            .catch(error => {
                this.setState({
                    loading: false
                })
                console.log(error)
            })
    }

    parseDateTime(dateTime) {
        const date = dateTime.date
        const time = dateTime.time
        return `${this.parseNumber(date.day)}.${this.parseNumber(date.month)}.${date.year} ${this.parseNumber(time.hour)}:${this.parseNumber(time.minute)}`
    }

    parseNumber(number) {
        return number < 10 ? `0${number}` : `${number}`
    }


    render() {
        console.log('TransactionPage rendering, page = ' + this.state.page)
        const { loading, transactions } = this.state

        if (loading) {
            return (<Loading />)
        }

        const transactionsRendering = []
        for (let i = 0; i < transactions.length; i++) {
            transactionsRendering.push(
                <tr className="table__transaction__body__rows">
                    <td className="table__transaction__columns">{this.parseDateTime(transactions[i].dateTime)}</td>
                    <td className="table__transaction__columns">{transactions[i].account}</td>
                    <td className="table__transaction__columns">{transactions[i].operation}</td>
                    <td className="table__transaction__columns">{
                        new Intl.NumberFormat('de-DE', {
                            style: 'currency',
                            currency: transactions[i].currency
                        }).format(transactions[i].amount)
                    }</td>
                </tr>
            )
        }

        return (
            <div className="container__transaction" >
                <table className="table__transaction">
                    <tr className="table__transaction__header__rows">
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