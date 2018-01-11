import React from 'react'
import { Link, Route } from 'react-router-dom'
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
import Transactions from './Transactions'

import axios from 'axios'

const Menu = (match) => (
    <div className="main-menu" >
        <Link className="linkButton button--primary" to="/transactions/1">Transactions</Link>
        <Link className="linkButton button--primary" to="/main">Accounts</Link>
        <a className="linkButton button--primary" href="/logout">Log out</a>
        <Route path="/main" component={AccountsMenu} />
        <Route path="/accounts" component={ShowAccounts} />
        <Route path="/account/:id/" component={AccountDetails} />
        <Route path="/deposit/:id/" component={Deposit} />
        <Route path="/withdraw/:id" component={Withdraw} />
        <Route path="/newAccount" component={NewAccount} />
        <Route path="/removeAccounts" component={RemoveAccountsMenu} />
        <Route path="/removeAccount/:id" component={removeAccount} />
        <Route path="/transactions" component={Transactions} />
    </div>
)

export default Menu