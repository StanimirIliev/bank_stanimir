import React, { Component } from 'react'
import './App.css'
import axios from 'axios'
import MockAdapter from 'axios-mock-adapter'
import { BrowserRouter as Router } from 'react-router-dom'
import MainMenu from './MainMenu'


const mock = new MockAdapter(axios, { delayResponse: 0 })

if (process.env.NODE_ENV === 'development') {
    mock
        .onGet('/v1/activeUsers').reply(200, {
            activeUsers: 17
        })
        .onGet('/v1/username').reply(200, {
            username: "user"
        })
        // .onGet('/v1/accounts').reply(200, {
        //   error: "Error occurred while getting cookie"
        // })
        .onGet('/v1/accounts').reply(200, {
            content: [
                {
                    title: "Fund for something",
                    balance: 250.50,
                    currency: "BGN",
                    id: 100
                },
                {
                    title: "Fund for other thing",
                    balance: 550,
                    currency: "EUR",
                    id: 101
                }
            ]
        })
        // .onGet('/v1/accounts').reply(200, {
        //     content: []
        // })
        .onGet('/v1/account', { params: { id: 100 } }).reply(200,
        {
            account: {
                title: "Fund for something",
                balance: 250.50,
                currency: "BGN"
            }
        }
        )
        .onGet('/v1/account', { params: { id: 101 } }).reply(200,
        {
            account: {
                title: "Fund for other thing",
                balance: 550,
                currency: "EUR"
            }
        }
        )
        /*
        axios.get('/v1/executeDeposit', { params: { id: this.state.id, value: deposit } })
            .then(resp => this.setState({msg: resp.data.msg, loading: false}))
            .catch(error => this.setState({msg: error.data.msg, loading: false}))
        */
        .onGet('/v1/executeDeposit', {
            params: {
                id: 100,
                value: 100
            }
        }).reply(200,
        {
            msg: "Operation successful"
        })
        .onGet('/v1/executeDeposit', {
            params: {
                id: 101,
                value: 100
            }
        }).reply(200,
        {
            msg: "Operation successful"
        })
        .onGet('/v1/executeDeposit').reply(400,
        {
            msg: "Operation unsuccessful"
        })
        .onGet('/v1/executeWithdraw', {
            params: {
                id: 100,
                value: 100
            }
        }).reply(200,
        {
            msg: "Operation successful"
        })
        .onGet('/v1/executeWithdraw', {
            params: {
                id: 101,
                value: 100
            }
        }).reply(200,
        {
            msg: "Operation successful"
        })
        .onGet('/v1/executeWithdraw').reply(400,
        {
            msg: "Operation unsuccessful"
        })
        .onGet('/v1/newAccount', {
            params: {
                title: 'acc',
                currency: 'bgn'
            }
        }).reply(200, {
            msg: "Operation successful"
        })
        .onGet('/v1/newAccount').reply(400, {
            msg: "You already have account with such a title"
        })
        .onGet('/v1/removeAccount', {
            params: {
                id: 100
            }
        }).reply(200, {
            msg: "Operation successful"
        })
        .onGet('/v1/removeAccount').reply(400, {
            msg: "Error with the server"
        })
}

export default class App extends Component {
    constructor() {
        super()
        this.state = {
            username: "",
            activeUsers: 0
        }
    }

    updateActivity = () => {
        axios.get('/v1/activeUsers')
            .then(response => this.setState({ activeUsers: response.data.activeUsers }))
            .catch(error => console.log(error))
    }

    componentDidMount() {
        setInterval(this.updateActivity, 2000)
        axios.get('/v1/username')
            .then(response => this.setState({ username: response.data.username }))
            .catch(error => console.log(error))
    }
    render() {
        return (
            <Router>
                <div className="container" >
                    <h1 className="message--greeting" >Welcome {this.state.username}</h1>
                    <h1 className="message--active-users" >At this moment {this.state.activeUsers} users uses this site</h1>
                    <MainMenu />
                </div>
            </Router>
        )
    }
}
