package com.gratex.mds

import groovy.io.FileType
import groovyx.gpars.activeobject.ActiveObjectRegistry
import groovyx.gpars.group.DefaultPGroup

import com.gratex.mds.catalog.CompiletimeCatalog
import com.gratex.mds.catalog.RuntimeCatalog
import com.gratex.mds.exception.ProjectFileException
import com.gratex.mds.file.BaseFile

class MDSScanner {

	static def printErr = System.err.&println
	
	static main(args) {

		def cli = new CliBuilder(usage: 'oracle.soa.scanner -opmx[dl]')
		cli.with {
			h longOpt: 'help', 'Show usage information'
			p longOpt: 'projectdir', args: 1, argName: 'dir',  'Path to project dirs to scan recursively'
			o longOpt: 'out', args: 1, argName: 'dir',  'Path to output direcotry'
			m longOpt: 'mds', args: 1, argName: 'dir',  'Path to root of mds direcotry'
			x longOpt: 'mdsXml', args: 1, argName: 'file',  'Path to mds ip xml file'
			d longOpt: 'deps', args: 1, argName: 'name',  'List all transitive runtime and compiletime dependencies for defined project name'
			l longOpt: 'listprojects', 'List all project names'
		}

		def options = cli.parse(args)
		if (!options) {
			return
		}
		// Show usage text when -h or --help option is used.
		if (options.h) {
			cli.usage()
			return
		}
		
		if(!options.o){
			System.err.println("Specify output dir")
			cli.usage()
			System.exit(1)
			return
		}
		def outputdir = new File(options.o)
		if(!outputdir.exists()){
			printErr("Output dir ${outputdir.toString()} does not exist")
			System.exit(1)
		}
		
		if(!options.p){
			printErr("Specify project dir")
			cli.usage()
			System.exit(1)
			return
		}
		def projectdir = new File(options.p)
		if(!projectdir.exists()){
			printErr("Project dir ${projectdir.toString()} does not exist")
			System.exit(1)
		}
		
		if(!options.m){
			printErr("Specify mds dir")
			cli.usage()
			System.exit(1)
			return
		}
		def mdsdir = new File(options.m)
		if(!mdsdir.exists()){
			printErr("Mds dir ${mdsdir.toString()} does not exist")
			System.exit(1)
		}
		
		if(!options.x){
			printErr("Specify mds ip xml file")
			cli.usage()
			System.exit(1)
			return
		}
		def mdsIpXmlFile = new File(options.x)
		if(!mdsIpXmlFile.exists()){
			printErr("Mds ip xml file ${mdsIpXmlFile.toString()} does not exist")
			System.exit(1)
		}
		AllMds.getInstance(mdsdir, mdsIpXmlFile)
		
		def listProjects = options.l
		def projectDeps = options.d
		
		
		def svcActorPG = new DefaultPGroup(1)  //1 daemon thread pool
		ActiveObjectRegistry.instance.register("svcActorGroup", svcActorPG)
		def prjActorPG = new DefaultPGroup(1)  //1 daemon thread pool
		ActiveObjectRegistry.instance.register("prjActorGroup", prjActorPG)
		
		
		def RuntimeCatalog runtimeCtl
		def CompiletimeCatalog compiletimeCtl
		try {
			runtimeCtl = RuntimeCatalog.instance
			compiletimeCtl = CompiletimeCatalog.instance

			projectdir.eachFileRecurse(FileType.FILES) {
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
			
			compiletimeCtl.saveGraphML(new File(outputdir,'compileTimeDependencies.graphml').toString())
			compiletimeCtl.saveGraphwiz(new File(outputdir,'compileTimeDependencies.png').toString())
			

			runtimeCtl.saveGraphML(new File(outputdir,'runtimeDependencies.graphml').toString())
			runtimeCtl.saveGML(new File(outputdir,'runtimeDependencies.gml').toString())

			def sgArr = runtimeCtl.getNeigbourSubGraphs() 
			sgArr.each { k,v ->
				if(v.vertices.isEmpty())
					return
				def gw = new GraphvizWriter(v)
				gw.outputGraph(new File(outputdir,"runtime_${k}.png").toString())
				v.saveGraphML(new File(outputdir,"runtime_${k}.graphml").toString())
			}
			
			
			sgArr = compiletimeCtl.getNeigbourSubGraphs()
			sgArr.each { k,v ->
				if(v.vertices.isEmpty())
					return
				def gw = new GraphvizWriter(v)
				gw.outputGraph(new File(outputdir,"compiletime_${k}.png").toString())
				v.saveGraphML(new File(outputdir,"compiletime_${k}.graphml").toString())
			}
			
			if(listProjects) {
				println "******* RUNTIME/COMPILETIME PROJECT LIST ******"
				println "not implemented"
			}
			
			if(projectDeps) {
				println "******* RUNTIME/COMPILETIME DEPENDENCIES FOR PROJECT: ${projectDeps} ******"
				//def prfj="GTI.MINIPAY"
				//def prfj="UPVS.MVMEP"
				//def prfj="CO.ZEP.UI"
				//def prfj="CO.SYN.MINIK"
				def set = runtimeCtl.subTree(projectDeps)

				Set all = new LinkedHashSet()
				set.each{
					all.add(it)
					all.addAll(compiletimeCtl.subTree(it))
				}
				// print located projects
				all.each {
					println it
				}
			}
	
			runtimeCtl.commit()
			compiletimeCtl.commit()
		} catch (e) {
			printErr(e)
			runtimeCtl?.rollback()
			compiletimeCtl?.rollback()
		} finally {
			runtimeCtl?.shutdown()
			compiletimeCtl?.shutdown()
		}

	}

}
