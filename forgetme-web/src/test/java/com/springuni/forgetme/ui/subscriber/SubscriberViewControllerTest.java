package com.springuni.forgetme.ui.subscriber;

import static com.springuni.forgetme.subscriber.Mocks.EMAIL;
import static com.springuni.forgetme.subscriber.Mocks.createSubscriber;
import static com.springuni.forgetme.ui.subscriber.SubscriberViewController.MODEL_NAME;
import static com.springuni.forgetme.ui.subscriber.SubscriberViewController.VIEW_NAME;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.springuni.forgetme.core.model.EntityNotFoundException;
import com.springuni.forgetme.subscriber.model.Subscriber;
import com.springuni.forgetme.subscriber.service.SubscriberService;
import com.springuni.forgetme.ui.subscriber.SubscriberViewControllerTest.TestConfig;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfig.class)
@WebMvcTest(controllers = SubscriberViewController.class)
public class SubscriberViewControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private SubscriberService subscriberService;

  private Subscriber subscriber;

  @Before
  public void setUp() {
    subscriber = createSubscriber();
  }

  @Test
  @WithMockUser(EMAIL)
  public void givenExistingSubscriber_whenLoadSubscriber_thenModelContainsIt() throws Exception {
    given(subscriberService.findSubscriber(EMAIL)).willReturn(Optional.of(subscriber));

    mockMvc.perform(get("/pages/subscriber"))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attributeExists(MODEL_NAME))
        .andDo(print());
  }

  @Test
  @WithMockUser(EMAIL)
  public void givenNonExistingSubscriber_whenLoadSubscriber_thenModelDoesNotContainIt()
      throws Exception {

    given(subscriberService.findSubscriber(EMAIL)).willReturn(Optional.empty());

    mockMvc.perform(get("/pages/subscriber"))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attributeDoesNotExist(MODEL_NAME))
        .andDo(print());
  }

  @Test
  public void givenUnauthenticated_whenLoadSubscriber_thenUnauthorized()
      throws Exception {

    mockMvc.perform(get("/pages/subscriber"))
        .andExpect(status().isUnauthorized())
        .andDo(print());
  }

  @Test
  @WithMockUser(EMAIL)
  public void givenExistingSubscriber_whenRequestForget_thenModelContainsIt() throws Exception {
    given(subscriberService.findSubscriber(EMAIL)).willReturn(Optional.of(subscriber));

    InOrder inOrder = inOrder(subscriberService);

    mockMvc.perform(post("/pages/subscriber").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attributeExists(MODEL_NAME))
        .andDo(print());

    then(subscriberService).should(inOrder).requestForget(EMAIL);
    then(subscriberService).should(inOrder).findSubscriber(EMAIL);
  }

  @Test
  @WithMockUser(EMAIL)
  public void givenNonExistingSubscriber_whenRequestForget_thenModelDoesNotContainIt()
      throws Exception {

    willThrow(EntityNotFoundException.class).given(subscriberService).requestForget(EMAIL);

    mockMvc.perform(post("/pages/subscriber").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attributeDoesNotExist(MODEL_NAME))
        .andDo(print());

    then(subscriberService).should(never()).findSubscriber(EMAIL);
  }

  @Test
  public void givenUnauthenticated_whenRequestForget_thenUnauthorized()
      throws Exception {

    mockMvc.perform(post("/pages/subscriber").with(csrf()))
        .andExpect(status().isUnauthorized())
        .andDo(print());
  }

  @TestConfiguration
  static class TestConfig {

  }

}