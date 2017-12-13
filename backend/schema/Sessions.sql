CREATE TABLE Sessions(
    SessionId VARCHAR(255) NOT NULL,
    Username VARCHAR(255) NOT NULL,
    ExpiresAt TIMESTAMP NOT NULL,
    PRIMARY KEY(SessionId),
    FOREIGN KEY(Username) REFERENCES Users(Username)
)