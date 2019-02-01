package com.credits.wallet.desktop.utils;

import com.credits.client.node.pojo.TokenStandartData;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.Utils;
import com.credits.wallet.desktop.struct.DeploySmartListItem;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class DeployControllerUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(DeployControllerUtils.class);




    public static TokenStandartData getTokenStandard(Class<?> contractClass) {
        TokenStandartData tokenStandart = TokenStandartData.NotAToken;
        try {
            Class<?>[] interfaces = contractClass.getInterfaces();
            if (interfaces.length > 0) {
                Class<?> basicStandard = Class.forName("BasicStandard");
                Class<?> extendedStandard = Class.forName("ExtensionStandard");
                for (Class<?> _interface : interfaces) {
                    if (_interface.equals(basicStandard)) {
                        tokenStandart = TokenStandartData.CreditsBasic;
                    }
                    if (_interface.equals(extendedStandard)) {
                        tokenStandart = TokenStandartData.CreditsExtended;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            LOGGER.debug("can't find standard classes. Reason {}", e.getMessage());
        }
        return tokenStandart;
    }

    public static String getContractFromTemplate(String template) {
        try {
            return IOUtils.toString(
                DeployControllerUtils.class.getResourceAsStream("/template/" + template + ".template"), "UTF-8");
        } catch (IOException e) {
            return null;
        }
    }

    public static String checkContractNameExist(String smartName, ObservableList<DeploySmartListItem> list) {
        AtomicBoolean flag = new AtomicBoolean(false);
        list.forEach(el->{
            if(el.name.equals(smartName)) {
                flag.set(true);
            }
        });
        if(flag.get()) {
            String nameWithBrace = smartName + "(";
            int maxNumber = 0;
            for (DeploySmartListItem existingItem : list){
                String existingName = existingItem.name;
                if(existingName.contains(nameWithBrace)){
                    int number = SmartContractsUtils.parseNumberOfDuplicateName(nameWithBrace.length(), existingName);
                    if (number != 0 && number > maxNumber) maxNumber = number;
                }
            }
            if(maxNumber>0) {
                return smartName + "(" + ++maxNumber + ")";
            }
            return smartName + "(1)";
        }
        return smartName;
    }


    public short actualOfferedMaxFee16Bits;


    public void initializeFee(TextField feeField, Label actualOfferedMaxFeeLabel, Label feeErrorLabel) {
        feeField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                newValue = NumberUtils.getCorrectNum(newValue);
                if (!org.apache.commons.lang3.math.NumberUtils.isCreatable(newValue) && !newValue.isEmpty()) {
                    refreshOfferedMaxFeeValues(oldValue,feeField,actualOfferedMaxFeeLabel,feeErrorLabel);
                    return;
                }
                refreshOfferedMaxFeeValues(newValue,feeField,actualOfferedMaxFeeLabel,feeErrorLabel);
            } catch (Exception e) {
                //FormUtils.showError("Error. Reason: " + e.getMessage());
                refreshOfferedMaxFeeValues(oldValue,feeField,actualOfferedMaxFeeLabel,feeErrorLabel);
            }
        });
    }

    private void refreshOfferedMaxFeeValues(String oldValue,TextField feeField, Label actualOfferedMaxFeeLabel, Label feeErrorLabel) {
        if (oldValue.isEmpty()) {
            actualOfferedMaxFeeLabel.setText("");
            feeField.setText("");
        } else {
            Pair<Double, Short> actualOfferedMaxFeePair =
                Utils.createActualOfferedMaxFee(GeneralConverter.toDouble(oldValue));
            actualOfferedMaxFeeLabel.setText(GeneralConverter.toString(actualOfferedMaxFeePair.getLeft()));
            actualOfferedMaxFee16Bits = actualOfferedMaxFeePair.getRight();
            feeField.setText(oldValue);
        }
    }

    public static short getActualOfferedMaxFee16Bits() {
        return 0;
    }

}
