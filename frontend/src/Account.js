import React from 'react'
import { Link } from 'react-router-dom'
import './Account.css'

const Account = ({ title, balance, currency }) => (
  <div className="container__account">
    <div className="container__account__title">
      <b>Name:</b> {title}
    </div>
    <div className="container__account__balance">
      <b>Balance:</b> {balance}
    </div>
    <div className="container__account__currency">
      <b>Currency:</b> {currency}
    </div>
  </div>
)

export default Account
