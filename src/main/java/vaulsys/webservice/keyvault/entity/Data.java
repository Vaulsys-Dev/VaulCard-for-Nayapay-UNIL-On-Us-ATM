package vaulsys.webservice.keyvault.entity;

/**
 * Created by Mati on 03/09/2019.
 */
public class Data {
    private SubData data;

    private String plaintext;

    private String token;

    public SubData getData() {
        return data;
    }

    public void setData(SubData data) {
        this.data = data;
    }

    public String getPlaintext() {
        return plaintext;
    }

    public void setPlaintext(String plaintext) {
        this.plaintext = plaintext;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
