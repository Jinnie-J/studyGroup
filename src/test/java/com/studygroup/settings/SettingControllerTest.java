package com.studygroup.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studygroup.WithAccount;
import com.studygroup.account.AccountRepository;
import com.studygroup.account.AccountService;
import com.studygroup.domain.Account;
import com.studygroup.domain.Tag;
import com.studygroup.domain.Zone;
import com.studygroup.settings.form.TagForm;
import com.studygroup.settings.form.ZoneForm;
import com.studygroup.tag.TagRepository;
import com.studygroup.zone.ZoneRepository;
import org.h2.engine.Setting;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sun.reflect.annotation.ExceptionProxy;

import javax.print.attribute.standard.Media;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class SettingControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ObjectMapper objectMapper;
    @Autowired TagRepository tagRepository;
    @Autowired AccountService accountService;
    @Autowired ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder().city("text").localNameOfCity("????????????").province("????????????").build();

    @BeforeEach
    void beforeEach(){
        zoneRepository.save(testZone);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
    }

    @WithAccount("jinnie")
    @DisplayName("????????? ?????? ?????? ?????? ???")
    @Test
    void updateZonesForm() throws Exception{
        mockMvc.perform(get(SettingsController.SETTINGS_ZONES_URL))
                .andExpect(view().name(SettingsController.SETTINGS_ZONES_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }

    @WithAccount("jinnie")
    @DisplayName("????????? ?????? ?????? ??????")
    @Test
    void addZone() throws Exception{
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(SettingsController.SETTINGS_ZONES_URL+"/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Account jinnie = accountRepository.findByNickname("jinnie");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertTrue(jinnie.getZones().contains(zone));
    }

    @WithAccount("jinnie")
    @DisplayName("????????? ?????? ?????? ??????")
    @Test
    void removeZone() throws Exception{
        Account jinnie = accountRepository.findByNickname("jinnie");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        accountService.addZone(jinnie, zone);

        ZoneForm zoneForm=new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(SettingsController.SETTINGS_ZONES_URL+"/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());
        assertFalse(jinnie.getZones().contains(zone));
    }

    @WithAccount("jinnie")
    @DisplayName("?????? ?????? ???")
    @Test
    void updateTagsForm() throws Exception{
        mockMvc.perform(get(SettingsController.SETTINGS_TAGS_URL))
                .andExpect(view().name(SettingsController.SETTINGS_TAGS_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));
    }

    @WithAccount("jinnie")
    @DisplayName("????????? ?????? ??????")
    @Test
    void addTag() throws Exception{
        TagForm tagForm= new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL+"/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag= tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        Account jinnie = accountRepository.findByNickname("jinnie");
        assertTrue(jinnie.getTags().contains(newTag));

    }

    @WithAccount("jinnie")
    @DisplayName("????????? ?????? ??????")
    @Test
    void removeTag() throws Exception {
        Account jinnie= accountRepository.findByNickname("jinnie");
        Tag newTag= tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(jinnie, newTag);

        assertTrue(jinnie.getTags().contains(newTag));

        TagForm tagForm= new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL+"/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(jinnie.getTags().contains(newTag));
    }

    @WithAccount("jinnie")
    @DisplayName("????????? ?????????")
    @Test
    void updateProfileForm() throws Exception{
        String bio="?????? ????????? ???????????? ??????";
        mockMvc.perform(get(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));

    }


    @WithAccount("jinnie")
    @DisplayName("????????? ???????????? - ????????? ??????")
    @Test
    void updateProfile() throws Exception{
        String bio="?????? ????????? ???????????? ??????";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
            .param("bio",bio)
            .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(SettingsController.SETTINGS_PROFILE_URL))
            .andExpect(flash().attributeExists("message"));

        Account jinnie = accountRepository.findByNickname("jinnie");
        assertEquals(bio, jinnie.getBio());
    }

    @WithAccount("jinnie")
    @DisplayName("????????? ???????????? - ????????? ??????")
    @Test
    void updateProfile_error() throws Exception{
        String bio="?????? ????????? ???????????? ??????. ?????? ????????? ???????????? ??????. ?????? ????????? ???????????? ??????. ?????? ????????? ???????????? ??????. ?????? ????????? ???????????? ??????.";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio",bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account jinnie = accountRepository.findByNickname("jinnie");
        assertNull(jinnie.getBio());
    }

    @WithAccount("jinnie")
    @DisplayName("???????????? ?????? ???")
    @Test
    void updatePassword_form() throws Exception{
        mockMvc.perform(get(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount("jinnie")
    @DisplayName("???????????? ?????? - ????????? ??????")
    @Test
    void updatePassword_success() throws Exception{
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword","12345678")
                .param("newPasswordConfirm","12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(flash().attributeExists("message"));

        Account jinnie = accountRepository.findByNickname("jinnie");
        assertTrue(passwordEncoder.matches("12345678", jinnie.getPassword()));
    }

    @WithAccount("jinnie")
    @DisplayName("???????????? ?????? - ????????? ?????? - ???????????? ?????????")
    @Test
    void updatePassword_fail() throws Exception{
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword","12345678")
                .param("newPasswordConfirm","11111111")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));
    }

    @WithAccount("jinnie")
    @DisplayName("????????? ?????? ???")
    @Test
    void updateAccountForm() throws Exception{
        mockMvc.perform(get(SettingsController.SETTINGS_ACCOUNT_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @WithAccount("jinnie")
    @DisplayName("????????? ???????????? - ????????? ??????")
    @Test
    void updateAccount_success() throws Exception{
        String newNickname="jinnie2";
        mockMvc.perform(post(SettingsController.SETTINGS_ACCOUNT_URL)
                .param("nickname", newNickname)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_ACCOUNT_URL))
                .andExpect(flash().attributeExists("message"));

        assertNotNull(accountRepository.findByNickname("jinnie2"));
    }

    @WithAccount("jinnie")
    @DisplayName("????????? ???????????? - ????????? ??????")
    @Test
    void updateAccount_failure() throws Exception{
        String newNickname="-\\_//";
        mockMvc.perform(post(SettingsController.SETTINGS_ACCOUNT_URL)
                .param("nickname",newNickname)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_ACCOUNT_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }

}
