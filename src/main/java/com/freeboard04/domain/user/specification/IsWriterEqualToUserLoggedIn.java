package com.freeboard04.domain.user.specification;

import com.freeboard04.domain.user.UserEntity;

public class IsWriterEqualToUserLoggedIn {

    public static boolean confirm(UserEntity writer, UserEntity loginUser) {
        return writer.getAccountId().equals(loginUser.getAccountId()) ;
    }
}
