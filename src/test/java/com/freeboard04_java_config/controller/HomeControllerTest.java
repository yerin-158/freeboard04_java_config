package com.freeboard04_java_config.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freeboard04_java_config.api.user.UserForm;
import com.freeboard04_java_config.config.ApplicationConfig;
import com.freeboard04_java_config.config.WebConfig;
import com.freeboard04_java_config.domain.user.UserEntity;
import com.freeboard04_java_config.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ApplicationConfig.class, WebConfig.class})
@Transactional
@WebAppConfiguration
@Rollback(value = false)
public class HomeControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void initMvc() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("로그아웃과 동시에 세션이 지워진다.")
    public void logoutTest1() throws Exception {
        UserEntity userEntity = userRepository.findAll().get(0);
        UserForm userForm = UserForm.builder().accountId(userEntity.getAccountId()).password(userEntity.getPassword()).build();
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute("USER", userForm);

        mvc.perform(get("/logout")
                .session(mockHttpSession))
                .andExpect(request().sessionAttribute("USER", nullValue()))
                .andExpect(status().isOk());
    }

}
