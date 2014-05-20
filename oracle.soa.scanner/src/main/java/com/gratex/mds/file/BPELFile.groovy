package com.gratex.mds.file


import java.net.URI;
import java.nio.file.Path
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

import com.gratex.mds.reader.WSDLReader.NSC

public class BPELFile extends BaseFile {
	public BPELFile(String file){		super(file)
	}

	public BPELFile(URI fURI) {
		super(fURI)
	}

	public void parseDependencies(){
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance()
		dbf.setNamespaceAware(true)
		def builder = dbf.newDocumentBuilder()

		BufferedInputStream bis
		try{
			bis = new BufferedInputStream(new FileInputStream(filePath.toFile()))
			def bpel = builder.parse(bis).documentElement

			XPath xPath = XPathFactory.newInstance().newXPath()
			xPath.setNamespaceContext(new NSC())

			xPath = XPathFactory.newInstance().newXPath()
			xPath.setNamespaceContext(new NSC())

			def nodes = xPath.evaluate( '//bpel2:import', bpel, XPathConstants.NODESET )
			nodes.each{
				def location = xPath.evaluate( '@location', it )
				def bf = BaseFile.create(filePath, location)
				if(bf) {
					//c.addEdge(self, bf.self)
					c.addProjectEdge(prjSelf, bf.prjSelf)
					bf.parseDependencies()
					println "bpel import $location"
				}
			}

			nodes = xPath.evaluate( '//bpel1:import', bpel, XPathConstants.NODESET )
			nodes.each{
				def location = xPath.evaluate( '@location', it )
				def bf = BaseFile.create(filePath, location)
				if(bf) {
					//c.addEdge(self, bf.self)
					c.addProjectEdge(prjSelf, bf.prjSelf)
					bf.parseDependencies()
					println "bpel import $location"
				}
			}
		} finally {
			bis?.close()
		}
	}
}
