import React, { Component } from 'react'
import { Link } from 'react-router-dom'
import Loading from './Loading'
import Msg from './Message'
import axios from 'axios'

class Deposit extends Component {
    constructor(props) {
        super(props)
        this.state = {
            id: parseInt(this.props.match.params.id),
            loading: false,
            account: {},
            error: null,
            msg: null,
            depositValue: null
        }
        this.handleChange = this.handleChange.bind(this);
    }

    componentWillMount() {
        this.setState({ loading: true })
        axios.get('/v1/account', {
            params: {
                id: this.state.id
            }
        })
            .then(resp => this.setState({
                account: resp.data.account,
                loading: false
            }))
            .catch(error => this.setState({
                loading: false,
                error: error.data.content
            }))
    }

    executeDeposit() {
        this.setState({ loading: true })
        axios.get('/v1/executeDeposit', {
            params: {
                id: parseInt(this.state.id),
                value: parseInt(this.state.depositValue)
            }
        })
            .then(resp => this.setState({
                msg: {
                    content: resp.data.msg,
                    messageClass: 'message--positive'
                },
                loading: false
            }))
            .catch(error => this.setState({
                msg: {
                    content: error.response.data.msg,
                    messageClass: 'message--negative'  
                },
                loading: false
            }))
    }

    handleChange(event) {
        this.setState({ depositValue: event.target.value })
    }

    render() {
        const { loading, account, id, msg } = this.state

        if (loading) {
            return (<Loading />)
        }
        if (msg != null) {
            return (<Msg content={msg.content}
                messageClass={msg.messageClass}
                returnPath={`/account/${id}`} />)
        }
        return (
            <div className="container__accounts">
                <h1>Deposit to: {account.title}</h1>
                <div className="container__selected_account">
                    <div className="selected_account__balance">
                        <b>Balance:</b> {account.balance}
                    </div>
                    <div className="selected_account__currency">
                        <b>Currency:</b> {account.currency}
                    </div>
                    <hr className="operation__splitter" />
                    <div className="container__operation" >
                        <div className="operation__name">Enter the amount of the deposit</div>
                        <input className="operation__input" id="deposit" placeholder="e.g. 500"
                            onChange={this.handleChange} />
                    </div>
                </div>
                <button className="button button--execute" onClick={() => { this.executeDeposit() }}>Execute</button>
                <Link className="linkButton button--close" to={`/account/${id}`}>Back</Link>
            </div>
        )
    }
}

export default Deposit