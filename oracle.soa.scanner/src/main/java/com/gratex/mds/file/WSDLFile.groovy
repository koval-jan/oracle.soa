package com.gratex.mds.file

import java.net.URI;
import java.nio.file.Path
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

import com.gratex.mds.reader.WSDLReader.NSC

public class WSDLFile extends BaseFile {
	public WSDLFile(String file){		super(file)
	}

	public WSDLFile(URI fURI) {
		super(fURI)
	}

	public void parseDependencies(){
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance()
		dbf.setNamespaceAware(true)
		def builder = dbf.newDocumentBuilder()

		BufferedInputStream bis
		try{
			bis = new BufferedInputStream(new FileInputStream(filePath.toFile()))
			def wsdl = builder.parse(bis).documentElement

			XPath xPath = XPathFactory.newInstance().newXPath()
			xPath.setNamespaceContext(new NSC())

			def nodes = xPath.evaluate( '//wsdl:import', wsdl, XPathConstants.NODESET )
			nodes.each{
				def location = xPath.evaluate( '@location', it )
				def bf = BaseFile.create(filePath, location)
				if(bf) {
					//c.addEdge(self, bf.self)
					c.addProjectEdge(prjSelf, bf.prjSelf)
					bf.parseDependencies()
					println "wsdl import $location"
				}
			}

			xPath = XPathFactory.newInstance().newXPath()
			xPath.setNamespaceContext(new NSC())

			nodes = xPath.evaluate( '//xsd:import', wsdl, XPathConstants.NODESET )
			nodes.each{
				def location = xPath.evaluate( '@schemaLocation', it )
				def bf = BaseFile.create(filePath, location)
				if(bf) {
					//c.addEdge(self, bf.self)
					c.addProjectEdge(prjSelf, bf.prjSelf)
					bf.parseDependencies()
					println "wsdl import $location"
				}
			}

			nodes = xPath.evaluate( '//xsd:include', wsdl, XPathConstants.NODESET )
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
