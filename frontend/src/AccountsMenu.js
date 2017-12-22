import React from 'react'
import {Link} from 'react-router-dom'
import './AccountsMenu.css'

const AccountsMenu = () => (
    <div className="container__accounts" >
        <div className="container__options" >
            <Link to="/accounts" className="container__options__item">
                <div className="icon">
                    <img src={require("./icons/list.png")}/>
                </div>
                <div className="item__text">
                    Show my accounts
                </div>
             </Link>
             <Link to="/newAccount" className="container__options__item">
                <div className="icon">
                    <img src={require("./icons/add.png")}/>
                </div>
                <div className="item__text">
                    Add new account
                </div>
             </Link>
             <Link to="/removeAccountsMenu" className="container__options__item">
                <div className="icon">
                    <img src={require("./icons/remove.png")}/>
                </div>
                <div className="item__text">
                    Delete account
                </div>
             </Link>
        </div>
        <Link className="linkButton button--close"  to="/">Close</Link>
    </div>
)

export default AccountsMenu
