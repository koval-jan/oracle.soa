package com.gratex.mds.catalog

import groovyx.gpars.activeobject.ActiveMethod
import groovyx.gpars.activeobject.ActiveObject

import com.gratex.mds.MDSScanner
import com.gratex.mds.Reference
import com.gratex.mds.Service
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
import com.tinkerpop.blueprints.util.ElementHelper
import com.tinkerpop.gremlin.groovy.Gremlin

@ActiveObject("svcActorGroup")
class RuntimeCatalog {

	def OServer server
	def OrientGraph g
	def OrientGraphFactory factory

	def Index projectIdx
	def Index projectNameIdx
	def Index serviceIdx
	def Index referenceIdx

	private static final INSTANCE = new RuntimeCatalog()
	static getInstance(){
		return INSTANCE
	}

	def static Vertex goc(Vertex v, com.tinkerpop.blueprints.Graph  g){
		def nv=g.getVertex(v.id)
		if(nv==null){
			nv=g.addVertex(v.id, ElementHelper.getProperties(v))
		}
		nv
	}

	private RuntimeCatalog() {
		server = OServerMain.create()
		server.startup(MDSScanner.class.getResourceAsStream("/db.cfg.xml"))

		Gremlin.load()
		OGremlinHelper.global().create()

		factory = new OrientGraphFactory("memory:test2").setupPool(1,10)
		def gt = factory.getNoTx()
		projectIdx = gt.createIndex("projectIdx",  Vertex.class)	
		projectNameIdx = gt.createIndex("projectNameIdx",  Vertex.class)
		serviceIdx = gt.createIndex("serviceIdx",  Vertex.class)
		referenceIdx = gt.createIndex("referenceIdx",  Vertex.class)
		gt.createEdgeType("REFERENCES");
		g = factory.getTx()
	}

	@ActiveMethod(blocking=true)
	public Vertex addService(Service svc) {
		def pName = svc.compositeFile.projectFile.toString()
		def logName = svc.compositeFile.projectName.toString()
		def v = projectIdx[[projectKey: "${pName}"]].toList()[0]
		if(!v){
			v = g.addVertex(null, [projectName: logName, projectFile: svc.compositeFile.projectFile, services: new HashSet(), references: new HashSet()])
			projectIdx.put("projectKey", "${pName}", v)
			projectNameIdx.put("projectName", logName, v)
		}
		v.getProperty("services").add(svc)
		def key = svc.key()
		serviceIdx.put("serviceKey", key , v)
		v.save()
		v
	}

	@ActiveMethod(blocking=true)
	public Vertex addReference(Reference ref) {
		def pName = ref.compositeFile.projectFile.toString()
		def logName = ref.compositeFile.projectName.toString()
		def v = projectIdx[[projectKey: "${pName}"]].toList()[0]
		if(!v){
			v = g.addVertex(null, [projectName: logName, projectFile: ref.compositeFile.projectFile, services: new HashSet(), references: new HashSet()])
			projectIdx.put("projectKey", "${pName}", v)
			projectNameIdx.put("projectName", logName, v)
		}
		v.getProperty("references").add(ref)
		def key = ref.key()
		referenceIdx.put("referenceKey", key, v)
		v.save()
		v
	}

	@ActiveMethod(blocking=true)
	public Map getNeigbourSubGraphs() {
		def sgArr = [:]
		def sg
		def t = this

		g.V.sideEffect {
			sg = new TinkerGraph()
			sgArr[it.getProperty('projectName')] = sg
		}.bothE("REFERENCES").sideEffect {
			if(!sg.getEdge(it.id))
				sg.addEdge(it.id, t.goc(it.outV.next(), sg), t.goc(it.inV.next(), sg), it.label, ElementHelper.getProperties(it))
		}.iterate()

		sgArr
	}
	
	@ActiveMethod(blocking=true)
	public Set subTree(String name) {
		def results = new LinkedHashSet()
		def r = []
		def p = projectNameIdx[["projectName": name.toString()]]
		if(p) {
			//println p.toList()
			p.as('x').sideEffect{ results << it.getProperty('projectName')}.out.simplePath.loop('x'){it.loops < 5}{true}.dedup.fill(r);
			results.addAll( r.collect { it.getProperty('projectName') })
		}
		
		results
	}

	@ActiveMethod(blocking=true)
	public void postprocessReferences() {
		def t = this
		g.getVertices().each {
			def srcSvcV = it
			def refs = srcSvcV.getProperty("references")
			refs.each { Reference r ->
				def key = r.key()
				def dstSvcV = serviceIdx[[serviceKey: key]].toList()[0]
				if(dstSvcV) {
					
					println "reference > service hit ${srcSvcV.getProperty('projectName')} ${dstSvcV.getProperty('projectName')} ${key}"
					def e = g.addEdge(null, srcSvcV, dstSvcV, "REFERENCES")
					e.setProperty("URL", "${r.name}")
					e.setProperty("description", "${r.name}")
					e.save()
				} else {
					println "reference > service miss ${srcSvcV.getProperty('projectName')} ${key}"
				}
			}
		}
	}

	@ActiveMethod(blocking=true)
	public saveGraphML(def loc) {
		//g.commit()
		g.saveGraphML(loc)
	}

	@ActiveMethod(blocking=true)
	public saveGML(def loc) {
		//g.commit()
		g.saveGML(loc)
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
