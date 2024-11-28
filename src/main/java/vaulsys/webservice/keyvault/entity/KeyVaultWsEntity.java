package vaulsys.webservice.keyvault.entity;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by RAZA MURTAZA BAIG on 1/27/2018.
 */
@XmlRootElement
public class KeyVaultWsEntity {

    private String role_id;

    private String secret_id;

    private String client_token;

    private String token;

    private String ciphertext;

    private Auth auth;

    private Data data;

    public String getClient_token() {
        return client_token;
    }

    public void setClient_token(String client_token) {
        this.client_token = client_token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCiphertext() {
        return ciphertext;
    }

    public void setCiphertext(String ciphertext) {
        this.ciphertext = ciphertext;
    }


    public String getRole_id() {
        return role_id;
    }

    public void setRole_id(String role_id) {
        this.role_id = role_id;
    }

    public String getSecret_id() {
        return secret_id;
    }

    public void setSecret_id(String secret_id) {
        this.secret_id = secret_id;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
