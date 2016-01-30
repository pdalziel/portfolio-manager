package uk.co.pm;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.csv.CSVFormat;

import org.glassfish.jersey.server.ResourceConfig;
import uk.co.pm.equity.DetailsResource;
import uk.co.pm.equity.EquityResource;
import uk.co.pm.internal.ApplicationBase;
import uk.co.pm.internal.Configuration;
import uk.co.pm.internal.file.FileEventHandler;
import uk.co.pm.internal.file.FileWatcher;

import javax.sql.DataSource;
import javax.validation.constraints.Null;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

public class PortfolioManagerApplication extends ApplicationBase {

    PreparedStatement equityStm, priceStm;

    @Override
    protected void configure(ResourceConfig jerseyConfig, Configuration config, DataSource dataSource, FileWatcher fileWatcher) throws SQLException {
        EquityResource equities = new EquityResource(dataSource);
        DetailsResource details = new DetailsResource(dataSource);
        jerseyConfig.register(equities);

        Connection connection = dataSource.getConnection();

        //create statements on the connections
        String insertPriceSQL ="INSERT INTO price (epic,year,price,currency,quarter) VALUES (?,?,?,?,?)";
        priceStm = connection.prepareStatement(insertPriceSQL);

        String insertEquitySQL = "INSERT INTO equity (epic,company_name,asset_type,sector,currency) VALUES (?,?,?,?,?)";
        equityStm = connection.prepareStatement(insertEquitySQL);

        String dataPath = "trunk/portfolio-manager/data/";
        String equityFolder = "equity/";
        String pricingFolder = "pricing/";
        // Go over all already there files
        final Iterator iterator = FileUtils.iterateFiles(new File(dataPath), new IOFileFilter() {
            @Override
            public boolean accept(File file) {
                return !file.isDirectory();
            }

            @Override
            public boolean accept(File file, String s) {
                return !(new File(file.getAbsolutePath() + "/" + s)).isDirectory();
            }        }, null);
         while (iterator.hasNext()) {
            File file = (File) iterator.next();
            ParseFile(file.toPath());
        }
        watchDirectory(dataPath);

    }

    public static void main(String[] args) throws Exception {
        new PortfolioManagerApplication().init(args).run();
    }


    private void watchDirectory(final String path) {
        try {
            FileWatcher watcher = new FileWatcher();
            watcher.register(new File(path).toPath(), new FileEventHandler() {
                @Override
                public void handle(Path file) throws IOException {
                    Path fullPath = new File(path + "/" + file.toString()).toPath();
                    ParseFile(fullPath);
                }
            });
            watcher.start();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Crash in dir watcher");
        }
    }
   /* Refactored to use https://commons.apache.org/proper/commons-csv/
    * as it will allow more flexibility and protect against badly formatted files, e.g. infinite line
    * csv - Paul
   * */
    private void ParseFile(Path path) {
        PreparedStatement stm;
        // create parser and define expected file format
        try (CSVParser parser = new CSVParser(new FileReader(path.toFile()), CSVFormat.DEFAULT.withHeader())) {
            if(parser.getHeaderMap().containsKey("Date Time")){ // check if this is the pricing csv
                stm = priceStm;
                for (CSVRecord record : parser) {
                    stm.setString(1, record.get("EPIC"));
                    stm.setString(2, record.get("Date Time"));
                    stm.setString(3, record.get("Mid Price"));
                    stm.setString(4, record.get("Currency"));
                    stm.executeUpdate(); //TODO fix java.sql.SQLException: Parameter not set exception
                }

            }
            else if(parser.getHeaderMap().containsKey("Company Name")){  // check if this is the equity csv

                stm = equityStm;
                for (CSVRecord record : parser) {
                    stm.setString(1, record.get("EPIC"));
                    stm.setString(2, record.get("Company Name"));
                    stm.setString(3, record.get("Asset Type"));
                    stm.setString(4, record.get("Sector"));
                    stm.setString(5, record.get("Currency"));
                    stm.executeUpdate();
                }

            }
            else{ // could use reg exp to check headers
                System.out.println("bad format = " + path);
                System.out.println(parser.getHeaderMap().toString());
                throw new BadFileFormatException();// empty but may be needed in future sprints..
            }
            parser.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (BadFileFormatException e) {
            e.printStackTrace();
        }

    }

}