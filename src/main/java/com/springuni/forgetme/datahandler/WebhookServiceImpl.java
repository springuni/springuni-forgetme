package com.springuni.forgetme.datahandler;

import com.springuni.forgetme.core.model.EntityNotFoundException;
import java.util.Map;
import java.util.UUID;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class WebhookServiceImpl implements WebhookService {

  static final String DATA_HANDLER_ID = "_data_handler_id";
  static final String DATA_HANDLER_NAME = "_data_handler_name";

  private final DataHandlerRepository dataHandlerRepository;
  private final MessageChannel webhookInboundChannler;

  public WebhookServiceImpl(
      DataHandlerRepository dataHandlerRepository,
      MessageChannel webhookInboundChannler) {

    this.dataHandlerRepository = dataHandlerRepository;
    this.webhookInboundChannler = webhookInboundChannler;
  }

  @Override
  public void submitData(UUID dataHandlerId, UUID key, Map<String, Object> data) {
    DataHandler dataHandler = dataHandlerRepository
        .findById(dataHandlerId)
        .orElseThrow(() -> new EntityNotFoundException("id", dataHandlerId));

    // For security reasons, it's safer to throw EntityNotFoundException, which will be exposed as
    // HTTP 404, so that an attacker couldn't differentiate a non-existent ID from a bad key.
    if (!dataHandler.getKey().equals(key)) {
      throw new EntityNotFoundException("key", key);
    }

    Message<Map<String, Object>> message = MessageBuilder
        .withPayload(data)
        .setHeader(DATA_HANDLER_ID, dataHandlerId)
        .setHeader(DATA_HANDLER_NAME, dataHandler.getName())
        .build();

    webhookInboundChannler.send(message);
  }

}
