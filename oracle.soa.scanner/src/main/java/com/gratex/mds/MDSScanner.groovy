package com.gratex.mds

import groovy.io.FileType
import groovyx.gpars.activeobject.ActiveObjectRegistry
import groovyx.gpars.group.DefaultPGroup

import com.gratex.mds.catalog.CompiletimeCatalog
import com.gratex.mds.catalog.RuntimeCatalog
import com.gratex.mds.exception.ProjectFileException
import com.gratex.mds.file.BaseFile

class MDSScanner {

	static main(args) {

		def svcActorPG = new DefaultPGroup(1)  //1 daemon thread pool
		ActiveObjectRegistry.instance.register("svcActorGroup", svcActorPG)
		def prjActorPG = new DefaultPGroup(1)  //1 daemon thread pool
		ActiveObjectRegistry.instance.register("prjActorGroup", prjActorPG)
		
		
		def RuntimeCatalog runtimeCtl
		def CompiletimeCatalog compiletimeCtl
		try {
			runtimeCtl = RuntimeCatalog.instance
			compiletimeCtl = CompiletimeCatalog.instance

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

			runtimeCtl.postprocessReferences()
			
			compiletimeCtl.saveGraphML('c:/compileTimeDependencies.graphml')
			//compiletimeCtl.saveGraphwiz('c:/compileTimeDependencies.png')
			

			runtimeCtl.saveGraphML('c:/runtimeDependencies.graphml')
			runtimeCtl.saveGML('c:/runtimeDependencies.gml')

			def sgArr = runtimeCtl.getNeigbourSubGraphs() 
			sgArr.each { k,v ->
				if(v.vertices.isEmpty())
					return
				def gw = new GraphvizWriter(v)
				gw.outputGraph("c:/runtime_${k}.png")
				v.saveGraphML("c:/runtime_${k}.graphml")
			}
			
			
			sgArr = compiletimeCtl.getNeigbourSubGraphs()
			sgArr.each { k,v ->
				if(v.vertices.isEmpty())
					return
				def gw = new GraphvizWriter(v)
				gw.outputGraph("c:/compiletime_${k}.png")
				v.saveGraphML("c:/compiletime_${k}.graphml")
			}
			
			println "*******"
			//def prfj="GTI.MINIPAY"
			//def prfj="UPVS.MVMEP"
			//def prfj="CO.ZEP.UI"
			def prfj="CO.SYN.MINIK"
			def set = runtimeCtl.subTree(prfj)
			println set
			Set all = new LinkedHashSet()
			set.each{
				all.add(it)
				all.addAll(compiletimeCtl.subTree(it))
			}
			println all
			
//			println runtimeCtl.subTree("UPVS.MVG2G")
//			println runtimeCtl.subTree("CO.SYN.MINIK")
//			println runtimeCtl.subTree("UPVS.MVMEP")
			
			runtimeCtl.commit()
		} catch (e) {
			println e
			runtimeCtl?.rollback()
			compiletimeCtl?.rollback()
		} finally {
			runtimeCtl?.shutdown()
			compiletimeCtl?.shutdown()
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
