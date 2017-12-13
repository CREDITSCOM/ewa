package com.credits.daoh2;

import com.credits.dao.AbstractDao;
import com.credits.dao.ContractDao;
import com.credits.vo.Contract;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class ContractDaoH2 extends AbstractDao implements ContractDao {

    @Override
    public Contract read(int id) {
        return jdbcTemplate.queryForObject("select * from contracts where id = ?", new Object[]{id},
                BeanPropertyRowMapper.newInstance(Contract.class));
    }

    @Override
    public void update(Contract contract) {
        namedJdbcTemplate.update("update contracts set " +
                "source_account = HASH('SHA256', STRINGTOUTF8(:sourceAccount), 1000), " +
                "sender = :sender, " +
                "transaction_amount = :transactionAmount, " +
                "dest_account = HASH('SHA256', STRINGTOUTF8(:destAccount), 1000)" +
                "where id = :id", new BeanPropertySqlParameterSource(contract));
    }

    @Override
    public void insert(Contract contract) {
        namedJdbcTemplate.update("insert into contracts (source_account, sender, transaction_amount, dest_account)" +
                "values (" +
                "HASH('SHA256', STRINGTOUTF8(:sourceAccount), 1000)" +
                ",:sender" +
                ",:transactionAmount" +
                ",HASH('SHA256', STRINGTOUTF8(:destAccount), 1000))", new BeanPropertySqlParameterSource(contract));
    }
}
