package com.gratex.mds

import com.orientechnologies.orient.core.exception.OSerializationException;
import com.orientechnologies.orient.core.serialization.OSerializableStream

class OSerializable implements OSerializableStream {

	@Override
	public byte[] toStream() throws OSerializationException {
		final StringBuilder buffer = new StringBuilder()
		this.properties.each{ k,v ->
			write(buffer, v)
		}

		buffer.toString().getBytes();
	}

	@Override
	public OSerializableStream fromStream(byte[] iStream) throws OSerializationException {
		int i=0
		new String(iStream).split("(?<!\\|)\\|(?!\\|)").collect {
			properties[i++] = it.substring(1, it.length()-1)
		}
		return this;
	}

	private void write(final StringBuilder iBuffer, final Object iValue) {
		if (iBuffer.length() > 0)
			iBuffer.append('|');
		String v = " ${iValue.toString()} " ?: '  '
		v.replaceAll("|", "||")
		iBuffer.append(v);
	}

	static main(args) {
		"  | ad | asd | ||asda ||d".split("(?<!\\|)\\|(?!\\|)").each{
			println it.substring(1, it.length()-1)
		}
	}
}
