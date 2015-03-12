-- name: find-all-users
-- Finds all users in the database.
SELECT *
FROM   users

-- name: find-user-by-id
-- Finds a user by their ID. Returns an empty list if the ID cannot be found.
SELECT *
FROM   users
WHERE  id = :id::uuid

-- name: find-user-by-confirmation-token
-- Finds a user by their confirmation token. If the token cannot be found or is
-- expired, returns an empty list.
SELECT *
FROM   users
WHERE  confirmation_token = :token
       AND confirmation_token_expires_at > now()

-- name: find-user-by-email
-- Finds user by their email address. Returns an empty list if no user is
-- found.
SELECT *
FROM   users
WHERE  email = :email

-- name: find-confirmed-user-by-email
-- Finds a user by their email that has been confirmed. Returns an empty list
-- if no user is found.
SELECT *
FROM   users
WHERE  is_confirmed = TRUE
       AND email = :email

-- name: insert-user!
-- Inserts a new user into the database. Returns the number of records that
-- were inserted.
INSERT INTO users
            (email,
             confirmation_token,
             confirmation_token_expires_at)
VALUES      (:email,
             :confirmation_token,
             :confirmation_token_expires_at)

-- name: update-user-registration!
-- Updates a user by confirming their registration. Returns the number of
-- records that were updated.
UPDATE users
SET    location = :location,
       latitude = :latitude,
       longitude = :longitude,
       password = :password,
       is_confirmed = TRUE,
       confirmed_at = now(),
       confirmation_token = NULL,
       confirmation_token_expires_at = NULL
WHERE  confirmation_token = :confirmation_token
       AND confirmation_token_expires_at > now()
       AND email = :email

-- name: update-user-confirmation-token!
-- Updates the user with a new confirmation token. Returns the number of
-- records that were updated.
UPDATE users
SET    confirmation_token = :confirmation_token,
       confirmation_token_expires_at = :confirmation_token_expires_at
WHERE  is_confirmed = FALSE
       AND email = :email

-- name: update-user-settings!
-- Updates a user's settings. Returns the number of records that were updated.
UPDATE users
SET    location = :location,
       latitude = :latitude,
       longitude = :longitude,
       password = CASE
                    WHEN length(:password) > 0 THEN :password
                    ELSE password
                  END
WHERE  id = :id::uuid

-- name: delete-user!
-- Deletes the user form the database. Returns the number of records that were
-- deleted.
DELETE FROM users
WHERE id = :id::uuid
