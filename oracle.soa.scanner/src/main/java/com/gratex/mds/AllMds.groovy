package com.gratex.mds;

import java.nio.file.Path

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

import com.gratex.mds.file.BaseFile
import com.gratex.mds.reader.WSDLReader.NSC

public class AllMds {

	private static final INSTANCE = new AllMds()
	static getInstance(){ return INSTANCE }

	HashSet mds

	public String resolveProjectName(URI path) {
		if(!mds) {
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
					def name = xPath.evaluate( 'text()', it )
					mds.add(name.replaceAll("\\\\", "/").replaceFirst("ALL.MDS/", ""))
				}

			} finally {
				bis?.close()
			}
		}

		mds.find { path.path.startsWith("/$it") }
	}

}
