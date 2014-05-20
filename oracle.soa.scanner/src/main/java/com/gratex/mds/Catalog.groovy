package com.gratex.mds

import com.gratex.mds.file.BaseFile
import com.orientechnologies.orient.graph.gremlin.OGremlinHelper
import com.orientechnologies.orient.server.OServer
import com.orientechnologies.orient.server.OServerMain
import com.tinkerpop.blueprints.Edge
import com.tinkerpop.blueprints.Index
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.OrientGraph
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory
import com.tinkerpop.gremlin.groovy.Gremlin

class Catalog {

	def OServer server
	def OrientGraph g
	def OrientGraph gModules
	def OrientGraphFactory factory
	def OrientGraphFactory factoryModules

	def Index fielNameIdx
	def Index projectIdx

	private static final INSTANCE = new Catalog()
	static getInstance(){ return INSTANCE }

	private Catalog() {
		server = OServerMain.create()
		server.startup(MDSScanner.class.getResourceAsStream("/db.cfg.xml"))
		//server.openDatabase("graph", "plocal:c:/temp/orientdb/test", "root", "ThisIsA_TEST")
		Gremlin.load()
		OGremlinHelper.global().create()

//		factory = new OrientGraphFactory("memory:test").setupPool(1,10)
//		g = factory.getTx()
//		fielNameIdx = g.createIndex("fileNameIdx",  Vertex.class)

		factoryModules = new OrientGraphFactory("memory:test2").setupPool(1,10)
		gModules = factoryModules.getTx()
		projectIdx = gModules.createIndex("projectIdx",  Vertex.class)
	}

	public Vertex addVertex(BaseFile file) {
		def fName = file.filePath.toString()
		def v = fielNameIdx[[fileName: fName]].toList()[0]
		if(!v) {
			v = g.addVertex(null, [fileName: fName, project: file.projectName, relPath: file.relPath])
			v.save()
			fielNameIdx.put("fileName", fName, v)
//			g.commit()
		}
		return v
	}

	public Edge addEdge(Vertex src, Vertex dst) {
		if(src == dst)
			return null

		def e = g.addEdge(null, src, dst, "USES")
		e.save()
//		g.commit()
		return e
	}

	public Vertex addProjectVertex(BaseFile file) {
		def pName = file.projectFile.toString()
		def v = projectIdx[[projectName: pName]].toList()[0]
		if(!v) {
			v = gModules.addVertex(null, [projectName: file.projectName, projectFile: file.projectFile])
			v.save()
			projectIdx.put("projectName", pName, v)

		}
		return v
	}

	public Edge addProjectEdge(Vertex src, Vertex dst) {
		if(src == dst)
			return null

		def e = gModules.addEdge(null, src, dst, "USES")
		e.save()
		return e
	}
}
