package com.credits.cglib;

import com.credits.service.db.leveldb.LevelDbInteractionService;

public interface SimpleConstructorDelegate {

    Object newInstance(LevelDbInteractionService service);
}
