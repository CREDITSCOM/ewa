package com.credits.client.node.util;


import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.service.NodeApiService;
import com.credits.general.util.Converter;
import com.credits.general.util.exception.ConverterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


public class TransactionIdCalculateUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(TransactionIdCalculateUtils.class);
    public static ConcurrentHashMap<String, AtomicLong> walletLastTransactionIdCache = new ConcurrentHashMap<>();

    private static long createTransactionId(boolean senderIndexExists, boolean receiverIndexExists, long transactionId)
        throws ConverterException {

        byte[] transactionIdBytes = Converter.toByteArray(transactionId);
        BitSet transactionIdBitSet = Converter.toBitSet(transactionIdBytes);
        for (int i = 63; i > 45; i--) {
            transactionIdBitSet.set(i, false);
        }
        transactionIdBitSet.set(47, senderIndexExists);
        transactionIdBitSet.set(46, receiverIndexExists);
        return Converter.toLong(transactionIdBitSet);
    }

    public CalcTransactionIdSourceTargetResult calcTransactionIdSource(NodeApiService nodeApiService, String wideSource,
        String wideTarget) throws NodeClientException, ConverterException {
        return calcTransactionIdSourceTarget(nodeApiService,wideSource,wideTarget);
    }

    public static CalcTransactionIdSourceTargetResult calcTransactionIdSourceTarget(NodeApiService nodeApiService, String wideSource,
        String wideTarget) throws NodeClientException, ConverterException {

        // get transactions count from Node and increment it
        long transactionId = nodeApiService.getWalletTransactionsCount(wideSource) + 1;
        LOGGER.debug("<---  Transaction ID from node = {}", transactionId);

        // get last transaction id from cache
        AtomicLong lastTransactionId = walletLastTransactionIdCache.get(wideSource);

        if (lastTransactionId == null || transactionId > lastTransactionId.get()) {
            walletLastTransactionIdCache.put(wideSource, new AtomicLong(transactionId));
        } else {
            transactionId = lastTransactionId.incrementAndGet();
        }

        LOGGER.info("Result transaction ID = {}", transactionId);

        boolean sourceIndexExists = false;
        String shortSource = wideSource;
        boolean targetIndexExists = false;
        String shortTarget = wideTarget;

        Integer sourceWalletId = nodeApiService.getWalletId(wideSource);
        if (sourceWalletId != 0) {
            sourceIndexExists = true;
            shortSource = Converter.encodeToBASE58(Converter.toByteArrayLittleEndian(sourceWalletId, 4));
        }
        Integer targetWalletId = nodeApiService.getWalletId(wideTarget);
        if (targetWalletId != 0) {
            targetIndexExists = true;
            shortTarget = Converter.encodeToBASE58(Converter.toByteArrayLittleEndian(targetWalletId, 4));
        }

        return new CalcTransactionIdSourceTargetResult(
            createTransactionId(sourceIndexExists, targetIndexExists, transactionId), wideSource, wideTarget,
            shortSource, shortTarget);
    }


    public static class CalcTransactionIdSourceTargetResult {
        private long transactionId;
        private String source;
        private String target;
        private String wideSource;

        public String getWideSource() {
            return wideSource;
        }

        public void setWideSource(String wideSource) {
            this.wideSource = wideSource;
        }

        public String getWideTarget() {
            return wideTarget;
        }

        public void setWideTarget(String wideTarget) {
            this.wideTarget = wideTarget;
        }

        private String wideTarget;

        public CalcTransactionIdSourceTargetResult(long transactionId, String wideSource, String wideTarget,
            String source, String target) {
            this.transactionId = transactionId;
            this.wideSource = wideSource;
            this.wideTarget = wideTarget;
            this.source = source;
            this.target = target;
        }

        public long getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(long transactionId) {
            this.transactionId = transactionId;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getTarget() {
            return target;
        }

        public byte[] getByteSource() throws ConverterException {
            return Converter.decodeFromBASE58(source);
        }

        public byte[] getByteTarget() throws ConverterException {
            return Converter.decodeFromBASE58(target);
        }
    }
}