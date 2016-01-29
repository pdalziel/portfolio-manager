package uk.co.pm.equity;

public class EquityReference {

    public String epic;

    public String detailLink;

    public String getEpic() {
        return epic;
    }

    public void setEpic(String epic) {
        this.epic = epic;
    }

    public String getDetailLink() {
        return detailLink;
    }

    public void setDetailLink() {
        this.detailLink = "equities/"+this.epic;
    }
}
