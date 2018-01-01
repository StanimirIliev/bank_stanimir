import React, { Component } from 'react'
import { Link } from 'react-router-dom'
import axios from 'axios'
import './AccountDetails.css'
import Loading from './Loading'

class AccountDetails extends Component {
    constructor(props) {
        super(props)
        this.state = {
            account: {},
            loading: false,
            error: null,
            id: parseInt(this.props.match.params.id)
        }
    }

    componentWillMount() {
        this.setState({ loading: true })
        axios.get('/v1/account', { params: { id: this.state.id } })
            .then(resp => {
                this.setState({ loading: false, account: resp.data.account })
            })
            .catch(error => this.setState({ loading: false, error: error.data.content }))
    }

    render() {
        if (this.state.loading) {
            return (<Loading />)
        }

        const title = this.state.account.title
        const balance = this.state.account.balance
        const currency = this.state.account.currency
        const id = this.state.id

        return (
            <div className="container__accounts" >
                <h1 className="account_menu__header">Account:  {title}</h1>
                <div className="container__selected_account">
                    <div className="selected_account__balance">
                        <b>Balance:</b> {balance}
                    </div>
                    <div className="selected_account__currency">
                        <b>Currency:</b> {currency}
                    </div>
                    <div className="selected_account__buttons">
                        <Link className="linkButton selected_account__button" to={`/deposit/${id}`}>Deposit</Link>
                        <Link className="linkButton selected_account__button" to={`/withdraw/${id}`}>Withdraw</Link>
                    </div> 
                </div>
                <Link className="linkButton button--close" to="/accounts">Back</Link>
            </div>
        )
    }
}

export default AccountDetails