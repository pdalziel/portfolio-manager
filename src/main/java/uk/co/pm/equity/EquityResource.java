package uk.co.pm.equity;

import org.glassfish.jersey.server.mvc.Viewable;
import uk.co.pm.PortfolioManagerApplication;
import uk.co.pm.internal.ApplicationBase;
import uk.co.pm.internal.database.PortfolioManagerDB;
import uk.co.pm.internal.view.ViewModelBuilder;

import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Path("equities")
public class EquityResource {

    DataSource  dataSource;
    List<EquityReference> equityRefs;

    public EquityResource(DataSource ds) {
        this.dataSource=ds;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<EquityReference> retrieveEquities() {
        getDataFromDB();

        return this.equityRefs;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Viewable equities() {
        getDataFromDB();

        Map<String, Object> equities = ViewModelBuilder.init().add("equities", this.equityRefs).build();

        return new Viewable("/equities.ftl", equities);
    }

    private void getDataFromDB(){
        try {
            Connection connection = this.dataSource.getConnection();
            String getEpicSQL ="SELECT epic FROM equity";
            PreparedStatement epics = connection.prepareCall(getEpicSQL);
            ResultSet rs = epics.executeQuery();

            this.equityRefs=new ArrayList<>();
            while (rs.next()) {
                EquityReference eq = new EquityReference();
                eq.setEpic(rs.getString("epic"));
                eq.setDetailLink();

                this.equityRefs.add(eq);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }



    }
}
