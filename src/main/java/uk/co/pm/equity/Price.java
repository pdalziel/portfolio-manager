package uk.co.pm.equity;

/**
 * Created by 0900772r on 22/01/16.
 */
public class Price {
    String price;

    public Price(String year,String quarter, String price) {
        this.price=year+"-"+quarter+"price was"+price;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setPrice(String year,String quarter, String price) {
        this.price=year+"-"+quarter+"price was"+price;
    }
}
