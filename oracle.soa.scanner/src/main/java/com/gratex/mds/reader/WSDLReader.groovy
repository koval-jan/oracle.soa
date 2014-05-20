package com.gratex.mds.reader

import java.nio.file.Path;

import javax.xml.namespace.NamespaceContext
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory

import com.gratex.mds.Catalog;

class WSDLReader {

	def read(Path wsdlPath){
		// look for project file


		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance()
		dbf.setNamespaceAware(true)
		def builder = dbf.newDocumentBuilder()

		BufferedInputStream bis
		try{
			bis = new BufferedInputStream(new FileInputStream(wsdlPath.toFile()))
			def wsdl = builder.parse(bis).documentElement

			XPath xPath = XPathFactory.newInstance().newXPath()
			xPath.setNamespaceContext(new NSC())

			def nodes = xPath.evaluate( '//wsdl:import', wsdl, XPathConstants.NODESET )
			nodes.each{
				def location = xPath.evaluate( '@location', it )

				//Catalog.instance.addFile

				println location
			}

			xPath = XPathFactory.newInstance().newXPath()
			xPath.setNamespaceContext(new NSC())

			nodes = xPath.evaluate( '//xsd:import', wsdl, XPathConstants.NODESET )
			nodes.each{
				def schemaLocation = xPath.evaluate( '@schemaLocation', it )

				println schemaLocation
			}
		} finally {
		 	bis?.close()
		}
	}

	class NSC implements NamespaceContext {
		def Map nsp = [:]
		def Map nsu = [:]

		def addNs = { p, u -> nsp[p]=u; nsu[u]=p }

		public NSC(){
			addNs('xsd', 'http://www.w3.org/2001/XMLSchema')
			addNs('wsdl', 'http://schemas.xmlsoap.org/wsdl/')
			addNs('bpel1', 'http://schemas.xmlsoap.org/ws/2003/03/business-process/')
			addNs('bpel2', 'http://docs.oasis-open.org/wsbpel/2.0/process/executable')
			addNs('sca', 'http://xmlns.oracle.com/sca/1.0')


		}

		@Override
		public String getNamespaceURI(String prefix) {
			nsp[prefix]
		}

		@Override
		public String getPrefix(String namespaceURI) {
			nsu[namespaceURI]
		}

		@Override
		public Iterator getPrefixes(String namespaceURI) {
			nsp.keySet()
		}
	}
}
