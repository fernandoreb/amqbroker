package com.redhat.cloudnative.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/producer")  // <1>
public class ProducerController {

    private static final String JMS_MQ = "jms-artemis";
    private final Logger logger = LoggerFactory.getLogger(ProducerController.class);

    @Autowired
    private JmsTemplate jmsTemplate;


    @ResponseBody
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public String sendMessage(@RequestBody String message) {
        
        this.jmsTemplate.convertAndSend(JMS_MQ, message);
        
        return "Ok";
    }
}