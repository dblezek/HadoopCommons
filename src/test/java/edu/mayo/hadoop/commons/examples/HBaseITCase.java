package edu.mayo.hadoop.commons.examples;

import com.github.sakserv.minicluster.config.ConfigVars;
import edu.mayo.hadoop.commons.hbase.HBaseConnector;
import edu.mayo.hadoop.commons.hbase.HBaseUtil;
import edu.mayo.hadoop.commons.minicluster.MiniClusterUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class HBaseITCase {

    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(HBaseITCase.class);
    //properties file
    public static final String HBASE_PROPS = "target/test-classes/hbase.properties";
    private static Properties props;

    static {
        try {
            props = MiniClusterUtil.loadPropertiesFile(HBASE_PROPS);
        } catch(IOException e) {
            LOG.error("Unable to load property file: {}", HBASE_PROPS);
        }
    }



    @BeforeClass
    public static void setUp() throws Exception {
        MiniClusterUtil.startHBASE(props);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        MiniClusterUtil.stopAll();
    }

    @Test
    public void testHbaseLocalCluster() throws Exception {

        LOG.info("Establishing a connection with HBase");
        Configuration configuration = MiniClusterUtil.getHbaseLocalCluster().getHbaseConfiguration();
        HBaseConnector hcon = new HBaseConnector(configuration);
        HBaseUtil hutil = new HBaseUtil(hcon);

        LOG.info("Drop Tables in case things did not cleanup correctly in the past");
        hutil.dropAll();

        String tableName = props.getProperty(ConfigVars.HBASE_TEST_TABLE_NAME_KEY);
        String colFamName = props.getProperty(ConfigVars.HBASE_TEST_COL_FAMILY_NAME_KEY);
        String colQualiferName = props.getProperty(ConfigVars.HBASE_TEST_COL_QUALIFIER_NAME_KEY);
        Integer numRowsToPut = Integer.parseInt(props.getProperty(ConfigVars.HBASE_TEST_NUM_ROWS_TO_PUT_KEY));


        LOG.info("HBASE: Creating table {} with column family {}", tableName, colFamName);
        createHbaseTable(tableName, colFamName, configuration);

        LOG.info("HBASE: Populate the table with {} rows.", numRowsToPut);
        for (int i=0; i<numRowsToPut; i++) {
            putRow(tableName, colFamName, String.valueOf(i), colQualiferName, "row_" + i, configuration);
        }

        LOG.info("HBASE: Fetching and comparing the results");
        for (int i=0; i<numRowsToPut; i++) {
            Result result = getRow(tableName, colFamName, String.valueOf(i), colQualiferName, configuration);
            assertEquals("row_" + i, new String(result.value()));
        }

        LOG.info("Test complete, dropping schema!");
        hutil.dropAll();
        //shutdown client
        hcon.close();


    }

    private static void createHbaseTable(String tableName, String colFamily,
                                         Configuration configuration) throws Exception {

        final HBaseAdmin admin = new HBaseAdmin(configuration);
        HTableDescriptor hTableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
        HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(colFamily);

        hTableDescriptor.addFamily(hColumnDescriptor);
        admin.createTable(hTableDescriptor);
    }

    private static void putRow(String tableName, String colFamName, String rowKey, String colQualifier, String value,
                               Configuration configuration) throws Exception {
        HTable table = new HTable(configuration, tableName);
        Put put = new Put(Bytes.toBytes(rowKey));
        put.add(Bytes.toBytes(colFamName), Bytes.toBytes(colQualifier), Bytes.toBytes(value));
        table.put(put);
        table.flushCommits();
        table.close();
    }

    private static Result getRow(String tableName, String colFamName, String rowKey, String colQualifier,
                                 Configuration configuration) throws Exception {
        Result result;
        HTable table = new HTable(configuration, tableName);
        Get get = new Get(Bytes.toBytes(rowKey));
        get.addColumn(Bytes.toBytes(colFamName), Bytes.toBytes(colQualifier));
        get.setMaxVersions(1);
        result = table.get(get);
        return result;
    }

}