package com.studygroup.account;

import com.studygroup.domain.Account;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Arrays;

@Getter
public class UserAccount extends User {

    private Account account;

    public UserAccount(Account account){
        super(account.getNickname(), account.getPassword(), Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
        this.account= account;
    }
}
