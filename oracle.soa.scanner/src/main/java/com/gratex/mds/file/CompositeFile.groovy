package com.gratex.mds.file


import java.net.URI;
import java.nio.file.Path
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

import com.gratex.mds.Service
import com.gratex.mds.Reference
import com.gratex.mds.reader.WSDLReader.NSC

public class CompositeFile extends BaseFile {
	public CompositeFile(String file){		super(file)
	}

	public CompositeFile(URI fURI) {
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

			def nodes = xPath.evaluate( '//sca:component/sca:implementation.bpel', bpel, XPathConstants.NODESET )
			nodes.each{
				def location = xPath.evaluate( '@src', it )
				def bf = BaseFile.create(filePath, location)
				if(bf) {
					prjCtlg.addProjectEdge(self, bf.self)
					bf.parseDependencies()
					println "sca import $location"
				}
			}

			nodes = xPath.evaluate( '//sca:import', bpel, XPathConstants.NODESET )
			nodes.each{
				def location = xPath.evaluate( '@location', it )
				def bf = BaseFile.create(filePath, location)
				if(bf) {
					prjCtlg.addProjectEdge(self, bf.self)
					bf.parseDependencies()
					println "sca import $location"
				}
			}

			nodes = xPath.evaluate( '/sca:composite/sca:service', bpel, XPathConstants.NODESET )
			nodes.each{
				def iface = xPath.evaluate( 'sca:interface.wsdl/@interface', it )
				def port = xPath.evaluate( 'sca:binding.ws/@port', it )
				def n = xPath.evaluate( '@name', it )

				def s = new Service(iface: iface, port: port, name: n, compositeFile: this)
				svcCtlg.addService(s)
				println "Service $iface $port"
			}

			nodes = xPath.evaluate( '/sca:composite/sca:reference', bpel, XPathConstants.NODESET )
			nodes.each{
				def iface = xPath.evaluate( 'sca:interface.wsdl/@interface', it )
				def port = xPath.evaluate( 'sca:binding.ws/@port', it )
				def n = xPath.evaluate( '@name', it )

				def r = new Reference(iface: iface, port: port, name: n, compositeFile: this)
				svcCtlg.addReference(r)
				println "Reference $iface $port"
			}
		} finally {
			bis?.close()
		}
	}
}
