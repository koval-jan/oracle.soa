package com.gratex.mds.file

import java.nio.file.Path

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

import com.gratex.mds.reader.WSDLReader.NSC

public class XSDFile extends BaseFile {

	public XSDFile(String file){		super(file)
	}

	public XSDFile(URI fURI) {
		super(fURI)
	}

	public void parseDependencies(){
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance()
		dbf.setNamespaceAware(true)
		def builder = dbf.newDocumentBuilder()

		BufferedInputStream bis
		try{
			bis = new BufferedInputStream(new FileInputStream(filePath.toFile()))
			def xsd = builder.parse(bis).documentElement

			XPath xPath = XPathFactory.newInstance().newXPath()
			xPath.setNamespaceContext(new NSC())

			xPath = XPathFactory.newInstance().newXPath()
			xPath.setNamespaceContext(new NSC())

			def nodes = xPath.evaluate( '//xsd:import', xsd, XPathConstants.NODESET )
			nodes.each{
				def location = xPath.evaluate( '@schemaLocation', it )
				def bf = BaseFile.create(filePath, location)
				if(bf) {
					//c.addEdge(self, bf.self)
					c.addProjectEdge(prjSelf, bf.prjSelf)
					bf.parseDependencies()
					println "xsd import $location"
				}
			}

			nodes = xPath.evaluate( '//xsd:include', xsd, XPathConstants.NODESET )
			nodes.each{
				def location = xPath.evaluate( '@schemaLocation', it )
				def bf = BaseFile.create(filePath, location)
				if(bf) {
					//c.addEdge(self, bf.self)
					c.addProjectEdge(prjSelf, bf.prjSelf)
					bf.parseDependencies()
					println "xsd include $location"
				}
			}
		} finally {
			bis?.close()
		}
	}
}
