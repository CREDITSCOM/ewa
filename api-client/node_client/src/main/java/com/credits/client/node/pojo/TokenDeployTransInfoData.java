package com.credits.client.node.pojo;

public class TokenDeployTransInfoData extends SmartTransInfoData {
    public String name;
    public String code;
    public TokenStandartData standart;

    public TokenDeployTransInfoData(String name, String code, TokenStandartData standart) {
        this.name = name;
        this.code = code;
        this.standart = standart;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public TokenStandartData getStandart() {
        return standart;
    }

    public void setStandart(TokenStandartData standart) {
        this.standart = standart;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TokenDeployTransInfoData)) return false;

        TokenDeployTransInfoData that = (TokenDeployTransInfoData) o;

        if (!getName().equals(that.getName())) return false;
        if (!getCode().equals(that.getCode())) return false;
        return getStandart() == that.getStandart();
    }

    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + getCode().hashCode();
        result = 31 * result + getStandart().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TokenDeployTransInfoData{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", standart=" + standart +
                '}';
    }
}
