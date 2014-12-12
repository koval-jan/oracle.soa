package com.gratex.mds;

import groovy.transform.EqualsAndHashCode;

import java.nio.file.Path
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

import com.gratex.mds.file.BaseFile
import com.gratex.mds.reader.WSDLReader.NSC

public class AllMds {

	public static final String MDSPrefix = "file:///c:/Workspaces/MVSR/EGOV/MDSIP/MVSR.EGOV.MDSIP-Trunk/MVSR/EGOV/MDSIP/BPEL/osoa/MDS.SLN/ALL.MDS"
	
	HashSet<MdsInfo> mds
	
	@EqualsAndHashCode
	public static class MdsInfo {
		String name
		String logicalName
		Path dir
	}
	
	private static final INSTANCE = new AllMds()
	static getInstance(){ return INSTANCE }
	
	private AllMds(){
		mds = new HashSet()
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance()
		def builder = dbf.newDocumentBuilder()

		BufferedInputStream bis
		try{
			bis = new BufferedInputStream(new FileInputStream(new File("c:\\Workspaces\\TSpsiAll\\Logis\\TOP\\MVSR\\EGOV\\WKSMVSR.EGOV.MDSIP.xml")))
			def top = builder.parse(bis).documentElement

			XPath xPath = XPathFactory.newInstance().newXPath()

			def nodes = xPath.evaluate( '/workspaces/build/solution/soaproject', top, XPathConstants.NODESET )
			nodes.each{
				def prjName = xPath.evaluate( 'text()', it )
				prjName = prjName.replaceAll("\\\\", "/").replaceFirst("ALL.MDS/", "")
				Path projectDirPath = Paths.get(new URI(AllMds.MDSPrefix + "/" + prjName))
				
				mds.add(new MdsInfo(name: prjName, logicalName: prjName.replaceAll("/", "."), dir:projectDirPath))
			}

		} finally {
			bis?.close()
		}
	}
	

	public def resolveInfo(URI path) {
		def info = mds.find { path.path.startsWith("/${it.name}") }
		if(!info)
			return null
		def _actualURI = new URI(path.toString().replaceFirst("oramds:", AllMds.MDSPrefix))
		[mdsInfo: info, actualURI:_actualURI, actualPath: Paths.get(_actualURI)]
	}

}
