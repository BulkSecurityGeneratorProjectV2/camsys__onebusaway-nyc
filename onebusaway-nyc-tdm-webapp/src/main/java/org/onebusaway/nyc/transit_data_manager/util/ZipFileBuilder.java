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

package org.onebusaway.nyc.transit_data_manager.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This class handles the details of using the java zip file libraries
 * Basically it should be used as follows:
 * 1) Instantiate
 * 2) Add a file by calling addFile with the file name
 * 3) Write the file to the OutputStream returned by addFile
 * 4) Repeat 2-3 as neccessary
 * 5) of course, close!
 * @author scott
 *
 */
public class ZipFileBuilder implements Closeable {

  private ZipOutputStream zipOutput = null;
  
  /**
   * Create a new zip file object at zipFile.
   * @param zipFile The File object representing the zip file to be created.
   * @throws FileNotFoundException
   */
  public ZipFileBuilder(File zipFile) throws FileNotFoundException {
    zipOutput = new ZipOutputStream(new FileOutputStream(zipFile));
  }
  
  /**
   * Add a file to the zip file. Just call addFile with the name and 
   * write to the returned OutputStream
   * @param name The name of the file to add to the zip file
   * @return an output stream the file being added should be written to.
   * @throws IOException
   */
  public OutputStream addFile(String name) throws IOException {
    ZipEntry zipEntry = new ZipEntry(name);
    zipOutput.putNextEntry(zipEntry);
    
    return zipOutput;
  }

  @Override
  public void close() throws IOException {
    if (zipOutput != null)
      zipOutput.close();    
  }
}
