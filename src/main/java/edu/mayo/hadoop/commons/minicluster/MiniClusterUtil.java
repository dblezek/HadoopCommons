package edu.mayo.hadoop.commons.minicluster;

import com.github.sakserv.minicluster.config.ConfigVars;
import com.github.sakserv.minicluster.impl.HbaseLocalCluster;
import com.github.sakserv.minicluster.impl.HdfsLocalCluster;
import com.github.sakserv.minicluster.impl.YarnLocalCluster;
import com.github.sakserv.minicluster.impl.ZookeeperLocalCluster;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by m102417 on 2/11/16.
 * <p>
 * Utility class for starting and stopping hadoop mini-clusters (these are
 * clusters that can run in the JVM on localhost)
 * <p>
 * This is used in integration tests on the hadoop stack
 * <p>
 * Note much of this is static because we don't want to be starting and stoping
 * multiple copies of these services because it is expensive!
 */
public class MiniClusterUtil {

    // variables to track what services are currently started in the jvm env.
    private static boolean zookeeperStarted = false;
    private static boolean yarnStarted = false;
    private static boolean hdfsStarted = false;
    private static boolean hbaseStarted = false;

    private static ZookeeperLocalCluster zookeeperLocalCluster;
    private static HdfsLocalCluster hdfsLocalCluster;
    private static HbaseLocalCluster hbaseLocalCluster;

    /**
     * starts all the services we need.
     *
     * @throws Exception
     */
    public static void startAll(Properties prop) throws Exception {
        startZookeeper(prop);
        startYarn(prop);
        startHBASE(prop);
    }

    // public HdfsLocalCluster startHDFS() throws Exception {
    // HdfsLocalCluster hdfsLocalCluster = new HdfsLocalCluster.Builder()
    // .setHdfsNamenodePort(12345)
    // .setHdfsNamenodeHttpPort(12341)
    // .setHdfsTempDir("embedded_hdfs")
    // .setHdfsNumDatanodes(1)
    // .setHdfsEnablePermissions(false)
    // .setHdfsFormat(true)
    // .setHdfsEnableRunningUserAsProxyUser(true)
    // .setHdfsConfig(new Configuration())
    // .build();
    //
    // hdfsLocalCluster.start();
    // return hdfsLocalCluster;
    // }

    public static synchronized ZookeeperLocalCluster startZookeeper(Properties props) throws Exception {
        if (!zookeeperStarted) {
            ZookeeperLocalCluster.Builder builder = new ZookeeperLocalCluster.Builder();
            builder.setPort(Integer.parseInt(props.getProperty(ConfigVars.ZOOKEEPER_PORT_KEY)));
            builder.setTempDir(props.getProperty(ConfigVars.ZOOKEEPER_TEMP_DIR_KEY));
            builder.setZookeeperConnectionString(props.getProperty(ConfigVars.ZOOKEEPER_CONNECTION_STRING_KEY));
            zookeeperLocalCluster = builder.build();
            zookeeperLocalCluster.start();
            zookeeperStarted = true;
        }
        return zookeeperLocalCluster;
    }

    public static void setUp(Properties props) {
        hdfsLocalCluster = new HdfsLocalCluster.Builder().setHdfsNamenodePort(Integer.parseInt(props.getProperty(ConfigVars.HDFS_NAMENODE_PORT_KEY))).setHdfsTempDir(props.getProperty(ConfigVars.HDFS_TEMP_DIR_KEY))
                .setHdfsNumDatanodes(Integer.parseInt(props.getProperty(ConfigVars.HDFS_NUM_DATANODES_KEY))).setHdfsEnablePermissions(Boolean.parseBoolean(props.getProperty(ConfigVars.HDFS_ENABLE_PERMISSIONS_KEY)))
                .setHdfsFormat(Boolean.parseBoolean(props.getProperty(ConfigVars.HDFS_FORMAT_KEY))).setHdfsEnableRunningUserAsProxyUser(Boolean.parseBoolean(props.getProperty(ConfigVars.HDFS_ENABLE_RUNNING_USER_AS_PROXY_USER)))
                .setHdfsConfig(new Configuration()).build();
    }

    public static YarnLocalCluster startYarn(Properties props) {
        YarnLocalCluster yarnLocalCluster = new YarnLocalCluster.Builder()
                .setNumNodeManagers(Integer.parseInt(props.getProperty(ConfigVars.YARN_NUM_NODE_MANAGERS_KEY)))
                .setNumLocalDirs(Integer.parseInt(props.getProperty(ConfigVars.YARN_NUM_LOCAL_DIRS_KEY)))
                .setNumLogDirs(Integer.parseInt(props.getProperty(ConfigVars.YARN_NUM_LOG_DIRS_KEY)))
                .setResourceManagerAddress(props.getProperty(ConfigVars.YARN_RESOURCE_MANAGER_ADDRESS_KEY))
                .setResourceManagerHostname(props.getProperty(ConfigVars.YARN_RESOURCE_MANAGER_HOSTNAME_KEY))
                .setResourceManagerSchedulerAddress(props.getProperty(
                        ConfigVars.YARN_RESOURCE_MANAGER_SCHEDULER_ADDRESS_KEY))
                .setResourceManagerResourceTrackerAddress(props.getProperty(
                        ConfigVars.YARN_RESOURCE_MANAGER_RESOURCE_TRACKER_ADDRESS_KEY))
                .setResourceManagerWebappAddress(props.getProperty(
                        ConfigVars.YARN_RESOURCE_MANAGER_WEBAPP_ADDRESS_KEY))
                .setUseInJvmContainerExecutor(Boolean.parseBoolean(props.getProperty(
                        ConfigVars.YARN_USE_IN_JVM_CONTAINER_EXECUTOR_KEY)))
                .setConfig(new Configuration())
                .build();
        return yarnLocalCluster;
    }

    public synchronized static HbaseLocalCluster startHBASE(Properties props) throws Exception {
        startZookeeper(props);
        if (!hbaseStarted) {
            hbaseLocalCluster = new HbaseLocalCluster.Builder().setHbaseMasterPort(Integer.parseInt(props.getProperty(ConfigVars.HBASE_MASTER_PORT_KEY))).setHbaseMasterInfoPort(Integer.parseInt(props.getProperty(ConfigVars.HBASE_MASTER_INFO_PORT_KEY)))
                    .setNumRegionServers(Integer.parseInt(props.getProperty(ConfigVars.HBASE_NUM_REGION_SERVERS_KEY))).setHbaseRootDir(props.getProperty(ConfigVars.HBASE_ROOT_DIR_KEY))
                    .setZookeeperPort(Integer.parseInt(props.getProperty(ConfigVars.ZOOKEEPER_PORT_KEY))).setZookeeperConnectionString(props.getProperty(ConfigVars.ZOOKEEPER_CONNECTION_STRING_KEY))
                    .setZookeeperZnodeParent(props.getProperty(ConfigVars.HBASE_ZNODE_PARENT_KEY)).setHbaseWalReplicationEnabled(Boolean.parseBoolean(props.getProperty(ConfigVars.HBASE_WAL_REPLICATION_ENABLED_KEY)))
                    .setHbaseConfiguration(new Configuration()).build();
            hbaseLocalCluster.start();
            hbaseStarted = true;
        }
        return hbaseLocalCluster;
    }

    public static Properties loadPropertiesFile(String propFilePath) throws IOException {
        try (InputStream input = new FileInputStream(propFilePath)) {
            return loadPropertiesStream(input);
        }
    }

    public static Properties loadPropertiesStream(InputStream input) throws IOException {
        Properties prop = new Properties();
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

    public static synchronized void stopZookeeper() throws Exception {
        if (zookeeperStarted) {
            zookeeperLocalCluster.stop();
            zookeeperStarted = false;
        }
    }

    public static void stopHDFS() throws Exception {
        if (hdfsStarted) {
            hdfsLocalCluster.stop();
            hdfsStarted = false;
        }
    }

    public static void stopHBASE() throws Exception {
        if (hbaseStarted) {
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

    /**
     * gets the local directory where hbase is keeping it's files
     */
    public static String getHBaseDir(Properties props){
        String hbaseRoot = props.getProperty(ConfigVars.HBASE_ROOT_DIR_KEY);
        return hbaseRoot;
    }

    /**
     * used for deleting unstable/corrupted hbase files from a bad server shutdown
     * @param props
     * @throws IOException
     */
    public static void deleteLocalHBaseDir(Properties props) throws IOException {
        FileUtils.deleteDirectory(new File(getHBaseDir(props)));
    }

    /**
     * gets the local directory where zookeeper is keeping it's files
     */
    public static String getZookeeperDir(Properties props){
        String hbaseRoot = props.getProperty(ConfigVars.ZOOKEEPER_TEMP_DIR_KEY);
        return hbaseRoot;
    }

    /**
     * used for deleting unstable/corrupted hbase files from a bad server shutdown
     * @param props
     * @throws IOException
     */
    public static void deleteLocalZookeeperDir(Properties props) throws IOException {
        FileUtils.deleteDirectory(new File(getZookeeperDir(props)));
    }

}
