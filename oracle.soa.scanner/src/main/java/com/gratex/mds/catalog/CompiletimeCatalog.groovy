package com.gratex.mds.catalog

import java.util.List;
import java.util.Map;

import groovyx.gpars.activeobject.ActiveMethod
import groovyx.gpars.activeobject.ActiveObject

import com.gratex.mds.GraphvizWriter;
import com.gratex.mds.MDSScanner
import com.gratex.mds.ProjectGraphvizWriter
import com.gratex.mds.file.BaseFile
import com.orientechnologies.orient.graph.gremlin.OGremlinHelper
import com.orientechnologies.orient.server.OServer
import com.orientechnologies.orient.server.OServerMain
import com.tinkerpop.blueprints.Edge
import com.tinkerpop.blueprints.Index
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.OrientGraph
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory
import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import com.tinkerpop.blueprints.util.ElementHelper;
import com.tinkerpop.gremlin.groovy.Gremlin

@ActiveObject("prjActorGroup")
class CompiletimeCatalog {

	def OServer server
	def OrientGraph g
	def OrientGraphFactory factory

	def Index fielNameIdx
	def Index projectIdx

	private static final INSTANCE = new CompiletimeCatalog()
	static getInstance(){
		return INSTANCE
	}

	private CompiletimeCatalog() {
		server = OServerMain.create()
		server.startup(MDSScanner.class.getResourceAsStream("/db.cfg.xml"))

		Gremlin.load()
		OGremlinHelper.global().create()

		factory = new OrientGraphFactory("memory:test").setupPool(1,10)
		def gt = factory.getNoTx()
		//fielNameIdx = gt.createIndex("fileNameIdx",  Vertex.class)
		projectIdx = gt.createIndex("projectIdx",  Vertex.class)
		gt.createEdgeType("USES");
		g = factory.getTx()
	}

	@ActiveMethod(blocking=true)
	public Vertex addProjectVertex(BaseFile file) {
		def pName = file.projectFile.toString()
		def v = projectIdx[[projectName: pName]].toList()[0]
		if(!v) {
			v = g.addVertex(null, [projectName: file.projectName, projectFile: file.projectFile])
			v.save()
			projectIdx.put("projectName", pName, v)
			projectIdx.put("projectLogicalname", file.projectName, v)
			
		}
		v
	}

	@ActiveMethod(blocking=true)
	public Edge addProjectEdge(Vertex src, Vertex dst) {
		if(src == dst)
			return null

		def e = g.addEdge(null, src, dst, "USES")
		e.save()
		e
	}
	
	def static Vertex goc(Vertex v, com.tinkerpop.blueprints.Graph  g){
		def nv=g.getVertex(v.id)
		if(nv==null){
			nv=g.addVertex(v.id, ElementHelper.getProperties(v))
		}
		nv
	}
	
	@ActiveMethod(blocking=true)
	public Set subTree(String name) {
		println name
		def results = new LinkedHashSet()
		def r = []
		def p = projectIdx[["projectLogicalname": name.toString()]]
		if(p) {
			//println p.toList()
			p.as('x').sideEffect{ results << it.getProperty('projectName')}.out.simplePath.loop('x'){it.loops < 5}{true}.dedup.fill(r);
			results.addAll(r.collect { it.getProperty('projectName') })
		}
		
		results
	}
	
	@ActiveMethod(blocking=true)
	public Map getNeigbourSubGraphs() {
		def sgArr = [:]
		def sg
		def t = this

		g.V.sideEffect {
			sg = new TinkerGraph()
			sgArr[it.getProperty('projectName')] = sg
		}.bothE("USES").sideEffect {
			if(!sg.getEdge(it.id))
				sg.addEdge(it.id, t.goc(it.outV.next(), sg), t.goc(it.inV.next(), sg), it.label, ElementHelper.getProperties(it))
		}.iterate()

		sgArr
	}

	@ActiveMethod(blocking=true)
	public saveGraphML(def loc) {
		g.commit()
		g.saveGraphML(loc)
	}

	@ActiveMethod(blocking=true)
	public saveGML(def loc) {
		g.commit()
		g.saveGML(loc)
	}

	@ActiveMethod(blocking=true)
	public saveGraphwiz(def loc) {
		g.commit()
		def gw = new ProjectGraphvizWriter(g)
		gw.outputGraph(loc)
	}


	@ActiveMethod(blocking=true)
	public commit() {
		g.commit()
	}

	@ActiveMethod(blocking=true)
	public rollback() {
		g?.rollback()
	}

	@ActiveMethod(blocking=true)
	public shutdown() {
		server?.shutdown()
	}
}
