package org.onebusaway.nyc.admin.service;

import java.io.InputStream;
import java.util.List;

public interface FileService {
  void setup();
  void setUser(String user);
  void setPassword(String password);
  void setBucketName(String bucketName);
  void setGtfsPath(String gtfsPath);
  String getGtfsPath();
  void setAuxPath(String auxPath);
  String getAuxPath();
  void setBuildPath(String buildPath);
  String getConfigPath();
  void setConfigPath(String configPath);
  String getBuildPath();
  String getBucketName();
  String getBundleDirTimestamp(String dir);
  
  boolean bundleDirectoryExists(String filename);

  boolean createBundleDirectory(String filename);

  List<String[]> listBundleDirectories(int maxResults);

  String get(String s3path, String tmpDir);
  InputStream get(String s3Path);
  String put(String key, String directory);
  
  List<String> list(String directory, int maxResults);
  
  /**
   * Creates a zip of all the output files generated in the given bundle directory during bundle building process
   * @param directoryName bundle outpur directory name
   * @return name of the zip file created
   */
  String createOutputFilesZip(String directoryName);
  
  /**
   * Validates that given file name does not contain characters which could lead to directory 
   * traversal attack.
   * @param fileName the given file name
   */
  void validateFileName(String fileName);
  

}
