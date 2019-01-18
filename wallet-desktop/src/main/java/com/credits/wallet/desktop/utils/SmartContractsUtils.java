package com.credits.wallet.desktop.utils;

import com.credits.client.node.util.ObjectKeeper;
import com.credits.general.crypto.Md5;
import com.credits.general.exception.CreditsException;
import com.credits.general.pojo.ByteCodeObjectData;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.credits.general.crypto.Blake2S.generateHash;
import static com.credits.general.util.GeneralConverter.byteArrayToHex;
import static com.credits.general.util.GeneralConverter.toByteArray;
import static com.credits.general.util.GeneralConverter.toByteArrayLittleEndian;
import static org.apache.commons.lang3.ArrayUtils.addAll;

public class SmartContractsUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(SmartContractsUtils.class);
    private static byte[] bytes;


    public static byte[] generateSmartContractAddress(byte[] deployerAddress, long transactionId, List<ByteCodeObjectData> byteCodeObjects) {

        bytes = toByteArray(transactionId);

        byte[] sliceId = Arrays.copyOfRange(toByteArray(transactionId), 2, 8);
        ArrayUtils.reverse(sliceId);

        byte[] seed = addAll(deployerAddress, toByteArrayLittleEndian(sliceId,sliceId.length));
        for (ByteCodeObjectData unit: byteCodeObjects) {
            seed = addAll(seed, unit.getByteCode());
        }
        seed = toByteArrayLittleEndian(seed, seed.length);
        LOGGER.info("Generate smart contract address:\n");

        for(Byte b: seed){
            System.out.print(((0xff) & b) + ", ");
        }
        byte[] bytes = generateHash(seed);
        return bytes;
    }

    public static void saveSmartInTokenList(ObjectKeeper<ConcurrentHashMap<String, String>> coinsKeeper, String coinName, BigDecimal balance, String smartContractAddress) {
        if(balance != null) {
            ConcurrentHashMap<String, String> coins = coinsKeeper.getKeptObject().orElseGet(ConcurrentHashMap::new);
            coinName = checkCoinNameExist(coinName, coins);
            coins.put(coinName, smartContractAddress);
            coinsKeeper.keepObject(coins);
            coinsKeeper.flush();
        }
    }

    private static String checkCoinNameExist(String coinName, ConcurrentHashMap<String, String> coins) {
        if(coins.containsKey(coinName)) {
            String nameWithBrace = coinName + "(";
            for (String existingName : coins.keySet()){
               if(existingName.contains(nameWithBrace)){
                   int number = parseNumberOfDuplicateName(nameWithBrace.length(), existingName);
                   if (number != 0) return coinName + "(" + ++number + ")";
               }
            }
            return coinName + "(1)";
        }
        return coinName;
    }

    private static int parseNumberOfDuplicateName(int identityPieceIndex, String coinName) {
        StringBuilder sb = new StringBuilder(coinName);
        sb.replace(0, identityPieceIndex, "");
        sb.deleteCharAt(sb.length() - 1);
        try {
            return Integer.parseInt(sb.toString());
        }catch (NumberFormatException ignored){
        }
        return 0;
    }


    public static String generateSmartContractHashState(byte[] byteCode) throws CreditsException {
        byte[] hashBytes = Md5.encrypt(byteCode);
        return byteArrayToHex(hashBytes);
    }
}
