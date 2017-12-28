package com.credits.daoh2;

import com.credits.vo.contract.Contract;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

public class ContractDaoH2Test {

    private EmbeddedDatabase db;
    private ContractDaoH2 contractDao;


    @Before
    public void setUp() {
        db = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("db/sql/create-db.sql")
                .addScript("db/sql/insert-data.sql")
                .build();

        contractDao = new ContractDaoH2();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(db);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(db);

        contractDao.setJdbcTemplate(jdbcTemplate);
        contractDao.setNamedJdbcTemplate(namedJdbcTemplate);
    }

    @Test
    public void testFindById() {
        Contract contract = contractDao.read(1);

        Assert.assertNotNull(contract);
        Assert.assertEquals(1, contract.getId());
        Assert.assertEquals("akrasnov@gmail.com", contract.getSender());
    }

    @Test
    public void update() {
        Contract contract = contractDao.read(2);
        contract.setSender("akrasnov@gmail.com");
        contract.setSourceAccount("akrasnov@gmail.com");
        contractDao.update(contract);

        contract = contractDao.read(2);

        Assert.assertNotNull(contract);
        Assert.assertEquals("akrasnov@gmail.com", contract.getSender());
    }

    @Test
    public void insert() {
        Contract contract = contractDao.read(1);
        contractDao.insert(contract);
        contract = contractDao.read(4);
        Assert.assertNotNull(contract);
        Assert.assertEquals(4, contract.getId());
        Assert.assertEquals("akrasnov@gmail.com", contract.getSender());
    }

    @After
    public void tearDown() {
        db.shutdown();
    }
}
