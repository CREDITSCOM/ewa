package com.credits.dao;

import com.credits.vo.Contract;

public interface ContractDao {

    Contract read(int i);

    void update(Contract contract);

    void insert(Contract contract);
}
