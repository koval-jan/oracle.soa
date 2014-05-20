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

class RuntimeModuleCatalog {

	def OServer server
	def OrientGraph g
	def OrientGraph gModules
	def OrientGraphFactory factory
	def OrientGraphFactory factoryModules

	def Index fielNameIdx
	def Index projectIdx
	def Index serviceIdx
	def Index referenceIdx

	private static final INSTANCE = new RuntimeModuleCatalog()
	static getInstance(){ return INSTANCE }

	private RuntimeModuleCatalog() {
		server = OServerMain.create()
		server.startup(MDSScanner.class.getResourceAsStream("/db.cfg.xml"))
		//server.openDatabase("graph", "plocal:c:/temp/orientdb/test", "root", "ThisIsA_TEST")
		Gremlin.load()
		OGremlinHelper.global().create()

		factoryModules = new OrientGraphFactory("memory:test2").setupPool(1,10)
		gModules = factoryModules.getTx()
		projectIdx = gModules.createIndex("projectIdx",  Vertex.class)
		serviceIdx = gModules.createIndex("serviceIdx",  Vertex.class)
		referenceIdx = gModules.createIndex("referenceIdx",  Vertex.class)

	}

	public Vertex addService(Service svc) {
		def pName = svc.compositeFile.projectFile.toString()
		def v = projectIdx[[projectKey: "${pName}"]].toList()[0]
		if(!v) {
			v = gModules.addVertex(null, [projectName: svc.compositeFile.projectName, projectFile: svc.compositeFile.projectFile, services: new HashSet(), references: new HashSet()])
			projectIdx.put("projectKey", "${pName}", v)
		}
		v.getProperty("services").add(svc)
		def key = "${svc.iface}_${svc.port}"
		serviceIdx.put("serviceKey", key , v)
		v.save()
		return v
	}

	public Vertex addReference(Reference ref) {
		def pName = ref.compositeFile.projectFile.toString()

		def v = projectIdx[[projectKey: "${pName}"]].toList()[0]
		if(!v) {
			v = gModules.addVertex(null, [projectName: ref.compositeFile.projectName, projectFile: ref.compositeFile.projectFile, services: new HashSet(), references: new HashSet()])
			projectIdx.put("projectKey", "${pName}", v)
		}
		v.getProperty("references").add(ref)
		def key = "${ref.iface}_${ref.port}"
		referenceIdx.put("referenceKey", key, v)
		v.save()
		return v
	}

	public addReferenceEdges(Vertex ref) {
		def refs = ref.getProperty("references")
		refs.each { Reference r ->
			def key = "${r.iface}_${r.port}"
			def srv = serviceIdx[[serviceKey: key]].toList()[0]
			if(srv) {
				def e = gModules.addEdge(null, ref, srv, "REFERENCES")
				e.setProperty("URL", "${r.name}")
				e.setProperty("description", "${r.name}")
				e.save()
			}
		}
	}

}
