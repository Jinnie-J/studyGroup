package com.studygroup.account;

import com.studygroup.domain.Account;
import com.studygroup.domain.Tag;
import com.studygroup.domain.Zone;
import com.studygroup.settings.form.Notifications;
import com.studygroup.settings.form.Profile;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.Arrays;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;


@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Transactional
    public Account processNewAccount(SignUpForm signUpForm){
        Account newAccount = saveNewAccount(signUpForm);
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    private Account saveNewAccount(@Valid  SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = modelMapper.map(signUpForm, Account.class);
        account.generateEmailCheckToken();
        return accountRepository.save(account);
    }

    public void sendSignUpConfirmEmail(Account newAccount) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("회원 가입 인증");
        mailMessage.setText("/check-email-token?token="+ newAccount.getEmailCheckToken()+"&email="+ newAccount.getEmail());
        javaMailSender.send(mailMessage);
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(emailOrNickname);
        if(account== null){
            account= accountRepository.findByNickname(emailOrNickname);
        }

        if(account==null){
            throw new UsernameNotFoundException(emailOrNickname);
        }
        return new UserAccount(account);
    }

    public void completeSignUp(Account account){
        account.completeSignUp();
        login(account);
    }


    public void updateProfile(Account account, Profile profile) {
        modelMapper.map(profile,account);
        accountRepository.save(account);
    }

    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    public void updateNotifications(Account account, Notifications notifications) {
        modelMapper.map(notifications,account);
        accountRepository.save(account);
    }

    public void updateNickname(Account account, String nickname) {
        account.setNickname(nickname);
        accountRepository.save(account);
        login(account);
    }

    public void sendLoginLink(Account account) {
        account.generateEmailCheckToken();
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(account.getEmail());
        mailMessage.setSubject("스터디그룹, 로그인 링크");
        mailMessage.setText("/login-by-email?token="+ account.getEmailCheckToken()+
                "&email="+account.getEmail());
        javaMailSender.send(mailMessage);
    }

    public void addTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().add(tag));
    }

    public Set<Tag> getTags(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        // return byId.orElseThrow().getTags();
        return Optional.ofNullable(byId).orElseThrow(NoSuchElementException::new).get().getTags();
    }

    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a ->  a.getTags().remove(tag));
    }

    public Set<Zone> getZones(Account account){
        Optional<Account> byId = accountRepository.findById(account.getId());
        return Optional.ofNullable(byId).orElseThrow(NoSuchElementException::new).get().getZones();
    }

    public void addZone(Account account, Zone zone){
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().add(zone));
    }

    public void removeZone(Account account, Zone zone){
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().remove(zone));
    }
}
