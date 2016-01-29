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



        System.out.println("watchDirectory " + dataPath+equityFolder);

        watchDirectory(dataPath+equityFolder);
        System.out.println("watchDirectory " + dataPath+pricingFolder);
        watchDirectory(dataPath+pricingFolder);
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

    private void ParseFile(Path path) {

        BufferedReader br = null;
        PreparedStatement stm;

        // TODO validate file contents
        // TODO create error alerts/logging
        // TODO file-loading mechanism should be flexible to handle other file types

        if (path.toString().contains("details")) { // needs to be more robust
            stm = equityStm;
        } else {
            stm = priceStm;
        }
        try (CSVParser parser = new CSVParser(new FileReader(path.toFile()), CSVFormat.DEFAULT.withHeader())) {
            for (CSVRecord record : parser) {
                System.out.println(path);
                // check header to see if equity or pricing
                if (record.isMapped("Date")){
                    System.out.println("TRUE");
                    System.out.println(record.get("EPIC"));
                    System.out.println(record.get("Date"));
                }
                else {
                    System.out.println("FALSE");
                    System.out.println(record.get("EPIC"));
                    System.out.println(record.get("Company Name"));
                    System.out.println(record.get("Asset Type"));
                    System.out.println(record.get("Sector"));
                    System.out.println(record.get("Currency"));
                }
            }
            parser.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
/*

        try {

            File csvFile = path.toFile();

            String line;
            String cvsSplitBy = ",";

            br = new BufferedReader(new FileReader(csvFile));
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if(header) {
                    header = false;
                    continue;
                }

                // use comma as separator
                String[] values = line.split(cvsSplitBy);

                stm.setString(1, values[0]);
                stm.setString(3, values[2]);
                stm.setString(4, values[3]);
                if (priceStm == stm) {
                    String[] dateValues = values[1].split("-");
                    if (dateValues.length > 1) {
                        stm.setString(2, dateValues[0]);
                        stm.setString(5, dateValues[1]);
                    } else {
                        throw new IOException("Invalid date fields, no quarter info.");
                    }
                } else {
                    stm.setString(2, values[1]);
                    stm.setString(5, values[4]);
                }
                stm.executeUpdate();
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }*/
    }

}