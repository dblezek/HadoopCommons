package edu.mayo.hadoop.commons.minicluster;

import com.github.sakserv.minicluster.config.ConfigVars;
import com.github.sakserv.minicluster.impl.HbaseLocalCluster;
import com.github.sakserv.minicluster.impl.HdfsLocalCluster;
import com.github.sakserv.minicluster.impl.ZookeeperLocalCluster;
import org.apache.hadoop.conf.Configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by m102417 on 2/11/16.
 *
 * Utility class for starting and stopping hadoop mini-clusters (these are clusters that can run in the JVM on localhost)
 *
 * This is used in integration tests on the hadoop stack
 *
 * Note much of this is static because we don't want to be starting and stoping multiple copies of these
 * services because it is expensive!
 */
public class MiniClusterUtil {

    //variables to track what services are currently started in the jvm env.
    private static boolean zookeeperStarted = false;
    private static boolean yarnStarted = false;
    private static boolean hdfsStarted = false;
    private static boolean hbaseStarted = false;

    private static ZookeeperLocalCluster zookeeperLocalCluster;
    private static HdfsLocalCluster hdfsLocalCluster;
    private static HbaseLocalCluster hbaseLocalCluster;


    /**
     * starts all the services we need.
     * @throws Exception
     */
    public static void startAll(Properties prop) throws Exception {
        startZookeeper(prop);
        startHBASE(prop);
    }


//    public HdfsLocalCluster startHDFS() throws Exception {
//        HdfsLocalCluster hdfsLocalCluster = new HdfsLocalCluster.Builder()
//                .setHdfsNamenodePort(12345)
//                .setHdfsNamenodeHttpPort(12341)
//                .setHdfsTempDir("embedded_hdfs")
//                .setHdfsNumDatanodes(1)
//                .setHdfsEnablePermissions(false)
//                .setHdfsFormat(true)
//                .setHdfsEnableRunningUserAsProxyUser(true)
//                .setHdfsConfig(new Configuration())
//                .build();
//
//        hdfsLocalCluster.start();
//        return hdfsLocalCluster;
//    }

    public static ZookeeperLocalCluster startZookeeper(Properties props) throws Exception {
        zookeeperLocalCluster = new ZookeeperLocalCluster.Builder()
                .setPort(Integer.parseInt(props.getProperty(ConfigVars.ZOOKEEPER_PORT_KEY)))
                .setTempDir(props.getProperty(ConfigVars.ZOOKEEPER_TEMP_DIR_KEY))
                .setZookeeperConnectionString(props.getProperty(ConfigVars.ZOOKEEPER_CONNECTION_STRING_KEY))
                .build();
        zookeeperLocalCluster.start();
        zookeeperStarted = true;
        return zookeeperLocalCluster;
    }

    public static void setUp(Properties props){
        hdfsLocalCluster = new HdfsLocalCluster.Builder()
                .setHdfsNamenodePort(Integer.parseInt(props.getProperty(ConfigVars.HDFS_NAMENODE_PORT_KEY)))
                .setHdfsTempDir(props.getProperty(ConfigVars.HDFS_TEMP_DIR_KEY))
                .setHdfsNumDatanodes(Integer.parseInt(props.getProperty(ConfigVars.HDFS_NUM_DATANODES_KEY)))
                .setHdfsEnablePermissions(
                        Boolean.parseBoolean(props.getProperty(ConfigVars.HDFS_ENABLE_PERMISSIONS_KEY)))
                .setHdfsFormat(Boolean.parseBoolean(props.getProperty(ConfigVars.HDFS_FORMAT_KEY)))
                .setHdfsEnableRunningUserAsProxyUser(Boolean.parseBoolean(
                        props.getProperty(ConfigVars.HDFS_ENABLE_RUNNING_USER_AS_PROXY_USER)))
                .setHdfsConfig(new Configuration())
                .build();
    }


    public static HbaseLocalCluster startHBASE(Properties props) throws Exception {
        if(!zookeeperStarted) { startZookeeper(props);}

        hbaseLocalCluster = new HbaseLocalCluster.Builder()
                .setHbaseMasterPort(
                        Integer.parseInt(props.getProperty(ConfigVars.HBASE_MASTER_PORT_KEY)))
                .setHbaseMasterInfoPort(
                        Integer.parseInt(props.getProperty(ConfigVars.HBASE_MASTER_INFO_PORT_KEY)))
                .setNumRegionServers(
                        Integer.parseInt(props.getProperty(ConfigVars.HBASE_NUM_REGION_SERVERS_KEY)))
                .setHbaseRootDir(props.getProperty(ConfigVars.HBASE_ROOT_DIR_KEY))
                .setZookeeperPort(Integer.parseInt(props.getProperty(ConfigVars.ZOOKEEPER_PORT_KEY)))
                .setZookeeperConnectionString(props.getProperty(ConfigVars.ZOOKEEPER_CONNECTION_STRING_KEY))
                .setZookeeperZnodeParent(props.getProperty(ConfigVars.HBASE_ZNODE_PARENT_KEY))
                .setHbaseWalReplicationEnabled(
                        Boolean.parseBoolean(props.getProperty(ConfigVars.HBASE_WAL_REPLICATION_ENABLED_KEY)))
                .setHbaseConfiguration(new Configuration())
                .build();
        hbaseLocalCluster.start();
        hbaseStarted = true;
        return hbaseLocalCluster;
    }

    public static Properties loadPropertiesFile(String propFilePath) throws IOException {
        Properties prop = new Properties();
        InputStream input = new FileInputStream(propFilePath);
        prop.load(input);
        return prop;
    }

    /**
     * stops all services that are running
     */
    public static void stopAll() throws Exception {
        stopHBASE();
        stopZookeeper();
    }

    public static void stopZookeeper()throws Exception{
        if(zookeeperStarted){
            zookeeperLocalCluster.stop();
            zookeeperStarted = false;
        }
    }

    public static void stopHDFS()throws Exception{
        if(hdfsStarted){
            hdfsLocalCluster.stop();
            hdfsStarted = false;
        }
    }

    public static void stopHBASE() throws Exception {
        if(hbaseStarted){
            hbaseLocalCluster.stop();
            hbaseStarted = false;
        }
    }


    public static ZookeeperLocalCluster getZookeeperLocalCluster() {
        return zookeeperLocalCluster;
    }

    public static HbaseLocalCluster getHbaseLocalCluster() {
        return hbaseLocalCluster;
    }
}