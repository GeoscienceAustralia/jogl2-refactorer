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
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit test class used to test the Refactorer.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class TestRefactorer
{
	public void performTest(String className) throws IOException
	{
		File sourceDirectory = new File("data/test/input");
		File expectedDirectory = new File("data/test/expected");
		String filename = className.replace(".", "/") + ".java";

		File sourceFile = new File(sourceDirectory, filename);
		File expectedFile = new File(expectedDirectory, filename);
		String source = FileUtils.readFileToString(sourceFile);
		String expected = FileUtils.readFileToString(expectedFile);

		String[] classpath = new String[] { "data/lib/jogl.jar", "data/lib/gdal.jar", "data/lib/worldwind.jar" };
		String[] sources = new String[] {};

		for (int i = 0; i < classpath.length; i++)
		{
			classpath[i] = new File(classpath[i]).getAbsolutePath();
		}

		Refactorer refactorer = new Refactorer(classpath, sources);
		String output = refactorer.refactor(source, filename);

		Assert.assertEquals(expected, output);
	}

	@Test
	public void testRefactorer() throws IOException
	{
		//performTest("gov.nasa.worldwind.render.DrawContextImpl");
		//performTest("gov.nasa.worldwind.awt.AWTInputHandler");

		//performTest("gov.nasa.worldwind.layers.TextureTile");
		//performTest("gov.nasa.worldwind.render.SurfaceObjectTileBuilder");
		//performTest("performance.VBORenderer.GLDisplay");

		//performTest("gov.nasa.worldwind.util.TextureAtlas");

		File sourceDirectory = new File("data/test/expected");
		List<File> files = Util.getJavaFiles(sourceDirectory);

		for (File file : files)
		{
			String className = file.getAbsolutePath().substring(sourceDirectory.getAbsolutePath().length() + 1);
			className = className.substring(0, className.length() - ".java".length());
			className = className.replace("\\", ".");
			className = className.replace("/", ".");
			performTest(className);
		}
	}
}
