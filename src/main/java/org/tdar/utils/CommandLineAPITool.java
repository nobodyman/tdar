/**
 * $Id$
 *
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.configuration.TdarConfiguration;

/**
 * @author Adam Brin
 * 
 */
public class CommandLineAPITool {

    private static final String OPTION_FILE = "file";
    private static final String OPTION_CONFIG = "config";
    private static final String OPTION_PASSWORD = "password";
    private static final String OPTION_USERNAME = "username";
    private static final String OPTION_HOST = "host";
    private static final String OPTION_LOG_FILE = "logFile";
    private static final String OPTION_ACCOUNTID = "accountid";
    private static final String OPTION_SLEEP = "sleep";
    private static final String OPTION_PROJECT_ID = "projectid";
    private static final String OPTION_ACCESS_RESTRICTION = "accessRestrictions";
    private static final String ALPHA_TDAR_ORG = "alpha.tdar.org";
    private static final String CORE_TDAR_ORG = "core.tdar.org";

    Logger logger = Logger.getLogger(getClass());
    DefaultHttpClient httpclient = new DefaultHttpClient();
    private String hostname = ALPHA_TDAR_ORG; // DEFAULT SHOULD NOT BE CORE
    private String username = "";
    private String password = "";
    private File logFile = new File("import.log");
    private Long projectId;
    private Long accountId;
    private Long msSleepBetween;
    private FileAccessRestriction fileAccessRestriction = FileAccessRestriction.PUBLIC;
    private List<String> seen = new ArrayList<String>();

    /**
     * return codes
     */

    // TODO: get rid of magic numbers

    /**
     * @param args
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args) {
        CommandLineAPITool importer = new CommandLineAPITool();

        String siteAcronym = TdarConfiguration.getInstance().getSiteAcronym();
        Options options = new Options();
        options.addOption(OptionBuilder.withArgName(OPTION_USERNAME).hasArg().withDescription(siteAcronym + " username")
                .create(OPTION_USERNAME));
        options.addOption(OptionBuilder.withArgName(OPTION_PASSWORD).hasArg().withDescription(siteAcronym + " password")
                .create(OPTION_PASSWORD));
        options.addOption(OptionBuilder.withArgName(OPTION_HOST).hasArg().withDescription("override hostname (default alpha.tdar.org)")
                .create(OPTION_HOST));
        options.addOption(OptionBuilder.withArgName(OPTION_FILE).hasArg().withDescription("the file(s) or directories to process")
                .create(OPTION_FILE));
        options.addOption(OptionBuilder.withArgName(OPTION_CONFIG).hasArg().withDescription("optional configuration file")
                .create(OPTION_CONFIG));
        options.addOption(OptionBuilder.withArgName(OPTION_PROJECT_ID).hasArg().withDescription(siteAcronym + "Project ID to associate w/ resource")
                .create(OPTION_PROJECT_ID));
        options.addOption(OptionBuilder.withArgName(OPTION_ACCOUNTID).hasArg().withDescription(siteAcronym + "tDAR Account Id")
                .create(OPTION_ACCOUNTID));
        options.addOption(OptionBuilder.withArgName(OPTION_SLEEP).hasArg().withDescription(siteAcronym + "timeToSleep")
                .create(OPTION_SLEEP));
        options.addOption(OptionBuilder.withArgName(OPTION_LOG_FILE).hasArg().withDescription(siteAcronym + "logFile")
                .create(OPTION_LOG_FILE));
        options.addOption(OptionBuilder.withArgName(OPTION_ACCESS_RESTRICTION).hasArg()
                .withDescription("file access restrictions (" + StringUtils.join(FileAccessRestriction.values()) + ")")
                .create(OPTION_ACCESS_RESTRICTION));
        CommandLineParser parser = new GnuParser();

        // TODO: lies! all lies!!!
        System.out.println("visit http://dev.tdar.org/confluence/");
        System.out.println("for documentation on how to use the " + siteAcronym);
        System.out.println("commandline API Tool");
        System.out.println("-------------------------------------------");
        String[] filenames = new String[0];
        try {
            // parse the command line arguments
            System.err.println("args are: " + Arrays.asList(args));
            CommandLine line = parser.parse(options, args);
            System.err.println("command line is: " + line);

            // validate that block-size has been set
            if (line.hasOption(OPTION_USERNAME)) {
                importer.setUsername(line.getOptionValue(OPTION_USERNAME));
            }
            if (line.hasOption(OPTION_HOST)) {
                System.err.println("Setting host to " + line.getOptionValue(OPTION_HOST));
                importer.setHostname(line.getOptionValue(OPTION_HOST));
            }
            if (line.hasOption(OPTION_PASSWORD)) {
                importer.setPassword(line.getOptionValue(OPTION_PASSWORD));
            }
            if (line.hasOption(OPTION_FILE)) {
                filenames = line.getOptionValues(OPTION_FILE);
            }

            if (line.hasOption(OPTION_PROJECT_ID)) {
                importer.setProjectId(new Long(line.getOptionValue(OPTION_PROJECT_ID)));
            }

            if (line.hasOption(OPTION_ACCOUNTID)) {
                importer.setAccountId(new Long(line.getOptionValue(OPTION_ACCOUNTID)));
            }

            if (line.hasOption(OPTION_SLEEP)) {
                importer.setMsSleepBetween(new Long(line.getOptionValue(OPTION_SLEEP)));
            }

            if (line.hasOption(OPTION_ACCESS_RESTRICTION)) {
                importer.setFileAccessRestriction(FileAccessRestriction.valueOf(line.getOptionValue(OPTION_ACCESS_RESTRICTION)));
            }

            if (line.hasOption(OPTION_LOG_FILE)) {
                importer.setLogFile(line.getOptionValue(OPTION_LOG_FILE));
                try {
                    importer.getSeen().addAll(FileUtils.readLines(importer.getLogFile()));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if (line.hasOption(OPTION_CONFIG)) {
                Properties properties = new Properties();
                try {
                    properties.load(new FileInputStream(line.getOptionValue(OPTION_CONFIG)));
                    importer.setHostname(properties.getProperty(OPTION_HOST, importer.getHostname()));
                    importer.setUsername(properties.getProperty(OPTION_USERNAME, importer.getHostname()));
                    importer.setPassword(properties.getProperty(OPTION_PASSWORD, importer.getHostname()));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (StringUtils.isEmpty(importer.getHostname())) {
                    throw new ParseException("no hostname specified");
                }
                if (StringUtils.isEmpty(importer.getUsername())) {
                    throw new ParseException("no username specified");
                }
                if (StringUtils.isEmpty(importer.getPassword())) {
                    throw new ParseException("no password specified");
                }
            }

        } catch (ParseException exp) {
            exp.printStackTrace();
            System.err.println("ParseException:" + exp);
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(siteAcronym + " cli api tool", options);
            System.exit(1);
        }

        File[] paths = new File[filenames.length];
        for (int i = 0; i < filenames.length; i++) {
            paths[i] = new File(filenames[i]);
            if (!paths[i].exists()) {
                System.err.println("Specified file does not exist: " + paths[i]);
                System.exit(1);
            }
        }

        if (paths.length == 0) {
            System.err.println("nothing to do, no files or directories specified...");
            System.exit(1);
        }

        int errorCount = importer.test(paths);
        if (errorCount > 0) {
            System.err.println("Exiting with errors");
            System.exit(errorCount);
        }

    }

    /**
     * 
     */
    private int test(File... files) {

        int errorCount = 0;
        try {

            if (getHostname().equalsIgnoreCase(ALPHA_TDAR_ORG)) {
                AuthScope scope = new AuthScope(getHostname(), 80);
                UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials("tdar", "alpha");
                httpclient.getCredentialsProvider().setCredentials(scope,
                        usernamePasswordCredentials);

                logger.info("creating challenge/response authentication request for alpha");
                HttpGet tdarIPAuth = new HttpGet("https://" + getHostname() + "/");
                logger.debug(tdarIPAuth.getRequestLine());
                HttpResponse response = httpclient.execute(tdarIPAuth);
                HttpEntity entity = response.getEntity();
                entity.consumeContent();
            }

            // make tdar authentication call
            HttpPost tdarAuth = new HttpPost("https://" + getHostname() + "/login/process");
            List<NameValuePair> postNameValuePairs = new ArrayList<NameValuePair>();
            postNameValuePairs.add(new BasicNameValuePair("loginUsername", getUsername()));
            postNameValuePairs.add(new BasicNameValuePair("loginPassword", getPassword()));

            tdarAuth.setEntity(new UrlEncodedFormEntity(postNameValuePairs, HTTP.UTF_8));
            HttpResponse response = httpclient.execute(tdarAuth);
            HttpEntity entity = response.getEntity();

            logger.trace("Login form get: " + response.getStatusLine());
            logger.trace("Post logon cookies:");
            List<Cookie> cookies = httpclient.getCookieStore().getCookies();
            boolean sawCrowdAuth = false;
            if (cookies.isEmpty()) {
                logger.trace("None");
            } else {
                for (int i = 0; i < cookies.size(); i++) {
                    if (cookies.get(i).getName().equals("crowd.token_key"))
                        sawCrowdAuth = true;
                    logger.trace("- " + cookies.get(i).toString());
                }
            }

            if (!sawCrowdAuth) {
                logger.warn("unable to authenticate, check username and password " + getHostname());
                // System.exit(0);
            }
            logger.trace(EntityUtils.toString(entity));
            entity.consumeContent();

            for (File file : files) {
                errorCount += processDirectory(file);
            }

        } catch (Exception e) {
            e.printStackTrace();
            errorCount++;
        }
        return errorCount;
    }

    /**
     * @param file
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    private int processDirectory(File parentDir) throws UnsupportedEncodingException, IOException {
        List<File> directories = new ArrayList<File>();
        List<File> attachments = new ArrayList<File>();
        List<File> records = new ArrayList<File>();

        int errorCount = 0;
        for (File file : parentDir.listFiles()) {
            if (file.isHidden())
                continue;
            String fileName = file.getName();
            if (file.isDirectory()) {
                directories.add(file);
            } else if (FilenameUtils.getExtension(fileName).equalsIgnoreCase("xml")) {
                records.add(file);
            } else {
                attachments.add(file);
            }
        }

        // if there is more than one record in a directory after scanning of the directory is
        // complete, then ignore all files that are not xml records
        if (records.size() > 1) {
            logger.debug("processing multiple xml files ...  (ignoring attachments) " + records);
            for (File record : records) {
                if (!makeAPICall(record, null)) {
                    errorCount++;
                }
            }
        }
        if (records.size() == 1) {
            logger.debug("processing : " + records);
            if (!makeAPICall(records.get(0), attachments)) {
                errorCount++;
            }
        }

        for (File directory : directories) {
            processDirectory(directory);
        }
        return errorCount;
    }

    public boolean makeAPICall(File record, List<File> attachments) throws UnsupportedEncodingException, IOException {
        String path = record.getPath();
        HttpPost apicall = new HttpPost("https://" + getHostname() + "/api/upload?uploadedItem=" + URLEncoder.encode(path));
        MultipartEntity reqEntity = new MultipartEntity();
        boolean callSuccessful = true;
        if (seen.contains(path)) {
            logger.debug("skipping: " + path);
        }
        reqEntity.addPart("record", new StringBody(FileUtils.readFileToString(record)));

        if (projectId != null) {
            logger.trace("setting projectId:" + projectId);
            reqEntity.addPart("projectId", new StringBody(projectId.toString()));
        }
        if (accountId != null) {
            logger.trace("setting accountId:" + accountId);
            reqEntity.addPart("accountId", new StringBody(accountId.toString()));
        }

        reqEntity.addPart("accessRestriction", new StringBody(getFileAccessRestriction().name()));

        if (!CollectionUtils.isEmpty(attachments)) {
            for (int i = 0; i < attachments.size(); i++) {
                reqEntity.addPart("uploadFile", new FileBody(attachments.get(i)));
                if (getFileAccessRestriction().isRestricted()) {
                    reqEntity.addPart("restrictedFiles", new StringBody(attachments.get(i).getName()));
                }
            }
        }

        apicall.setEntity(reqEntity);
        logger.debug("      files: " + StringUtils.join(attachments, ", "));

        HttpResponse response = httpclient.execute(apicall);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode >= HttpStatus.SC_BAD_REQUEST) {
            System.err.println("Server returned error: [" + record.getAbsolutePath() + "]:" + response.getStatusLine().getReasonPhrase());
            callSuccessful = false;
        }
        logger.info(record.toString() + " - " + response.getStatusLine());
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            String resp = EntityUtils.toString(entity);
            entity.consumeContent();
            if (resp != null && resp != "") {
                logger.debug(resp);
            }
        }

        FileUtils.writeStringToFile(getLogFile(), path + "\r\n", true);
        logger.info("done: " + path);
        try {
            Thread.sleep(getMsSleepBetween());
        } catch (Exception e) {

        }
        return callSuccessful;
    }

    /**
     * @param hostname
     *            the hostname to set
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getMsSleepBetween() {
        return msSleepBetween;
    }

    public void setMsSleepBetween(Long msSleepBetween) {
        this.msSleepBetween = msSleepBetween;
    }

    public File getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = new File(logFile);
    }

    public List<String> getSeen() {
        return seen;
    }

    public void setSeen(List<String> seen) {
        this.seen = seen;
    }

    public FileAccessRestriction getFileAccessRestriction() {
        return fileAccessRestriction;
    }

    public void setFileAccessRestriction(FileAccessRestriction fileAccessRestriction) {
        this.fileAccessRestriction = fileAccessRestriction;
    }
}
