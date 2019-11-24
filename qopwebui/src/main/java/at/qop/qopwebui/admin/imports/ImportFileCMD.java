/* 
 * Copyright (C) 2018 Norbert Frese
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
*/

package at.qop.qopwebui.admin.imports;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import at.qop.qoplib.Config;

public abstract class ImportFileCMD {
	
	public final Path path;
	public boolean importFlag=true;
	public String tableName;
	public int srid = 4326;
	public String encoding = "LATIN1";	
	public List<String> warnings = new ArrayList<>();
	public String error = null;
	
	public ImportFileCMD(Path path) {
		super();
		this.path = path;
		tableName = proposeTableName();
	}
	
	public abstract String getFilename();

	protected abstract String proposeTableName();

	public abstract  String cmd(Config cfgFile);

	public abstract void validate();
	
	public boolean isValid() {
		return error == null;
	}

	public boolean isReprojectionRequired() {
		return false;
	}

	public String reprojectCmd() {
		return null;
	}
}