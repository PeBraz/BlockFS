package com.blockfs.client.connection;

import com.blockfs.client.exception.ValidationException;

public interface PoolTask {
    void validation() throws ValidationException;
}
