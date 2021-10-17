package nsu.MobileNetOptimiztion.NetOptimizationServer.Controller;

public class CellInfo {
    private String connection_type;
    private String identity;
    private String sig_strength;

    public String getConnection_type() {
        return connection_type;
    }

    public void setConnection_type(String connection_type) {
        this.connection_type = connection_type;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }
    public String getSig_strength() {
        return sig_strength;
    }

    public void setSig_strength(String sig_strength) {
        this.sig_strength = sig_strength;
    }
}
