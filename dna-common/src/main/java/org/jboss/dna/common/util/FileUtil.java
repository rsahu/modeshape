/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.dna.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class FileUtil {

    /**
     * Delete the file or directory at the supplied path. This method works on a directory that is not empty, unlike the
     * {@link File#delete()} method.
     * 
     * @param path the path to the file or directory that is to be deleted
     * @return true if the file or directory at the supplied path existed and was successfully deleted, or false otherwise
     */
    public static boolean delete( String path ) {
        if (path == null || path.trim().length() == 0) return false;
        return delete(new File(path));
    }

    /**
     * Delete the file or directory given by the supplied reference. This method works on a directory that is not empty, unlike
     * the {@link File#delete()} method.
     * 
     * @param fileOrDirectory the reference to the Java File object that is to be deleted
     * @return true if the supplied file or directory existed and was successfully deleted, or false otherwise
     */
    public static boolean delete( File fileOrDirectory ) {
        if (fileOrDirectory == null) return false;
        if (!fileOrDirectory.exists()) return false;

        // The file/directory exists, so if a directory delete all of the contents ...
        if (fileOrDirectory.isDirectory()) {
            for (File childFile : fileOrDirectory.listFiles()) {
                delete(childFile); // recursive call (good enough for now until we need something better)
            }
            // Now an empty directory ...
        }
        // Whether this is a file or empty directory, just delete it ...
        return fileOrDirectory.delete();
    }

    /**
     * Copy the source file system structure into the supplied target location. If the source is a file, the destiniation will be
     * created as a file; if the source is a directory, the destination will be created as a directory.
     * 
     * @param sourceFileOrDirectory the file or directory whose contents are to be copied into the target location
     * @param destinationFileOrDirectory the location where the copy is to be placed; does not need to exist, but if it does its
     *        type must match that of <code>src</code>
     * @return the number of files (not directories) that were copied
     * @throws IllegalArgumentException if the <code>src</code> or <code>dest</code> references are null
     * @throws IOException
     */
    public static int copy( File sourceFileOrDirectory,
                            File destinationFileOrDirectory ) throws IOException {
        int numberOfFilesCopied = 0;
        if (sourceFileOrDirectory.isDirectory()) {
            destinationFileOrDirectory.mkdirs();
            String list[] = sourceFileOrDirectory.list();

            for (int i = 0; i < list.length; i++) {
                String dest1 = destinationFileOrDirectory.getPath() + File.separator + list[i];
                String src1 = sourceFileOrDirectory.getPath() + File.separator + list[i];
                numberOfFilesCopied += copy(new File(src1), new File(dest1));
            }
        } else {
            FileInputStream fin = new FileInputStream(sourceFileOrDirectory);
            try {
                FileOutputStream fout = new FileOutputStream(destinationFileOrDirectory);
                try {
                    int c;
                    while ((c = fin.read()) >= 0) {
                        fout.write(c);
                    }
                } finally {
                    fout.close();
                }
            } finally {
                fin.close();
            }
            numberOfFilesCopied++;
        }
        return numberOfFilesCopied;
    }

    /**
     * Utility to convert {@link File} to {@link URL}.
     * 
     * @param filePath the path of the file
     * @return the {@link URL} representation of the file.
     * @throws MalformedURLException
     * @throws IllegalArgumentException if the file path is null, empty or blank
     */
    public static URL convertFileToURL( String filePath ) throws MalformedURLException {
        CheckArg.isNotEmpty(filePath, "filePath");
        File file = new File(filePath.trim());
        return file.toURI().toURL();
    }

}
