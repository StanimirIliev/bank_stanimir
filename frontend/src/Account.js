import React from 'react'
import { Link } from 'react-router-dom'
import './Account.css'

const Account = ({ title, balance, currency }) => (
  <div className="container__account">
    <div className="container__account__title">
      <b>Name:</b> {title}
    </div>
    <div className="container__account__balance">
      <b>Balance:</b> {
        new Intl.NumberFormat('de-DE', {
          style: 'currency',
          currency: currency
        }).format(balance)
      }
    </div>
  </div>
)

export default Account
