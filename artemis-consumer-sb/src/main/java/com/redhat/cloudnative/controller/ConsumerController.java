package com.redhat.cloudnative.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/consumer")
public class ConsumerController {

    private static final String JMS_MQ = "jms-artemis";

    private final Logger logger = LoggerFactory.getLogger(ConsumerController.class);
    
    @ResponseBody
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String getTest() {

        return "Ok";
    }

    @JmsListener(destination = JMS_MQ)
    public void atualizaCacheJms(String message) {
        logger.info("[mensagem] Mensagem recebida ({})", message);
    }

}
