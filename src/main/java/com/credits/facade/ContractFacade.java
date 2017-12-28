package com.credits.facade;

import com.credits.dao.ContractDao;
import com.credits.vo.contract.Contract;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Component
public class ContractFacade {

    private final static Logger logger = LoggerFactory.getLogger(ContractFacade.class);

    @Resource
    private ContractDao contractDao;

    public String get(String json) {
        logger.info("Converting json...");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Contract contract = objectMapper.readValue(json, Contract.class);
            contract = contractDao.read(contract.getId());
            return objectMapper.writeValueAsString(contract);
        } catch (IOException e) {
            logger.error("Cannot parse json.", e);
        }

        return null;
    }

    public void update(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        Contract contract = null;
        try {
            contract = objectMapper.readValue(json, Contract.class);
        } catch (IOException e) {
            logger.error("Cannot parse json.", e);
        }
        contractDao.update(contract);
    }

    public void insert(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        Contract contract = null;
        try {
            contract = objectMapper.readValue(json, Contract.class);
        } catch (IOException e) {
            logger.error("Cannot parse json.", e);
        }
        contractDao.insert(contract);
    }
}
