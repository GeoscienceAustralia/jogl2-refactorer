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
import java.util.ArrayList;
import java.util.List;

/**
 * Class containing utility methods.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Util
{
	public static List<File> getJavaFiles(File dir)
	{
		List<File> list = new ArrayList<File>();
		addJavaFiles(dir, list);
		return list;
	}

	public static void addJavaFiles(File dir, List<File> list)
	{
		File[] files = dir.listFiles();
		for (File file : files)
		{
			if (file.isDirectory())
			{
				addJavaFiles(file, list);
			}
			else if (file.getName().toLowerCase().endsWith(".java"))
			{
				list.add(file);
			}
		}
	}
}
