import React from 'react'
import { Switch, Link, Route } from 'react-router-dom'
import AccountsMenu from './AccountsMenu'
import ShowAccounts from './ShowAccounts'
import AccountDetails from './AccountDetails'
import './MainMenu.css'
import Loading from './Loading'
import Deposit from './Deposit'
import Withdraw from './Withdraw'
import Message from './Message'
import NewAccount from './NewAccount'
import RemoveAccountsMenu from './RemoveAccountsMenu'
import removeAccount from './RemoveAccount'

import axios from 'axios'

const Menu = (match) => (
    <div className="main-menu" >
        <Link className="linkButton button--primary" to="/main">Accounts</Link>
        <a className="linkButton button--primary" href="/logout">Log out</a>
        <Switch>
            <Route exact path="/main" component={AccountsMenu} />
            <Route exact path="/accounts" component={ShowAccounts} />
            <Route exact path="/accounts/:id/" component={AccountDetails} />
            <Route exact path="/accounts/:id/deposit" component={Deposit} />
            <Route exact path="/accounts/:id/withdraw" component={Withdraw} />
            <Route exact path="/new" component={NewAccount} />
            <Route exact path="/delete" component={RemoveAccountsMenu} />
            <Route exact path="/accounts/:id/delete" component={removeAccount} />
        </Switch>
    </div>
)

export default Menu