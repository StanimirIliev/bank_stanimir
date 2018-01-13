CREATE TABLE IF NOT EXISTS Users(
    Id INT NOT NULL AUTO_INCREMENT,
    Username VARCHAR(255) NOT NULL UNIQUE,
    Password VARCHAR(255) NOT NULL,
    Salt VARCHAR(255) NOT NULL,
    PRIMARY KEY(Id)
);
CREATE TABLE IF NOT EXISTS Accounts (
    Id INT NOT NULL AUTO_INCREMENT,
    Title VARCHAR(255) NOT NULL UNIQUE,
    UserId INT NOT NULL,
    Currency ENUM('BGN', 'EUR') NOT NULL,
    Balance FLOAT NOT NULL,
    PRIMARY KEY(Id),
    FOREIGN KEY(UserId) REFERENCES Users(Id)
    ON UPDATE CASCADE ON DELETE CASCADE,
    UNIQUE KEY UserIdAndTitle (UserId, Title)
);
CREATE TABLE IF NOT EXISTS Sessions(
    Id VARCHAR(255) NOT NULL,
    UserId INT NOT NULL,
    CreatedOn TIMESTAMP NOT NULL,
    ExpiresAt TIMESTAMP NOT NULL DEFAULT '2038-01-19 03:14:07',
    PRIMARY KEY(Id),
    FOREIGN KEY(UserId) REFERENCES Users(Id)
    ON UPDATE CASCADE ON DELETE CASCADE,
    UNIQUE KEY UserIdAndCreatedOn (UserId, CreatedOn)
);
CREATE TABLE IF NOT EXISTS Transactions(
    Id INT NOT NULL AUTO_INCREMENT,
    UserId INT NOT NULL,
    AccountId INT NOT NULL,
    OnDate TIMESTAMP NOT NULL,
    Operation ENUM('DEPOSIT', 'WITHDRAW') NOT NULL,
    Amount FLOAT NOT NULL,
    PRIMARY KEY(Id),
    FOREIGN KEY(UserId) REFERENCES Users(Id)
    ON UPDATE CASCADE ON DELETE CASCADE,
    UNIQUE KEY UserIdAccountIdAndOnDate (UserId, AccountId, OnDate)
);