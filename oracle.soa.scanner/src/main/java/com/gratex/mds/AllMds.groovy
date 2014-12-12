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

	//public static final String MDSPrefix = "file:///c:/Workspaces/MVSR/EGOV/MDSIP/MVSR.EGOV.MDSIP-Trunk/MVSR/EGOV/MDSIP/BPEL/osoa/MDS.SLN/ALL.MDS"
	
	HashSet<MdsInfo> mds
	
	@EqualsAndHashCode
	public static class MdsInfo {
		String name
		String logicalName
		Path dir
	}
	
	private static INSTANCE
	
	static getInstance(File mdsPrefix = null, File mdsipXml = null){
		if(!INSTANCE) {
			assert mdsPrefix : "mds root folder required"
			assert mdsipXml : "mds ip xml file required"
			INSTANCE = new AllMds(mdsPrefix, mdsipXml)
		} 
		return INSTANCE 
	}
	
	private String mdsURLPrefix
	
	private AllMds(File mdsPrefix, File mdsipXml){
		mds = new HashSet()
		mdsURLPrefix = mdsPrefix.toURI().toURL().toString()
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance()
		def builder = dbf.newDocumentBuilder()

		BufferedInputStream bis
		try{
			//new File("c:\\Workspaces\\TSpsiAll\\Logis\\TOP\\MVSR\\EGOV\\WKSMVSR.EGOV.MDSIP.xml")
			bis = new BufferedInputStream(new FileInputStream(mdsipXml))
			def top = builder.parse(bis).documentElement

			XPath xPath = XPathFactory.newInstance().newXPath()

			def nodes = xPath.evaluate( '/workspaces/build/solution/soaproject', top, XPathConstants.NODESET )
			nodes.each{
				def prjName = xPath.evaluate( 'text()', it )
				prjName = prjName.replaceAll("\\\\", "/").replaceFirst("ALL.MDS/", "")
				Path projectDirPath = Paths.get(new URI(mdsURLPrefix + "/" + prjName))
				
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
		def _actualURI = new URI(path.toString().replaceFirst("oramds:", mdsURLPrefix))
		[mdsInfo: info, actualURI:_actualURI, actualPath: Paths.get(_actualURI)]
	}

}
