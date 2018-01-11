import React, { Component } from 'react'
import './App.css'
import axios from 'axios'
import MockAdapter from 'axios-mock-adapter'
import { BrowserRouter as Router } from 'react-router-dom'
import MainMenu from './MainMenu'
import './Responsiveness.css'


const mock = new MockAdapter(axios)

if (process.env.NODE_ENV === 'development') {
    console.log('Development mode')
    mock
        .onGet('/v1/activeUsers').reply(200, {
            activeUsers: 17
        })
        .onGet('/v1/username').reply(200, {
            username: "user"
        })
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
        .onPost('/v1/executeDeposit', {
            params: {
                id: 100,
                value: 100
            }
        }).reply(200,
        {
            msg: "Operation successful"
        })
        .onPost('/v1/executeDeposit', {
            params: {
                id: 101,
                value: 100
            }
        }).reply(200,
        {
            msg: "Operation successful"
        })
        .onPost('/v1/executeDeposit').reply(400,
        {
            msg: "Operation unsuccessful"
        })
        .onPost('/v1/executeWithdraw', {
            params: {
                id: 100,
                value: 100
            }
        }).reply(200,
        {
            msg: "Operation successful"
        })
        .onPost('/v1/executeWithdraw', {
            params: {
                id: 101,
                value: 100
            }
        }).reply(200,
        {
            msg: "Operation successful"
        })
        .onPost('/v1/executeWithdraw').reply(400,
        {
            msg: "Operation unsuccessful"
        })
        .onPost('/v1/newAccount', {
            params: {
                title: 'acc',
                currency: 'bgn'
            }
        }).reply(200, {
            msg: "Operation successful"
        })
        .onPost('/v1/newAccount').reply(400, {
            msg: "You already have account with such a title"
        })
        .onDelete('/v1/removeAccount', {
            params: {
                id: 100
            }
        }).reply(200, {
            msg: "Operation successful"
        })
        .onDelete('/v1/removeAccount').reply(400, {
            msg: "Error with the server"
        })
}
else {
    mock.restore()
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
                    <img className="logo" src={require("./images/logo.png")} />
                    <div className="greetings">
                        <h1 className="greetings__element">Welcome {this.state.username}</h1>
                        <div className="greetings__element"><hr className="greetings--splitter" /></div>
                        <h1 className="greetings__element">{this.state.activeUsers} active users</h1>
                    </div>
                    <MainMenu />
                </div>
            </Router>
        )
    }
}
