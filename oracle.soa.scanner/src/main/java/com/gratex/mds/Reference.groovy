package com.gratex.mds

import groovy.transform.EqualsAndHashCode;

import com.gratex.mds.file.CompositeFile;
import com.orientechnologies.orient.core.serialization.serializer.object.OObjectSerializerHelperDocument;

@EqualsAndHashCode
class Reference  extends OSerializable {

	def String iface
	def String port
	def String name
	def CompositeFile compositeFile

	@Override
	public String toString() {
		return "${name}";
	}
	
	def key() {
		def m = port =~ /(.*)(#wsdl\.endpoint\().*\/.*(\))/;
		if(m.matches()) {
			"${iface}_${m[0][1]}${m[0][2]}${m[0][3]}".toString()
		} else {
			"${iface}_${port}".toString()
		}
	}
}
