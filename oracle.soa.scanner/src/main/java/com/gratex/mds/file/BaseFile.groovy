package com.gratex.mds.file;

import groovy.io.FileType
import groovy.transform.EqualsAndHashCode

import java.nio.file.Path
import java.nio.file.Paths

import com.gratex.mds.AllMds
import com.gratex.mds.AllMds.MdsInfo;
import com.gratex.mds.catalog.CompiletimeCatalog
import com.gratex.mds.catalog.RuntimeCatalog
import com.gratex.mds.exception.CreationException
import com.gratex.mds.exception.ProjectFileException
import com.tinkerpop.blueprints.Vertex

@EqualsAndHashCode(excludes=["self", "prjCtlg", "svcCtlg" ])
public abstract class BaseFile implements Serializable {

	String projectName
	File projectFile
	Path filePath
	Path relPath
	URI fileURI
	Vertex self

	transient CompiletimeCatalog prjCtlg = CompiletimeCatalog.instance
	transient RuntimeCatalog svcCtlg = RuntimeCatalog.instance

	public BaseFile(String file) {
		this(new URI(file))
	}

	public BaseFile(URI fURI) {
		// look for project file
		def dir
		fileURI = fURI
		if(fileURI.scheme == "oramds") {
			def file = fileURI.toString()
			def info = AllMds.getInstance().resolveInfo(fileURI)
			if(!info)
				throw new CreationException("Project not found $fileURI")

			fileURI = info.actualURI
			filePath = info.actualPath
			projectName = info.mdsInfo.logicalName
			Path projectDirPath = info.mdsInfo.dir 
			relPath = projectDirPath.relativize(filePath)
			projectFile = projectDirPath.toFile()

		} else {
			filePath = Paths.get(fileURI)
			for(dir = filePath.getParent(); dir != null && projectFile == null ; dir = dir.getParent()) {
				dir.toFile().eachFileMatch(FileType.FILES, ~".*\\.jpr") { projectFile = it }
			}

			if(!projectFile) {
				throw new ProjectFileException("Unable to find project file for path: $filePath")
			}

			relPath = projectFile.toPath().relativize(filePath)

			def matcher = projectFile.name =~ /^(.*)\..*?$/
			projectName = matcher[0][1]
		}

		self = prjCtlg.addProjectVertex(this)
	}

	public abstract void parseDependencies()

	public static BaseFile create(String location) {
		create(null, location)
	}

	public static BaseFile create(Path parent, String location) {
		if(!location)
			return

		URI u = new URI(location);
		if (u.scheme != "oramds" && u.scheme != "file" && u.scheme != null)
			return

		if(!u.absolute) {
			if(!parent)
				return
			u = parent.resolveSibling(location).toUri()
		}

		try {
			def f = new File(location)
			if(f.name.toLowerCase() == "composite.xml")
				return new CompositeFile(u)

			def matcher = location =~ /^.*\.(.*)?$/
			def ext = matcher[0][1]
			switch(ext){
				case "xsd":
					return new XSDFile(u)
				case "wsdl":
					return new WSDLFile(u)
				case "bpel":
					return new BPELFile(u)
			}
		} catch (CreationException e) {
			return null
		}
	}

	@Override
	public String toString() {
		return "${filePath}";
	}
}
