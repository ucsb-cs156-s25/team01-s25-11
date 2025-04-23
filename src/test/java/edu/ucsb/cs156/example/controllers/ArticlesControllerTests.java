package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {

    @MockBean
    ArticlesRepository articlesRepository;

    @MockBean
    UserRepository userRepository;

    @Autowired
    ObjectMapper mapper;

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
            mockMvc.perform(get("/api/articles/all"))
                            .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
            mockMvc.perform(get("/api/articles/all"))
                            .andExpect(status().is(200)); // logged
    }

    @Test
    public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/articles/post"))
                            .andExpect(status().is(403));
    }
    

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/articles/post"))
                            .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_articles() throws Exception {

            // arrange
            LocalDateTime ldt1 = LocalDateTime.parse("2025-04-21T17:00:00");

            Articles article1 = Articles.builder()
                    .title("sl - create database table fot Articles")
                    .url("https://github.com/ucsb-cs156-s25/team01-s25-11/commit/8e28ad3216e5156bb0ffbd67164f761635920f41")
                    .explanation("commit database table")
                    .email("shuang_li@ucsb.edu")
                    .dateAdded(ldt1)
                    .build();

            ArrayList<Articles> expectedArticles = new ArrayList<>();
            expectedArticles.add(article1);

            when(articlesRepository.findAll()).thenReturn(expectedArticles);

            // act
            MvcResult response = mockMvc.perform(get("/api/articles/all"))
                            .andExpect(status().isOk()).andReturn();

            // assert

            verify(articlesRepository, times(1)).findAll();
            String expectedJson = mapper.writeValueAsString(expectedArticles);

            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_user_can_post_new_article() throws Exception {
        // arrange
        LocalDateTime ldt1 = LocalDateTime.parse("2025-04-21T17:00:00");
    
        Articles article1 = Articles.builder()
                .title("sl - create database table for Articles")
                .url("https://github.com/ucsb-cs156-s25/team01-s25-11/commit/8e28ad3216e5156bb0ffbd67164f761635920f41")
                .explanation("sl-updated")
                .email("shuang_li@ucsb.edu")
                .dateAdded(ldt1)
                .build();
    
        when(articlesRepository.save(any(Articles.class))).thenReturn(article1);
    
        // act
        MvcResult response = mockMvc.perform(
                post("/api/articles/post?message=sl-updated" +
                     "&url=https://github.com/ucsb-cs156-s25/team01-s25-11/commit/8e28ad3216e5156bb0ffbd67164f761635920f41" +
                     "&title=sl%20-%20create%20database%20table%20for%20Articles" +
                     "&email=shuang_li@ucsb.edu" +
                     "&explanation=sl-updated" +
                     "&dateAdded=2025-04-21T17:00:00")
                    .with(csrf()))
                .andExpect(status().isOk()).andReturn();
        
    
        // assert
        verify(articlesRepository, times(1)).save(any(Articles.class));
        String expectedJson = mapper.writeValueAsString(article1);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }
    


}
