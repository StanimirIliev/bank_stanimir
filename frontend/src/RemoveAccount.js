import React, { Component } from 'react'
import { Link } from 'react-router-dom'
import axios from 'axios'
import './RemoveAccount.css'
import Loading from './Loading'
import Message from './Message'


class AccountDetails extends Component {
    constructor(props) {
        super(props)
        this.state = {
            account: {},
            loading: false,
            error: null,
            id: parseInt(this.props.match.params.id),
            msg: null
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

    deleteAccount() {
        this.setState({ loading: true })
        axios.delete('/v1/removeAccount', {
            params: {
                id: this.state.id
            }
        })
            .then(response => this.setState({
                loading: false,
                msg: {
                    content: response.data.msg,
                    messageClass: 'message--positive'
                }
            }))
            .catch(error => this.setState({
                loading: false,
                msg: {
                    content: error.response.data.msg,
                    messageClass: 'message--negative'
                }
            }))
    }

    render() {
        const { loading, msg, title, account, id } = this.state
        if (loading) {
            return (<Loading />)
        }

        if (msg != null) {
            return (<Message returnPath="/removeAccounts"
                content={msg.content}
                messageClass={msg.messageClass} />)
        }

        return (
            <div className="container__accounts" >
                <h1 className="account_menu__header">Are you sure you want to delete account:<br />  {account.title}</h1>
                <div className="container__selected_account">
                    <div className="selected_account__balance">
                        <b>Balance:</b>
                        {
                            new Intl.NumberFormat('de-DE', {
                                style: 'currency',
                                currency: account.currency
                            }).format(account.balance)
                        }
                    </div>
                    <div className="selected_account__buttons">
                        <button className="button remove_account__buttons" onClick={() => {
                            this.deleteAccount()
                        }}>Yes</button>
                        <Link className="linkButton remove_account__buttons" to='/removeAccounts'>No</Link>
                    </div>
                </div>
                <Link className="linkButton button--close" to="/removeAccounts">Back</Link>
            </div>
        )
    }
}

export default AccountDetails