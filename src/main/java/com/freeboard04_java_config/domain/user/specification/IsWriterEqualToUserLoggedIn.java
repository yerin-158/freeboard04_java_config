package com.freeboard04_java_config.domain.user.specification;

import com.freeboard04_java_config.domain.user.UserEntity;

public class IsWriterEqualToUserLoggedIn {

    public static boolean confirm(UserEntity writer, UserEntity loginUser) {
        return writer.getAccountId().equals(loginUser.getAccountId()) ;
    }
}
