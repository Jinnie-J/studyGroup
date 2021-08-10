package com.studygroup.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studygroup.account.AccountService;
import com.studygroup.account.CurrentAccount;
import com.studygroup.domain.Account;
import com.studygroup.domain.Tag;
import com.studygroup.domain.Zone;
import com.studygroup.settings.form.*;
import com.studygroup.settings.validator.NicknameValidator;
import com.studygroup.settings.validator.PasswordFormValidator;
import com.studygroup.tag.TagRepository;
import com.studygroup.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    @InitBinder("passwordForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("nicknameForm")
    public void nicknameFormInitBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(nicknameValidator);
    }

    static final String SETTINGS_PROFILE_VIEW_NAME = "settings/profile";
    static final String SETTINGS_PROFILE_URL = "/"+ SETTINGS_PROFILE_VIEW_NAME;

    static final String SETTINGS_PASSWORD_VIEW_NAME = "settings/password";
    static final String SETTINGS_PASSWORD_URL = "/"+SETTINGS_PASSWORD_VIEW_NAME;

    static final String SETTINGS_NOTIFICATIONS_VIEW_NAME="settings/notifications";
    static final String SETTINGS_NOTIFICATIONS_URL="/"+SETTINGS_NOTIFICATIONS_VIEW_NAME;

    static final String SETTINGS_ACCOUNT_VIEW_NAME="settings/account";
    static final String SETTINGS_ACCOUNT_URL="/"+SETTINGS_ACCOUNT_VIEW_NAME;

    static final String SETTINGS_TAGS_VIEW_NAME ="settings/tags";
    static final String SETTINGS_TAGS_URL= "/"+ SETTINGS_TAGS_VIEW_NAME;

    static final String SETTINGS_ZONES_VIEW_NAME ="settings/zones";
    static final String SETTINGS_ZONES_URL= "/"+ SETTINGS_ZONES_VIEW_NAME;

    private final AccountService accountService;
    private final ModelMapper modelMapper;
    private final NicknameValidator nicknameValidator;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;
    private final ZoneRepository zoneRepository;

    @GetMapping(SETTINGS_PROFILE_URL)
    public String updateProfileForm(@CurrentAccount Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Profile.class));
        return SETTINGS_PROFILE_VIEW_NAME;
    }

    @PostMapping(SETTINGS_PROFILE_URL)
    public String updateProfile(@CurrentAccount Account account, @Valid @ModelAttribute Profile profile, Errors errors,
                                Model model, RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return SETTINGS_PROFILE_VIEW_NAME;
        }
        accountService.updateProfile(account, profile);
        attributes.addFlashAttribute("message","프로필을 수정했습니다.");
        return "redirect:" + SETTINGS_PROFILE_URL;
    }

    @GetMapping(SETTINGS_PASSWORD_URL)
    public String UpdatePasswordForm(@CurrentAccount Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return SETTINGS_PASSWORD_VIEW_NAME;
    }

    @PostMapping(SETTINGS_PASSWORD_URL)
    public String updatePassword(@CurrentAccount Account account, @Valid PasswordForm passwordform, Errors errors,
                                 Model model, RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return SETTINGS_PASSWORD_VIEW_NAME;
        }
        accountService.updatePassword(account, passwordform.getNewPassword());
        attributes.addFlashAttribute("message","패스워드를 변경했습니다.");
        return "redirect:"+ SETTINGS_PASSWORD_URL;

    }

    @GetMapping(SETTINGS_NOTIFICATIONS_URL)
    public String updateNotificationsForm(@CurrentAccount Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Notifications.class));
        return SETTINGS_NOTIFICATIONS_VIEW_NAME;
    }

    @PostMapping(SETTINGS_NOTIFICATIONS_URL)
    public String updateNotifications(@CurrentAccount Account account, @Valid Notifications notifications, Errors errors,
                                      Model model, RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return SETTINGS_NOTIFICATIONS_VIEW_NAME;
        }
        accountService.updateNotifications(account, notifications);
        attributes.addFlashAttribute("message","알림 설정을 변경했습니다.");
        return "redirect:" + SETTINGS_NOTIFICATIONS_URL;
    }

    @GetMapping(SETTINGS_TAGS_URL)
    public String updateTags(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        Set<Tag> tags = accountService.getTags(account);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));
        return SETTINGS_TAGS_VIEW_NAME;
    }

    @PostMapping(SETTINGS_TAGS_URL+"/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentAccount Account account, @RequestBody TagForm tagForm){
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if(tag == null){
            tag= tagRepository.save(Tag.builder().title(title).build());
        }
        accountService.addTag(account, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping(SETTINGS_TAGS_URL+"/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentAccount Account account, @RequestBody TagForm tagForm){
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);

        if(tag == null){
            return ResponseEntity.badRequest().build();
        }
        accountService.removeTag(account,tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping(SETTINGS_ZONES_URL)
    public String updateZonesForm(@CurrentAccount Account account, Model model) throws JsonProcessingException{
        model.addAttribute(account);

        Set<Zone> zones= accountService.getZones(account);
        model.addAttribute("zones", zones.stream().map(Zone::toString).collect(Collectors.toList()));

        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));

        return SETTINGS_ZONES_VIEW_NAME;
    }

    @PostMapping(SETTINGS_ZONES_URL+ "/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm){
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(),zoneForm.getProvinceName());
        if(zone == null){
            return ResponseEntity.badRequest().build();
        }
        accountService.addZone(account, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping(SETTINGS_ZONES_URL+"/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm){
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if( zone == null){
            return ResponseEntity.badRequest().build();
        }
        accountService.removeZone(account, zone);
        return ResponseEntity.ok().build();
    }

    @GetMapping(SETTINGS_ACCOUNT_URL)
    public String updateAccountForm(@CurrentAccount Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NicknameForm.class));
        return SETTINGS_ACCOUNT_VIEW_NAME;
    }

    @PostMapping(SETTINGS_ACCOUNT_URL)
    public String updateAccount(@CurrentAccount Account account, @Valid NicknameForm nicknameForm,
                                Errors errors, Model model, RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return SETTINGS_ACCOUNT_VIEW_NAME;
        }
        accountService.updateNickname(account, nicknameForm.getNickname());
        attributes.addFlashAttribute("message","닉네임을 수정했습니다.");
        return "redirect:" + SETTINGS_ACCOUNT_URL;
    }
}
