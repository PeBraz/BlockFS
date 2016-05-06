package com.blockfs.client.connection;

import com.blockfs.client.exception.ValidationException;
import com.blockfs.client.rest.model.Block;

public interface PoolTask {
    void validation(String id, Block block) throws ValidationException; //used for reads
}
