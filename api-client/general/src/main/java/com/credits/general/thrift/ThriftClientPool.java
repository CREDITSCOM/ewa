package com.credits.general.thrift;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Rustem.Saidaliyev on 22.02.2018.
 *
 * account: https://layer4.fr/blog/2013/11/04/pooling-a-thrift-client/
 */
public class ThriftClientPool<T extends TServiceClient> implements
    AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftClientPool.class);

    private final GenericObjectPool<T> internalPool;

    private GenericObjectPool.Config getPoolConfig() {
        GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
        poolConfig.maxActive = 500;
        poolConfig.minIdle = 500;
        poolConfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
        poolConfig.testOnBorrow = true;
        poolConfig.testWhileIdle = true;
        poolConfig.numTestsPerEvictionRun = 10;
        poolConfig.maxWait = 3000;
        return poolConfig;
    }

    public ThriftClientPool(ClientFactory<T> clientFactory, String host, int port) {
        GenericObjectPool.Config poolConfig = getPoolConfig();
        ProtocolFactory protocolFactory = new BinaryOverSocketProtocolFactory(host, port);
        this.internalPool = new GenericObjectPool<>(new ThriftClientFactory(clientFactory, protocolFactory), poolConfig);
    }


    class ThriftClientFactory extends BasePoolableObjectFactory<T> {

        private final ClientFactory<T> clientFactory;
        private final ProtocolFactory protocolFactory;

        public ThriftClientFactory(ClientFactory<T> clientFactory,
            ProtocolFactory protocolFactory) {
            this.clientFactory = clientFactory;
            this.protocolFactory = protocolFactory;
        }

        @Override
        public T makeObject() {
            try {
                TProtocol protocol = protocolFactory.make();
                return clientFactory.make(protocol);
            } catch (Exception e) {
                LOGGER.warn("whut?", e);
                throw new ThriftClientException(
                    "Can not make a new object for pool", e);
            }
        }

        @Override
        public void destroyObject(T obj) {
            if (obj.getOutputProtocol().getTransport().isOpen()) {
                obj.getOutputProtocol().getTransport().close();
            }
            if (obj.getInputProtocol().getTransport().isOpen()) {
                obj.getInputProtocol().getTransport().close();
            }
        }
    }

    public interface ClientFactory<T> {
        T make(TProtocol tProtocol);
    }

    public interface ProtocolFactory {
        TProtocol make();
    }

    public static class BinaryOverSocketProtocolFactory implements
        ProtocolFactory {

        private final String host;
        private final int port;

        public BinaryOverSocketProtocolFactory(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public TProtocol make() {
            TTransport transport = new TSocket(host, port,30000);
            try {
                transport.open();
            } catch (TTransportException e) {
                LOGGER.warn("whut?", e);
                throw new ThriftClientException("Can not make protocol", e);
            }
            return new TBinaryProtocol(transport);
        }
    }

    public static class ThriftClientException extends RuntimeException {

        public ThriftClientException(String message, Exception e) {
            super(message, e);
        }

    }

    public T getResource() {
        try {
            return internalPool.borrowObject();
        } catch (Exception e) {
            throw new ThriftClientException(
                "Could not getObject a resource from the pool", e);
        }
    }

    public void returnResourceObject(T resource) {
        try {
            internalPool.returnObject(resource);
        } catch (Exception e) {
            throw new ThriftClientException(
                "Could not return the resource to the pool", e);
        }
    }

    public void returnBrokenResource(T resource) {
        returnBrokenResourceObject(resource);
    }

    public void returnResource(T resource) {
        returnResourceObject(resource);
    }

    protected void returnBrokenResourceObject(T resource) {
        try {
            internalPool.invalidateObject(resource);
        } catch (Exception e) {
            throw new ThriftClientException(
                "Could not return the resource to the pool", e);
        }
    }

    public void destroy() {
        close();
    }

    public void close() {
        try {
            internalPool.close();
        } catch (Exception e) {
            throw new ThriftClientException("Could not destroy the pool", e);
        }
    }

    public Integer getNumActive() {
        return internalPool.getNumActive();
    }

    public Integer getNumIdle() {
        return internalPool.getNumIdle();
    }
}