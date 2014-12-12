package com.gratex.mds

import com.couggi.javagraphviz.Digraph
import com.couggi.javagraphviz.GraphvizEngine
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph
import com.tinkerpop.blueprints.impls.orient.OrientGraph

class ProjectGraphvizWriter {
	OrientGraph graph
	com.couggi.javagraphviz.Graph g

	public ProjectGraphvizWriter(final OrientGraph graph){
		this.graph = graph
		g = new Digraph("G");
		g.attr("resolution").value("300")
		g.attr("fontsize").value("10.0")
		g.attr("fontname").value("Helvetica")
		g.attr("ratio").value("auto")
		g.attr("rankdir").value("LR")
		g.attr("rankType").value("max")
		g.attr("overlap").value("scale")
		//g.attr("splines").value("ortho")
		//g.attr("nodesep").value("1")
		//g.node().attr("shape").value("record");
		//g.attr("splines").value("curved")
		//g.node().attr("shape").value("circle")
		g.node().attr("style").value("filled")
	}

	def goc(v) {
	}

	public void outputGraph(final String filename) throws IOException {
		graph.getVertices().each { vf->
			def idF = vf.getProperty("projectName").toString().replaceAll("\\.", "_")
			def f = g.tryAddNode(idF)
			f.attr("label").value(vf.getProperty("projectName"))
			f.attr("fillcolor").value(Palette.getInstance().getColor(idF))
			
			vf.getEdges(Direction.IN).each {
				Edge e->
				def vt = e.getVertex(Direction.OUT)
				
				def idT = vt.getProperty("projectName").toString().replaceAll("\\.", "_")
				def t = g.tryAddNode(idT)
				
				t.attr("label").value(vt.getProperty("projectName"))
				t.attr("fillcolor").value(Palette.getInstance().getColor(idT))
				
				def ge = g.addEdge(f, t);
				ge.attr("label").value(e.getProperty("description"))
				ge.attr("labeldistance").value("7")
			}
		}
		
//		graph.getEdges().each { e->
//
//			def vf = e.getVertex(Direction.OUT)
//			def id = vf.getProperty("projectName").toString().replaceAll("\\.", "_")
//
//			def f = g.tryAddNode(id)
//			f.attr("label").value(vf.getProperty("projectName"))
//			f.attr("fillcolor").value(Palette.getInstance().getColor(id))
//
//			def vt = e.getVertex(Direction.IN)
//			id = vt.getProperty("projectName").toString().replaceAll("\\.", "_")
//			def t = g.tryAddNode(id)
//			t.attr("label").value(vt.getProperty("projectName"))
//			t.attr("fillcolor").value(Palette.getInstance().getColor(id))
//
//			def ge = g.addEdge(f, t);
//			ge.attr("label").value(e.getProperty("description"))
//			ge.attr("labeldistance").value("7")
//		}

		try {
			GraphvizEngine engine = new GraphvizEngine(g);
			// define the type of the output
			//engine.addType("svg");
			// define the file name of the output.
			engine.toFilePath(filename);
			// generate output.
			engine.output();
		} catch (e) {
			throw e
		}
	}
}
