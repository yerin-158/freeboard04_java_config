package com.freeboard04_java_config.domain.user;

import com.freeboard04_java_config.domain.BaseEntity;
import com.freeboard04_java_config.domain.user.enums.UserRole;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "user")
@NoArgsConstructor
public class UserEntity extends BaseEntity {

    @Column
    private String accountId;

    @Column
    private String password;

    @Setter
    @Column
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Builder
    public UserEntity(String accountId, String password, UserRole role) {
        this.accountId = accountId;
        this.password = password;
        this.role = role;
    }

}
