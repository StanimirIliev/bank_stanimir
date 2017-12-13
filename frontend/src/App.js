import React, {Component} from 'react'
import './App.css'
import axios from 'axios'
import MockAdapter from 'axios-mock-adapter'


const mock = new MockAdapter(axios)

if (process.env.NODE_ENV === 'development') {
    mock
    .onGet('/v1/activeUsers').reply(200, {
        activeUsers: 17
    })
    .onGet('/v1/balance').reply(200, {
        balance: 1200
    })
}
else {
    mock.restore()
}

class App extends Component {
    constructor() {
        super()
        this.state = {
            sessionId: null,
            username: "",
            activeUsers: 0,
            balance: 0
        }
    }

    updateActivity = () => {
        axios.get('/v1/activeUsers')
        .then(response => this.setState({activeUsers: response.data.activeUsers}))
        .catch(error =>console.log(error))
    }

    updateBalance = () => {
        axios.get('/v1/balance')
        .then(response => this.setState({balance: response.data.balance}))
        .catch(error =>console.log(error))
    }

    componentDidMount() {
        setInterval(this.updateActivity, 2000)
        setInterval(this.updateBalance, 1000)

        const cookies = document.cookie
        this.setState({sessionId: cookies.substring(cookies.indexOf('sessionId=') + 10)})
        axios.get('/v1/username')
        .then(response => this.setState({username: response.data.username}))
        .catch(error => console.log(error))
    }
    render() {
        return(
            <div className="container" >
                <h1 className="container__greetings" >Welcome {this.state.username}</h1>
                <h1 className="container__balance" >Your balance is {this.state.balance} lv</h1>
                <h1 className="container__active_users" >At this moment {this.state.activeUsers} users uses this site</h1>
                <button className="button" onClick={() => {
                    document.location.replace('/home?logout')
                }}>Log out</button>
            </div>
        )
    }
}

export default App