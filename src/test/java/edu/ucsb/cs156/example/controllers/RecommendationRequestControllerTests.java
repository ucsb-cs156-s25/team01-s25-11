package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

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

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

        @MockBean
        RecommendationRequestRepository RecommendationRequestRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/recommendationrequest/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/recommendationrequest/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/recommendationrequest/all"))
                                .andExpect(status().is(200)); // logged
        }       
        
        // Authorization tests for /api/recommendationrequest/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/recommendationrequest/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/recommendationrequest/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_recommendationrequests() throws Exception {

                // arrange
                LocalDateTime datereq1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime dateneeded1 = LocalDateTime.parse("2023-01-03T00:00:00");

                RecommendationRequest RecommendationRequest1 = RecommendationRequest.builder()
                                .requesterEmail("user1@ucsb.edu")
                                .professorEmail("user2@ucsb.edu")
                                .explanation("recommendation")
                                .dateRequested(datereq1)
                                .dateNeeded(dateneeded1)
                                .done(true)
                                .build();

                LocalDateTime datereq2 = LocalDateTime.parse("2022-01-13T00:00:00");
                LocalDateTime dateneeded2 = LocalDateTime.parse("2023-01-13T00:00:00");

                RecommendationRequest RecommendationRequest2 = RecommendationRequest.builder()
                                .requesterEmail("user3@ucsb.edu")
                                .professorEmail("user4@ucsb.edu")
                                .explanation("recommendation")
                                .dateRequested(datereq2)
                                .dateNeeded(dateneeded2)
                                .done(false)
                                .build();

                ArrayList<RecommendationRequest> expectedDates = new ArrayList<>();
                expectedDates.addAll(Arrays.asList(RecommendationRequest1, RecommendationRequest2));

                when(RecommendationRequestRepository.findAll()).thenReturn(expectedDates);

                // act
                MvcResult response = mockMvc.perform(get("/api/RecommendationRequests/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(RecommendationRequestRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedDates);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_RecommendationRequest() throws Exception {
                // arrange

                LocalDateTime datereq1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime dateneeded1 = LocalDateTime.parse("2023-01-03T00:00:00");

                RecommendationRequest RecommendationRequest1 = RecommendationRequest.builder()
                                .requesterEmail("user1@ucsb.edu")
                                .professorEmail("user2@ucsb.edu")
                                .explanation("recommendation")
                                .dateRequested(datereq1)
                                .dateNeeded(dateneeded1)
                                .done(true)
                                .build();

                when(RecommendationRequestRepository.save(eq(RecommendationRequest1))).thenReturn(RecommendationRequest1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/RecommendationRequests/post?requesterEmail=user1@ucsb.edu&professorEmail=user2@ucsb.edu&explanation=recommendation&dateRequested=2022-01-03T00:00:00&dateNeeded=2023-01-03T00:00:00&done=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(RecommendationRequestRepository, times(1)).save(RecommendationRequest1);
                String expectedJson = mapper.writeValueAsString(RecommendationRequest1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

}