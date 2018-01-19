import React, { Component } from 'react'
import axios from 'axios'
import MockAdapter from 'axios-mock-adapter'
import { BrowserRouter as Router } from 'react-router-dom'
import MainMenu from './components/Menus/MainMenu'
import './App.css'
import './Responsiveness.css'


const mock = new MockAdapter(axios)

if (process.env.NODE_ENV === 'development') {
    mock
        .onGet('/v1/activity').reply(200, {
            activity: 17
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
        .onGet('/v1/accounts/100').reply(200,
        {
            account: {
                title: "Fund for something",
                balance: 250.50,
                currency: "BGN"
            }
        }
        )
        .onGet('/v1/account/101').reply(200,
        {
            account: {
                title: "Fund for other thing",
                balance: 550,
                currency: "EUR"
            }
        }
        )
        .onPost(/\/v1\/accounts\/d+\/deposit/, {
            params: {
                value: 100
            }
        }).reply(200,
        {
            message: "Operation successful"
        })
        .onPost('/v1/accounts/100/deposit', {
            params: {
                value: 100
            }
        }).reply(200,
        {
            message: "Operation successful"
        })
        .onPost('/v1/accounts/100/deposit').reply(400,
        {
            message: "Operation unsuccessful"
        })
        .onPost('/v1/accounts/*/withdraw', {
            params: {
                value: 100
            }
        }).reply(200,
        {
            message: "Operation successful"
        })
        .onPost('/v1/accounts/*/withdraw', {
            params: {
                id: 101,
                value: 100
            }
        }).reply(200,
        {
            message: "Operation successful"
        })
        .onPost('/v1/accounts/*/withdraw').reply(400,
        {
            message: "Operation unsuccessful"
        })
        .onPost('/v1/accounts', {
            params: {
                title: 'acc',
                currency: 'bgn'
            }
        }).reply(200, {
            message: "New account created successful"
        })
        .onPost('/v1/accounts').reply(400, {
            message: "You already have account with such a title"
        })
        .onDelete('/v1/accounts/*').reply(200, {
            message: "Operation successful"
        })
        .onDelete('/v1/accounts/*').reply(400, {
            message: "Error with the server"
        })
        .onGet('/v1/transactions/1?pageSize=20').reply(200, {
            transactions: [
                {
                    onDate: "Jan 27, 2018 12:47:47 AM",
                    title: "Some fund",
                    currency: "BGN",
                    operation: 'DEPOSIT',
                    amount: 250.57
                },
                {
                    onDate: "Jan 27, 2018 1:53:28 PM",
                    title: "Another fund",
                    currency: "EUR",
                    operation: 'WITHDRAW',
                    amount: 14000
                },
            ]
        })
        .onGet(/\/v1\/transactions\/\d+/).reply(200, {transactions: [
            {
                onDate: "Jan 27, 2018 1:53:28 PM",
                title: "Some fund",
                currency: "BGN",
                operation: 'WITHDRAW',
                amount: 14000
            }
        ]})
        .onGet('/v1/transactions/count').reply(200, {transactionsCount: 200})
}
else {
    mock.restore()
}

export default class App extends Component {
    constructor() {
        super()
        this.state = {
            username: "",
            activity: 0
        }
    }

    updateActivity = () => {
        axios.get('/v1/activity')
            .then(response => this.setState({ activity: response.data.activity }))
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
                        <h1 className="greetings__element">{this.state.activity} active users</h1>
                    </div>
                    <MainMenu />
                </div>
            </Router>
        )
    }
}
