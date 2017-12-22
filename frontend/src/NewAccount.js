import React, { Component } from 'react'
import { Link } from 'react-router-dom'
import './NewAccount.css'
import Loading from './Loading'
import Message from './Message'
import axios from 'axios'

class NewAccount extends Component {
    constructor() {
        super()
        this.state = {
            currency: "bgn",
            title: null,
            loading: false,
            message: null
        }
        this.handleCurrencyChange = this.handleCurrencyChange.bind(this)
        this.handleTitleChange = this.handleTitleChange.bind(this)
    }

    createNewAccount() {
        this.setState({ loading: true })
        console.log(this.state.title)
        console.log(this.state.currency)
        axios.get('/v1/newAccount', {
            params: {
                title: this.state.title,
                currency: this.state.currency
            }
        })
            .then(resp => this.setState({
                loading: false,
                message: {
                    content: resp.data.msg,
                    messageClass: 'message--positive'
                }
            }))
            .catch(error => this.setState({
                loading: false,
                message: {
                    content: error.response.data.msg,
                    messageClass: 'message--negative'
                }
            }))
    }

    handleCurrencyChange(event) {
        this.setState({ currency: event.target.value })
    }

    handleTitleChange(event) {
        this.setState({ title: event.target.value })
    }

    render() {
        const { loading, message } = this.state

        if (loading) {
            return (<Loading />)
        }
        if (message != null) {
            return (<Message returnPath="accountsMenu" messageClass={message.messageClass} content={message.content} />)
        }
        return (
            <div className="container__accounts">
                <h1>Open a new account</h1>
                <div className="new_account__set_title" >
                    <div>Title:</div>
                    <input className="new_account__input" onChange={this.handleTitleChange} />
                </div>
                <div className="new_account__set_currency">
                    <div>Currency:</div>
                    <select className="new_account__select" name="currency" onChange={this.handleCurrencyChange}>
                        <option value="bgn">BGN</option>
                        <option value="eur">EUR</option>
                    </select>
                </div>
                <button className="button new_account__button" onClick={() => { this.createNewAccount() }}>Create</button>
                <Link className="linkButton button--close" to="/accountsMenu">Back</Link>
            </div>
        )
    }
}

export default NewAccount