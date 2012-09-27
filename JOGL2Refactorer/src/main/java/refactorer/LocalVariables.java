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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Helper class used to store names of variables (and their types) available in
 * the current source code scope.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LocalVariables
{
	private final List<List<LocalVariable>> variables = new ArrayList<List<LocalVariable>>();
	private final MultiMap<String, LocalVariable> typeToVariableMap = new MultiMap<String, LocalVariable>();

	public void push()
	{
		variables.add(new ArrayList<LocalVariable>());
	}

	public void pop()
	{
		List<LocalVariable> remove = variables.remove(variables.size() - 1);
		for (LocalVariable variable : remove)
		{
			variable.removeFromMap(typeToVariableMap);
		}
	}

	public void add(ITypeBinding type, String name)
	{
		LocalVariable variable = new LocalVariable(type, name);
		variables.get(variables.size() - 1).add(variable);
		variable.addToMap(typeToVariableMap);
	}

	public void addManual(String typeQualifiedName, String name)
	{
		LocalVariable variable = new LocalVariable(typeQualifiedName, name);
		variables.get(variables.size() - 1).add(variable);
		variable.addToMap(typeToVariableMap);
	}

	public List<LocalVariable> getLocalVariablesMatchingType(String typeQualifiedName)
	{
		return typeToVariableMap.get(typeQualifiedName);
	}

	public static class LocalVariable
	{
		public final ITypeBinding type;
		public final String typeQualifiedName;
		public final String name;

		public LocalVariable(ITypeBinding type, String name)
		{
			this.type = type;
			this.typeQualifiedName = type == null ? null : type.getQualifiedName();
			this.name = name;
		}

		public LocalVariable(String typeQualifiedName, String name)
		{
			this.type = null;
			this.typeQualifiedName = typeQualifiedName;
			this.name = name;
		}

		public void addToMap(MultiMap<String, LocalVariable> map)
		{
			if (type != null)
			{
				addToMap(map, type);
			}
			else if (typeQualifiedName != null)
			{
				map.putSingle(typeQualifiedName, this);
			}
		}

		public void removeFromMap(MultiMap<String, LocalVariable> map)
		{
			if (type != null)
			{
				removeFromMap(map, type);
			}
			else if (typeQualifiedName != null)
			{
				map.removeSingle(typeQualifiedName, this);
			}
		}

		private void addToMap(MultiMap<String, LocalVariable> map, ITypeBinding type)
		{
			if (type == null)
				return;

			map.putSingle(type.getQualifiedName(), this);
			addToMap(map, type.getSuperclass());
			for (ITypeBinding i : type.getInterfaces())
			{
				addToMap(map, i);
			}
		}

		private void removeFromMap(MultiMap<String, LocalVariable> map, ITypeBinding type)
		{
			if (type == null)
				return;

			map.removeSingle(type.getQualifiedName(), this);
			removeFromMap(map, type.getSuperclass());
			for (ITypeBinding i : type.getInterfaces())
			{
				removeFromMap(map, i);
			}
		}

		@Override
		public String toString()
		{
			return (type == null ? type : type.getQualifiedName()) + " " + name;
		}
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (List<LocalVariable> variables : this.variables)
		{
			sb.append("[");
			for (LocalVariable variable : variables)
			{
				sb.append(variable);
				sb.append(", ");
			}
			sb.append("], ");
		}
		return sb.toString();
	}
}
