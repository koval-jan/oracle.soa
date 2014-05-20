package com.gratex.mds

import groovy.transform.EqualsAndHashCode

import com.gratex.mds.file.CompositeFile
import com.orientechnologies.orient.core.serialization.serializer.object.OObjectSerializerHelperDocument

@EqualsAndHashCode
class Service extends OSerializable {

	def String iface
	def String port
	def String name
	def CompositeFile compositeFile

	@Override
	public String toString() {
		return "${name}";
	}
}
