/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onebusaway.nyc.util.impl;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * convenience class for file handling functions across OBA-NYC.
 * 
 */
public class FileUtility {

  private static Logger _log = LoggerFactory.getLogger(FileUtility.class);
  /**
   * Copy the input stream to the given destinationFileName (which includes path
   * and filename).
   */
  public void copy(InputStream source, String destinationFileName) {

    DataOutputStream destination = null;

    try {
      destination = new DataOutputStream(new FileOutputStream(
          destinationFileName));
      IOUtils.copy(source, destination);
    } catch (Exception any) {
      _log.error(any.toString());
      throw new RuntimeException(any);
    } finally {
      if (source != null)
        try {
          source.close();
        } catch (Exception any) {
        }
      if (destination != null)
        try {
          destination.close();
        } catch (Exception any) {
        }
    }
  }

  /**
   * Delete the file or directory represented by file. Throw an exception if
   * this is not possible.
   * 
   * @param file
   * @throws IOException
   */
  public void delete(File file) throws IOException {
    if (file.isDirectory()) {
      // directory is empty, then delete it
      if (file.list().length == 0) {
        file.delete();
      } else {
        // list all the directory contents
        String files[] = file.list();

        for (String temp : files) {
          // construct the file structure
          File fileDelete = new File(file, temp);
          // recursive delete
          delete(fileDelete);
        }

        // check the directory again, if empty then delete it
        if (file.list().length == 0) {
          file.delete();
        }
      }

    } else {
      // if file, then delete it
      file.delete();
    }
  }

  /**
   * Untar an input file into an output file.
   * 
   * The output file is created in the output folder, having the same name as
   * the input file, minus the '.tar' extension.
   * 
   * @param inputFile the input .tar file
   * @param outputDir the output directory file.
   * @throws IOException
   * @throws FileNotFoundException
   * 
   * @return The {@link List} of {@link File}s with the untared content.
   * @throws ArchiveException
   */
  public List<File> unTar(final File inputFile, final File outputDir)
      throws FileNotFoundException, IOException, ArchiveException {

    _log.info(String.format("Untaring %s to dir %s.",
        inputFile.getAbsolutePath(), outputDir.getAbsolutePath()));

    final List<File> untaredFiles = new LinkedList<File>();
    final InputStream is = new FileInputStream(inputFile);
    final TarArchiveInputStream debInputStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream(
        "tar", is);
    TarArchiveEntry entry = null;
    while ((entry = (TarArchiveEntry) debInputStream.getNextEntry()) != null) {
      final File outputFile = new File(outputDir, entry.getName());
      if (entry.isDirectory()) {
        _log.info(String.format("Attempting to write output directory %s.",
            outputFile.getAbsolutePath()));
        if (!outputFile.exists()) {
          _log.info(String.format("Attempting to create output directory %s.",
              outputFile.getAbsolutePath()));
          if (!outputFile.mkdirs()) {
            throw new IllegalStateException(String.format(
                "CHUNouldn't create directory %s.", outputFile.getAbsolutePath()));
          }
        }
      } else {
        _log.info(String.format("Creating output file %s.",
            outputFile.getAbsolutePath()));
        final OutputStream outputFileStream = new FileOutputStream(outputFile);
        IOUtils.copy(debInputStream, outputFileStream);
        outputFileStream.close();
      }
      untaredFiles.add(outputFile);
    }
    debInputStream.close();

    return untaredFiles;
  }

  /**
   * Ungzip an input file into an output file.
   * <p>
   * The output file is created in the output folder, having the same name as
   * the input file, minus the '.gz' extension.
   * 
   * @param inputFile the input .gz file
   * @param outputDir the output directory file.
   * @throws IOException
   * @throws FileNotFoundException
   * 
   * @return The {@File} with the ungzipped content.
   */
  public File unGzip(final File inputFile, final File outputDir)
      throws FileNotFoundException, IOException {

    _log.info(String.format("Ungzipping %s to dir %s.",
        inputFile.getAbsolutePath(), outputDir.getAbsolutePath()));

    final File outputFile = new File(outputDir, inputFile.getName().substring(
        0, inputFile.getName().length() - 3));

    final GZIPInputStream in = new GZIPInputStream(new FileInputStream(
        inputFile));
    final FileOutputStream out = new FileOutputStream(outputFile);
    
    IOUtils.copy(in, out);

    in.close();
    out.close();

    return outputFile;
  }


  /**
   * Zip up the files in basePath according to the globbing includeExpression.  Similar to
   * command line syntax except expecting java regex syntax (or a filename).
   * @param filename the created zip file including full path
   * @param basePath the directory to look for files in;
   * @param includeExpression the java regex to apply to the basePath.
   * @throws Exception should the zip fail, or should the includeExression not match any files
   */
  public void zipRecursively(String filename, String basePath, final String includeExpression) throws Exception {
    _log.info("creating zip file " + filename);
    FileOutputStream fos = new FileOutputStream(filename);
    ZipOutputStream zos = new ZipOutputStream(fos);
    File basePathDir = new File(basePath);
    String[] files = basePathDir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.matches(includeExpression);
      }
    });

    if (files == null) {
      zos.close();
      throw new FileNotFoundException("no files selected for basePath=" + basePath
              + " and includeExpression=" + includeExpression);
    }

    zipFolder(zos,files,basePath,includeExpression);
    zos.close();
  }

  /**
   * Zip up the files in basePath according to the globbing includeExpression.  Similar to
   * command line syntax except expecting java regex syntax (or a filename).
   * @param filename the created zip file including full path
   * @param basePath the directory to look for files in;
   * @param includeExpression the java regex to apply to the basePath.
   * @throws Exception should the zip fail, or should the includeExression not match any files
   */
  public void zip(String filename, String basePath, final String includeExpression) throws Exception {
    _log.info("creating zip file " + filename);
    FileOutputStream fos = new FileOutputStream(filename);
    ZipOutputStream zos = new ZipOutputStream(fos);
    File basePathDir = new File(basePath);
    String[] files = basePathDir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.matches(includeExpression);
      }
    });

    if (files == null) {
      zos.close();
      throw new FileNotFoundException("no files selected for basePath=" + basePath
              + " and includeExpression=" + includeExpression);
    }

    zipFolder(zos,files,basePath,includeExpression);
    zos.close();
  }

  /**
   * Zip up the files in basePath recursively.
   * @param filename the created zip file including full path
   * @param basePath the directory to look for files in;
   * @throws Exception should the zip fail, or should the includeExression not match any files
   */
  public void zipRecursively(String filename, String basePath) throws Exception {
    zipRecursively(filename,basePath,false);
  }

  /**
   * Zip up the files in basePath recursively.
   * @param filename the created zip file including full path
   * @param basePath the directory to look for files in;
   * @param includeBaseFolder whether or not to zip the basepath folder itself
   * @throws Exception should the zip fail, or should the includeExression not match any files
   */
  public void zipRecursively(String filename, String basePath, boolean includeBaseFolder) throws Exception {
    _log.info("creating zip file " + filename);
    FileOutputStream fos = new FileOutputStream(filename);
    ZipOutputStream zos = new ZipOutputStream(fos);
    File basePathDir = new File(basePath);
    String[] files = basePathDir.list();
    if (includeBaseFolder){
        files = new String[1];
        files[0] = basePathDir.getName();
        basePath = basePathDir.getParent();
    }

    if (files == null) {
      zos.close();
      throw new FileNotFoundException("no files selected for basePath=" + basePath);
    }

    zipFolderRecursively(zos,files,basePath);
    zos.close();
  }

  private void zipFolder(ZipOutputStream zos, String[] files, String basePath, final String includeExpression) throws IOException {
    for (String file : files) {
      String basePathSubFolder = basePath+"/"+file ;
      File fileObject = new File(basePathSubFolder);
      if (fileObject.isDirectory()){
        File basePathDir = new File(basePathSubFolder);
        String[] filesSubfolder = basePathDir.list(new FilenameFilter() {
          public boolean accept(File dir, String name) {
            return name.matches(includeExpression);
          }
        });
        zipFolder(zos,filesSubfolder,basePathSubFolder,includeExpression);
        return;
      }
      _log.info("compressing " + file);
      ZipEntry ze = new ZipEntry(file);
      zos.putNextEntry(ze);
      FileInputStream in = new FileInputStream(new File(basePath, file));
      IOUtils.copy(in, zos);
      in.close();
      zos.closeEntry();
    }
  }

  private void zipFolderRecursively(ZipOutputStream zos, String[] files, String basePath) throws IOException {
    zipFolderRecursively(zos,files,basePath, basePath, "");
  }

  private void zipFolderRecursively(ZipOutputStream zos, String[] files, String originalBasePath, String newBasePath, String subfolderPath) throws IOException {
    for (String file : files) {
      String basePathSubFolder = newBasePath+"/"+file ;
      File fileObject = new File(basePathSubFolder);
      String recursiveFilePath = subfolderPath == "" ? file : subfolderPath + "/" +file;
      if (fileObject.isDirectory()){
        File basePathDir = new File(basePathSubFolder);
        String[] filesSubfolder = basePathDir.list();
        zipFolderRecursively(zos,filesSubfolder,originalBasePath,basePathSubFolder, recursiveFilePath);
        continue;
      }
      _log.info("compressing " + recursiveFilePath);
      ZipEntry ze = new ZipEntry(recursiveFilePath);
      zos.putNextEntry(ze);
      FileInputStream in = new FileInputStream(new File(originalBasePath, recursiveFilePath));
      IOUtils.copy(in, zos);
      in.close();
      zos.closeEntry();
    }
  }

  /**
   * delete the files in basePath that match the given expression.
   * @param basePath the directory to examine; not recursive
   * @param includeExpression the java regular expression to consider
   * @return the number of files deleted.
   */
  public int deleteFilesInFolder(String basePath, final String includeExpression) {
    String[] files = new File(basePath).list(new FilenameFilter() {

      @Override
      public boolean accept(File dir, String name) {
        return name.matches(includeExpression);
      }

    });
    if (files == null) return 0;
    int count = 0;
    for (String file: files) {
      File toDelete = new File(basePath, file);
      toDelete.delete();
      count++;
    }
    return count;
  }

  public boolean printFromUrl(String url, String destination){
    return printFromUrl(url,null,destination);
  }

  public boolean printFromUrl(String url, String additionalText, String destination){
    try{
      URL inputAddress = new URL(url);
      InputStream in = inputAddress.openStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      File targetAdress = new File(destination);
      OutputStream out = new FileOutputStream(targetAdress);
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

      String line = null;

      while ((line = reader.readLine()) != null) {
        writer.write(line);
      }
      if(additionalText!=null){
        writer.newLine();
        writer.write(additionalText);
      }
      writer.close();
      return true;
    } catch (MalformedURLException e) {
      e.printStackTrace();

    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }
  
}
