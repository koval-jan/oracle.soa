package com.gratex.mds

import groovy.io.FileType

import java.util.Formatter.DateTime

import com.couggi.javagraphviz.Digraph
import com.couggi.javagraphviz.Node
import com.couggi.javagraphviz.Edge
import com.couggi.javagraphviz.Graph;
import com.couggi.javagraphviz.GraphvizEngine
import com.gratex.mds.exception.ProjectFileException
import com.gratex.mds.file.BaseFile;
import com.orientechnologies.orient.core.serialization.serializer.object.OObjectSerializer
import com.orientechnologies.orient.object.serialization.OObjectSerializerContext
import com.orientechnologies.orient.object.serialization.OObjectSerializerHelper
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.ElementHelper;
import com.tinkerpop.blueprints.util.GraphHelper;

class MDSScanner {

	public static final String MDSPrefix = "file:///c:/Workspaces/MVSR/EGOV/MDSIP/MVSR.EGOV.MDSIP-Trunk/MVSR/EGOV/MDSIP/BPEL/osoa/MDS.SLN/ALL.MDS"

	def static Vertex goc(Vertex v, com.tinkerpop.blueprints.Graph  g){
		def nv=g.getVertex(v.id)
		if(nv==null){
			nv=g.addVertex(v.id, ElementHelper.getProperties(v))
		}
		nv
	}

	static main(args) {

		def RuntimeModuleCatalog c
		try {
			c = RuntimeModuleCatalog.getInstance()

			new File("c:/Workspaces/MVSR/EGOV").eachFileRecurse(FileType.FILES) {
				if( it.name  ==~ ".*composite.xml") {
					try {
						def bf = BaseFile.create(it.toURI().toString())
						bf.parseDependencies()
					} catch (FileNotFoundException | ProjectFileException e) {
						println e
					}
				}
			}

			c.gModules.getVertices().each {
				c.addReferenceEdges(it)
			}
			//c.g.commit()
			//c.g.saveGraphML('c:/test.graphml')

//			c.gModules.saveGraphML('c:/testModule.graphml')
//			c.gModules.commit()

			c.gModules.saveGraphML('c:/testModuleRefs.graphml')
			c.gModules.saveGML('c:/testModuleRefs.gml')

			def sgArr = [:]
			def sg
			def t = this

			c.gModules.V.sideEffect {
				sg = new TinkerGraph()
				sgArr[it.getProperty('projectName')] = sg
			}.bothE("REFERENCES").sideEffect {
				if(!sg.getEdge(it.id))
					sg.addEdge(it.id, t.goc(it.outV.next(), sg), t.goc(it.inV.next(), sg), it.label, ElementHelper.getProperties(it))
			}.iterate()

			sgArr.each { k,v ->
				if(v.vertices.isEmpty())
					return
				def gw = new GraphvizWriter(v)
				gw.outputGraph("c:/dep_${k}.png")
				v.saveGraphML("c:/dep_${k}.graphml")
			}

			c.gModules.commit()
		} catch (e) {
			println e
			//c?.g?.rollback()
			c?.gModules?.rollback()
		} finally {
			c?.server?.shutdown()
		}

//		def OServer server
//		def OrientGraph g
//		try {
//			server = OServerMain.create()
//			server.startup(MDSScanner.class.getResourceAsStream("/db.cfg.xml"))
//			//server.openDatabase("graph", "plocal:c:/temp/orientdb/test", "root", "ThisIsA_TEST")
//
////			OrientGraphFactory factory = new OrientGraphFactory("plocal:C:/temp/orientdb/test").setupPool(1,10)
////			graph = factory.getTx()
////			graph.drop();
//			Gremlin.load()
//			OGremlinHelper.global().create()
//			OrientGraphFactory factory = new OrientGraphFactory("memory:test").setupPool(1,10)
//			g = factory.getTx()
//			def fielNameIdx = g.createIndex("fileNameIdx",  Vertex.class)
//
//			Vertex f1 = g.addVertex(1, [name:'1', fileName: 'file1', project: 'A'])
//			fielNameIdx.put("fileName", 'file1', f1)
//
//			Vertex f2 = g.addVertex(name: '2', fileName: "file2", project: "A" )
//			fielNameIdx.put("fileName", 'file2', f2)
//
//			Vertex f3 = g.addVertex(name: '3', fileName: "file3", project: "A" )
//			fielNameIdx.put("fileName", 'file3', f3)
//
//			Vertex f4 = g.addVertex(name: '4', fileName: "file4", project: "B" )
//			fielNameIdx.put("fileName", 'file4', f4)
//
//			Vertex f5 = g.addVertex(name: '5', fileName: "file5", project: "C" )
//			fielNameIdx.put("fileName", 'file5', f5)
//
//			/*
//			 *       5---
//			 *       ^    \
//			 *       |    |
//			 *       |    V
//			 * 	1 -> 2 -> 3
//			 *       ^
//			 *       |
//			 *       4
//			 *
//			 */
//
//			Edge e1 = g.addEdge(null, f1, f2, "depends")
//			Edge e2 = g.addEdge(null, f2, f3, "depends")
//			Edge e3 = g.addEdge(null, f2, f5, "depends")
//			Edge e4 = g.addEdge(null, f5, f3, "depends")
//			Edge e5 = g.addEdge(null, f4, f2, "depends")
//			g.commit()
//			//g.saveGraphML('c:/test.graphml')
//			//println g
//
//			def results = []
////			g.V('fileName', 'file1').as('x').out.simplePath.loop('x'){ i ->
////				i.loops < 3
////			}.fill(results);
////			results.each {
////				println it.fileName
////			}
//
//			fielNameIdx[[fileName: 'file1']].as('x').out.simplePath.loop('x'){it.loops < 10}{true}.dedup.fill(results);
//			results.each {
//				println it.fileName
//			}
//			//.out.simplePath.loop(2){it.loops < 2000}.path
//
//
//
//
//		} finally {
//			g?.rollback();
//			server?.shutdown();
//		}
	}

}
