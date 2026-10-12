package com.konfyrm.gigatester.users.domain.entity;

/** How an account came to exist. Orthogonal to {@link UserRole}, which says what the account may do. */
public enum UserType {
    /** The person signed up themselves. */
    SELF_REGISTERED,
    /** An admin created the account and chose its username and password. */
    ADMIN_CREATED
}
