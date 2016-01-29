package uk.co.pm.equity;

import org.glassfish.jersey.server.mvc.Viewable;
import uk.co.pm.internal.view.ViewModelBuilder;

import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Path("equities/{epic}")
public class DetailsResource {

    DataSource  dataSource;
    List<EquityReference> detailRefs;
    public DetailsResource(DataSource ds) {
        this.dataSource=ds;

    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<EquityReference> retrieveEquities() {
        EquityReference eq1 = new EquityReference();
        eq1.setEpic("III");


        EquityReference eq2 = new EquityReference();
        eq2.setEpic("ADN");

        return Arrays.asList(eq1, eq2);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Viewable equities(@PathParam("epic") String epic) {

        EquityReference eq1 = new EquityReference();
        eq1.setEpic("III");eq1.setDetailLink();

        EquityReference eq2 = new EquityReference();
        eq2.setEpic("ADN");eq2.setDetailLink();

        List<EquityReference> equityRefs = Arrays.asList(eq1, eq2);

        Map<String, Object> equities = ViewModelBuilder.init().add("equities", equityRefs).build();
        return new Viewable("/details.ftl", equities);
    }

    private void getDataFromDB(String epic){
        try {
            Connection connection = this.dataSource.getConnection();
            String getCompanySQL ="SELECT company_name,asset_sector,currency FROM equity WHERE epic="+epic;
            String getPricesSQL = "SELECT year,price,quarter FROM price WHERE epic="+epic;

            PreparedStatement company = connection.prepareCall(getCompanySQL);
            ResultSet rs = company.executeQuery();

            this.detailRefs=new ArrayList<>();
            while (rs.next()) {
                EquityReference eq = new EquityReference();
                eq.setEpic(rs.getString("epic"));
                eq.setDetailLink();

                this.detailRefs.add(eq);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }



    }
}
