/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package refactorer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * JOGL2 refactorer main class.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Main
{
	public static void main(String[] args) throws IOException
	{
		if (args.length != 1)
		{
			System.out.println("Usage: refactorer <worldwind_directory>");
			System.out.println();
			System.out.println("   <worldwind_directory>: Directory containing 'jogl.jar', 'gdal.jar', and the");
			System.out.println("                          World Wind Java 'src' directory.");
			return;
		}

		File worldwindDirectory = new File(args[0]);
		File sourceDirectory = new File(worldwindDirectory, "src");
		File joglJar = new File(worldwindDirectory, "jogl.jar");
		File gdalJar = new File(worldwindDirectory, "gdal.jar");

		if (!sourceDirectory.isDirectory())
		{
			System.err.println("Directory not found: " + sourceDirectory);
			return;
		}
		if (!joglJar.isFile())
		{
			System.err.println("File not found: " + joglJar);
			return;
		}
		if (!gdalJar.isFile())
		{
			System.err.println("File not found: " + gdalJar);
			return;
		}

		String[] classpath = new String[] { joglJar.getAbsolutePath(), gdalJar.getAbsolutePath() };
		String[] sources = new String[] { sourceDirectory.getAbsolutePath() };
		Refactorer refactorer = new Refactorer(classpath, sources);

		List<File> newFiles = new ArrayList<File>();
		List<File> oldFiles = new ArrayList<File>();

		@SuppressWarnings("unchecked")
		Collection<File> files = FileUtils.listFiles(sourceDirectory, null, true);
		System.out.println("Found " + files.size() + " source files, refactoring...");
		int i = 0;
		for (File file : files)
		{
			String filename = file.getAbsolutePath().substring(sourceDirectory.getAbsolutePath().length() + 1);

			System.out.print("(" + (++i) + "/" + files.size() + ") - ");
			if (filename.toLowerCase().endsWith(".java"))
			{
				System.out.println("refactoring " + filename);
				String source = FileUtils.readFileToString(file);
				String output = refactorer.refactor(source, filename);
				File outputFile = new File(file.getAbsolutePath() + ".jogl2");
				FileUtils.writeStringToFile(outputFile, output);

				newFiles.add(outputFile);
				oldFiles.add(file);
			}
			else
			{
				System.out.println("skipping " + filename);
			}
		}

		System.out.print("Finalising... ");
		for (i = 0; i < newFiles.size(); i++)
		{
			FileUtils.deleteQuietly(oldFiles.get(i));
			FileUtils.moveFile(newFiles.get(i), oldFiles.get(i));
		}
		System.out.println("done");
	}
}
