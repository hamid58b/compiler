/*
 * Copyright 2017, Hridesh Rajan, Ganesha Upadhyaya, Ramanathan Ramu, Robert Dyer, Che Shian Hung
 *                 Bowling Green State University
 *                 and Iowa State University of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package boa.functions;

import java.util.*;

import boa.graphs.cdg.CDG;
import boa.graphs.cfg.CFG;
import boa.graphs.ddg.DDG;
import boa.graphs.pdg.PDG;
import boa.graphs.slicers.CFGSlicer;
import boa.graphs.slicers.PDGSlicer;
import boa.graphs.trees.PDTree;
import boa.types.Ast.Expression;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Method;
import boa.types.Ast.Variable;
import boa.types.Control.CFGNode;

/**
 * Boa functions for working with control flow graphs.
 *
 * @author ganeshau
 * @author rramu
 * @author rdyer
 * @author hungc
 * @author marafat
 */
public class BoaGraphIntrinsics {
	@FunctionSpec(name = "getcfg", returnType = "CFG", formalParameters = { "Method" })
	public static CFG getcfg(final Method method) {
		return new CFG(method).get();
	}

	@FunctionSpec(name = "getpdtree", returnType = "PDTree", formalParameters = { "Method" })
	public static PDTree getpdtree(final Method method) throws Exception {
		return new PDTree(method);
	}

	@FunctionSpec(name = "getcdg", returnType = "CDG", formalParameters = { "Method" })
	public static CDG getcdg(final Method method) throws Exception {
		return new CDG(method);
	}

	@FunctionSpec(name = "getcdg", returnType = "CDG", formalParameters = { "CFG" })
	public static CDG getcdg(final CFG cfg) throws Exception {
		return new CDG(cfg);
	}

	@FunctionSpec(name = "getddg", returnType = "DDG", formalParameters = { "Method" })
	public static DDG getddg(final Method method) throws Exception {
		return new DDG(method);
	}

	@FunctionSpec(name = "getddg", returnType = "DDG", formalParameters = { "CFG" })
	public static DDG getddg(final CFG cfg) throws Exception {
		return new DDG(cfg);
	}

	@FunctionSpec(name = "getpdg", returnType = "PDG", formalParameters = { "Method" })
	public static PDG getpdg(final Method method) throws Exception {
		return new PDG(method);
	}

	@FunctionSpec(name = "getpdg", returnType = "PDG", formalParameters = { "Method", "bool" })
	public static PDG getpdg(final Method method, boolean paramAsStatement) throws Exception {
		return new PDG(method, paramAsStatement);
	}

	@FunctionSpec(name = "getcfgslice", returnType = "CFGSlicer", formalParameters = { "Method", "int" })
	public static CFGSlicer getcfgslice(final Method method, Long id) throws Exception {
		return new CFGSlicer(method, (int)(long) id);
	}

	@FunctionSpec(name = "getpdgslice", returnType = "PDGSlicer", formalParameters = { "PDG",  "int", "bool" })
	public static PDGSlicer getpdgslice(final PDG pdg, Long id, boolean normalize) throws Exception {
		return new PDGSlicer(pdg, (int)(long) id, normalize);
	}

	@FunctionSpec(name = "getpdgslice", returnType = "PDGSlicer", formalParameters = { "Method",  "int", "bool" })
	public static PDGSlicer getpdgslice(final Method method, Long id, boolean normalize) throws Exception {
		return new PDGSlicer(method, (int)(long) id, normalize);
	}

	@FunctionSpec(name = "get_nodes_with_definition", returnType = "set of string", formalParameters = { "CFGNode" })
	public static HashSet<String> getNodesWithDefinition(final CFGNode node) {
		final HashSet<String> vardef = new HashSet<String>();
		if (node.getExpression() != null) {
			if (node.getExpression().getKind() == ExpressionKind.VARDECL || node.getExpression().getKind() == ExpressionKind.ASSIGN) {
				vardef.add(String.valueOf(node.getId()));
			}
		}
		return vardef;
	}

	@FunctionSpec(name = "get_variable_killed", returnType = "set of string", formalParameters = {"CFG", "CFGNode" })
	public static HashSet<String> getVariableKilled(final boa.types.Control.CFG cfg, final CFGNode node) {
		final HashSet<String> varkilled = new HashSet<String>();
		String vardef = "";

		if (node.getExpression() != null) {
			if (node.getExpression().getKind() == ExpressionKind.VARDECL) {
				vardef = node.getExpression().getVariableDeclsList().get(0).getName();
			}
			else if (node.getExpression().getKind() == ExpressionKind.ASSIGN) {
				vardef = node.getExpression().getExpressionsList().get(0).getVariable();
			}
			else {
				return varkilled;
			}

			for (final CFGNode tnode : cfg.getNodesList()) {
				if (tnode.getExpression() != null && tnode.getId() != node.getId()) {
					if (tnode.getExpression().getKind() == ExpressionKind.VARDECL) {
						if (tnode.getExpression().getVariableDeclsList().get(0).getName().equals(vardef)) {
							varkilled.add(String.valueOf(tnode.getId()));
						}
					}
					else if (tnode.getExpression().getKind() == ExpressionKind.ASSIGN) {
						if (tnode.getExpression().getExpressionsList().get(0).getVariable().equals(vardef)) {
							varkilled.add(String.valueOf(tnode.getId()));
						}
					}
				}
			}
		}

		return varkilled;
	}

	@FunctionSpec(name = "get_variable_def", returnType = "set of string", formalParameters = { "CFGNode" })
	public static HashSet<String> getVariableDef(final CFGNode node) {
		final HashSet<String> vardef = new HashSet<String>();
		if (node.getExpression() != null) {
			if (node.getExpression().getKind() == ExpressionKind.VARDECL) {
				vardef.add(node.getExpression().getVariableDeclsList().get(0).getName());
			}
			else if (node.getExpression().getKind() == ExpressionKind.ASSIGN) {
				vardef.add(node.getExpression().getExpressionsList().get(0).getVariable());
			}
		}
		return vardef;
	}

	@FunctionSpec(name = "get_variable_used", returnType = "set of string", formalParameters = { "CFGNode" })
	public static HashSet<String> getVariableUsed(final CFGNode node) {
		final HashSet<String> varused = new HashSet<String>();
		if (node.getExpression() != null) {
			traverseExpr(varused,node.getExpression());
		}
		return varused;
	}

	public static void traverseExpr(final HashSet<String> varused, final Expression expr) {
		if (expr.getVariable() != null) {
			varused.add(expr.getVariable());
		}
		for (final Expression exprs : expr.getExpressionsList()) {
			traverseExpr(varused, exprs);
		}
		for (final Variable vardecls : expr.getVariableDeclsList()) {
			traverseVarDecls(varused, vardecls);
		}
		for (final Expression methodexpr : expr.getMethodArgsList()) {
			traverseExpr(varused, methodexpr);
		}
	}

	public static void traverseVarDecls(final HashSet<String> varused, final Variable vardecls) {
		if (vardecls.getInitializer() != null) {
			traverseExpr(varused, vardecls.getInitializer());
		}
	}

	private static String dotEscape(final String s) {
		final String escaped = s.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\l").replaceAll("\r", "\\\\l");
		if (escaped.indexOf("\\l") != -1 && !escaped.endsWith("\\l"))
			return escaped + "\\l";
		return escaped;
	}

	// Graph Visualizers

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "CFG" })
	public static String cfgToDot(final CFG cfg) {
		return cfgToDot(cfg, "");
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "CFG", "bool" })
	public static String cfgToDot(final CFG cfg, final boolean showMethod) {
		if (showMethod)
			return cfgToDot(cfg, boa.functions.BoaAstIntrinsics.prettyprint(cfg.getMd()));
		return cfgToDot(cfg, "");
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "CFG", "string" })
	public static String cfgToDot(final CFG cfg, final String label) {
		if (cfg == null || cfg.getNodes().size() == 0) return "";
		final StringBuilder str = new StringBuilder();
		str.append("digraph {\n");
		str.append("\t{ rank = sink; " + (cfg.getNodes().size() - 1) + "; }\n");
		if (label.length() > 0) {
			str.append("\tlabelloc=\"t\"\n");
			str.append("\tlabel=\"" + dotEscape(label) + "\"\n");
		}

		final boa.graphs.cfg.CFGNode[] sorted = cfg.sortNodes();
		for (final boa.graphs.cfg.CFGNode n : sorted) {
			final String shape;
			switch (n.getKind()) {
				case CONTROL:
					shape = "shape=diamond";
					break;
				case METHOD:
					shape = "shape=parallelogram";
					break;
				case OTHER:
					shape = "shape=box";
					break;
				case ENTRY:
				default:
					shape = "shape=ellipse";
					break;
			}

			if (n.hasStmt())
				str.append("\t" + n.getId() + "[" + shape + ",label=\"" + "[" + n.getId() + "] " + dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getStmt())) + "\"]\n");
			else if (n.hasExpr())
				str.append("\t" + n.getId() + "[" + shape + ",label=\"" + "[" + n.getId() + "] " + dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getExpr())) + "\"]\n");
			else if (n.getKind() == boa.types.Control.CFGNode.CFGNodeType.ENTRY)
				str.append("\t" + n.getId() + "[" + shape + ",label=\"" + "[" + n.getId() + "] " + n.getName() + "\"]\n");
			else
				str.append("\t" + n.getId() + "[" + shape + "]\n");
		}

		for (final boa.graphs.cfg.CFGNode n : sorted) {
			final java.util.List<boa.graphs.cfg.CFGEdge> edges = new ArrayList<boa.graphs.cfg.CFGEdge>(n.getOutEdges());
			Collections.sort(edges);
			for (final boa.graphs.cfg.CFGEdge e : edges) {
				str.append("\t" + n.getId() + " -> " + e.getDest().getId());
				if (!(e.label() == null || e.label().equals(".") || e.label().equals("")))
					str.append(" [label=\"" + dotEscape(e.label()) + "\"]");
				str.append("\n");
			}
		}

		str.append("}");

		return str.toString();
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "CDG" })
	public static String cdgToDot(final CDG cdg) {
		return cdgToDot(cdg, "");
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "CDG", "string" })
	public static String cdgToDot(final CDG cdg, final String label) {
		if (cdg == null || cdg.getNodes().size() == 0) return "";
		final StringBuilder str = new StringBuilder();
		str.append("digraph {\n");
		if (label.length() > 0) {
			str.append("\tlabelloc=\"t\"\n");
			str.append("\tlabel=\"" + dotEscape(label) + "\"\n");
		}

		for (final boa.graphs.cdg.CDGNode n : cdg.getNodes()) {
			final String shape = "shape=ellipse";

			if (n.hasStmt())
				str.append("\t" + n.getId() + "[" + shape + ",label=\"" + "[" + n.getId() + "] " + dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getStmt())) + "\"]\n");
			else if (n.hasExpr())
				str.append("\t" + n.getId() + "[" + shape + ",label=\"" + "[" + n.getId() + "] " + dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getExpr())) + "\"]\n");
			else if (n.getKind() == boa.types.Control.CDGNode.CDGNodeType.ENTRY)
				str.append("\t" + n.getId() + "[" + shape + ",label=\"" + "[" + n.getId() + "] " + "ENTRY" + "\"]\n");
			else
				str.append("\t" + n.getId() + "[" + shape + "]\n");
		}

		final boa.runtime.BoaAbstractTraversal printGraph = new boa.runtime.BoaAbstractTraversal<Object>(false, false) {
			protected Object preTraverse(final boa.graphs.cdg.CDGNode node) throws Exception {
				final java.util.Set<boa.graphs.cdg.CDGEdge> edges = node.getOutEdges();
				for (final boa.graphs.cdg.CDGEdge e : node.getOutEdges()) {
					str.append("\t" + node.getId() + " -> " + e.getDest().getId());
					if (!(e.getLabel() == null || e.getLabel().equals(".") || e.getLabel().equals("")))
						str.append(" [label=\"" + dotEscape(e.getLabel()) + "\"]");
					str.append("\n");
				}
				return null;
			}

			@Override
			public void traverse(final boa.graphs.cdg.CDGNode node, boolean flag) throws Exception {
				if (flag) {
					currentResult = preTraverse(node);
					outputMapObj.put(node.getId(), currentResult);
				} else {
					outputMapObj.put(node.getId(), preTraverse(node));
				}
			}
		};

		try {
			printGraph.traverse(cdg, boa.types.Graph.Traversal.TraversalDirection.FORWARD, boa.types.Graph.Traversal.TraversalKind.DFS);
		} catch (final Exception e) {
			// do nothing
		}

		str.append("}");

		return str.toString();
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "DDG" })
	public static String cdgToDot(final DDG ddg) {
		return ddgToDot(ddg, "");
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "DDG", "string" })
	public static String ddgToDot(final DDG ddg, final String label) {
		if (ddg == null || ddg.getNodes().size() == 0) return "";
		final StringBuilder str = new StringBuilder();
		str.append("digraph {\n");
		if (label.length() > 0) {
			str.append("\tlabelloc=\"t\"\n");
			str.append("\tlabel=\"" + dotEscape(label) + "\"\n");
		}

		for (final boa.graphs.ddg.DDGNode n : ddg.getNodes()) {
			final String shape = "shape=ellipse";

			if (n.hasStmt())
				str.append("\t" + n.getId() + "[" + shape + ",label=\"" + "[" + n.getId() + "] " + dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getStmt())) + "\"]\n");
			else if (n.hasExpr())
				str.append("\t" + n.getId() + "[" + shape + ",label=\"" + "[" + n.getId() + "] " + dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getExpr())) + "\"]\n");
			else if (n.getKind() == boa.types.Control.DDGNode.DDGNodeType.ENTRY)
					str.append("\t" + n.getId() + "[" + shape + ",label=\"" + "[" + n.getId() + "] " + "ENTRY" + "\"]\n");
			else
				str.append("\t" + n.getId() + "[" + shape + "]\n");
		}

		final boa.runtime.BoaAbstractTraversal printGraph = new boa.runtime.BoaAbstractTraversal<Object>(false, false) {
			protected Object preTraverse(final boa.graphs.ddg.DDGNode node) throws Exception {
				final java.util.Set<boa.graphs.ddg.DDGEdge> edges = node.getOutEdges();
				for (final boa.graphs.ddg.DDGEdge e : node.getOutEdges()) {
					str.append("\t" + node.getId() + " -> " + e.getDest().getId());
					if (!(e.getLabel() == null || e.getLabel().equals(".") || e.getLabel().equals("")))
						str.append(" [label=\"" + dotEscape(e.getLabel()) + "\"]");
					str.append("\n");
				}
				return null;
			}

			@Override
			public void traverse(final boa.graphs.ddg.DDGNode node, boolean flag) throws Exception {
				if (flag) {
					currentResult = preTraverse(node);
					outputMapObj.put(node.getId(), currentResult);
				} else {
					outputMapObj.put(node.getId(), preTraverse(node));
				}
			}
		};

		try {
			printGraph.traverse(ddg, boa.types.Graph.Traversal.TraversalDirection.FORWARD, boa.types.Graph.Traversal.TraversalKind.DFS);
		} catch (final Exception e) {
			// do nothing
		}

		str.append("}");

		return str.toString();
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "PDG" })
	public static String pdgToDot(final PDG pdg) {
		return pdgToDot(pdg, "");
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "PDG", "string" })
	public static String pdgToDot(final PDG pdg, final String label) {
		if (pdg == null || pdg.getNodes().size() == 0) return "";
		final StringBuilder str = new StringBuilder();
		str.append("digraph {\n");
		if (label.length() > 0) {
			str.append("\tlabelloc=\"t\"\n");
			str.append("\tlabel=\"" + dotEscape(label) + "\"\n");
		}

		for (final boa.graphs.pdg.PDGNode n : pdg.getNodes()) {
			final String shape = "shape=ellipse";

			if (n.hasStmt())
				str.append("\t" + n.getId() + "[" + shape + ",label=\"" + "[" + n.getId() + "] " + dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getStmt())) + "\"]\n");
			else if (n.hasExpr())
				str.append("\t" + n.getId() + "[" + shape + ",label=\"" + "[" + n.getId() + "] " + dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getExpr())) + "\"]\n");
			else if (n.getKind() == boa.types.Control.PDGNode.PDGNodeType.ENTRY)
				str.append("\t" + n.getId() + "[" + shape + ",label=\"" + "[" + n.getId() + "] " + "ENTRY" + "\"]\n");
			else
				str.append("\t" + n.getId() + "[" + shape + "]\n");
		}

		final boa.runtime.BoaAbstractTraversal printGraph = new boa.runtime.BoaAbstractTraversal<Object>(false, false) {
			protected Object preTraverse(final boa.graphs.pdg.PDGNode node) throws Exception {
				final java.util.Set<boa.graphs.pdg.PDGEdge> edges = node.getOutEdges();
				for (final boa.graphs.pdg.PDGEdge e : node.getOutEdges()) {
					str.append("\t" + node.getId() + " -> " + e.getDest().getId());
					if (!(e.getLabel() == null || e.getLabel().equals(".") || e.getLabel().equals("")))
						str.append(" [label=\"" + dotEscape(e.getKind() + ":" + e.getLabel()) + "\"]");
					str.append("\n");
				}
				return null;
			}

			@Override
			public void traverse(final boa.graphs.pdg.PDGNode node, boolean flag) throws Exception {
				if (flag) {
					currentResult = preTraverse(node);
					outputMapObj.put(node.getId(), currentResult);
				} else {
					outputMapObj.put(node.getId(), preTraverse(node));
				}
			}
		};

		try {
			printGraph.traverse(pdg, boa.types.Graph.Traversal.TraversalDirection.FORWARD, boa.types.Graph.Traversal.TraversalKind.DFS);
		} catch (final Exception e) {
			// do nothing
		}

		str.append("}");

		return str.toString();
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "PDGSlicer" })
	public static String pdgslicerToDot(final PDGSlicer pdgslicer) {
		return pdgslicerToDot(pdgslicer, "");
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "PDGSlicer", "string" })
	public static String pdgslicerToDot(final PDGSlicer pdgslicer, final String label) {
		if (pdgslicer == null || pdgslicer.getSlice().size() == 0) return "";
		final StringBuilder str = new StringBuilder();
		str.append("digraph {\n");
		if (label.length() > 0) {
			str.append("\tlabelloc=\"t\"\n");
			str.append("\tlabel=\"" + dotEscape(label) + "\"\n");
		}

		for (final boa.graphs.pdg.PDGNode n : pdgslicer.getSlice()) {
			final String shape = "shape=ellipse";

			if (n.hasStmt())
				str.append("\t" + n.getId() + "[" + shape + ",label=\"" + "[" + n.getId() + "] " + dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getStmt())) + "\"]\n");
			else if (n.hasExpr())
				str.append("\t" + n.getId() + "[" + shape + ",label=\"" + "[" + n.getId() + "] " + dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getExpr())) + "\"]\n");
			else if (n.getKind() == boa.types.Control.PDGNode.PDGNodeType.ENTRY)
				str.append("\t" + n.getId() + "[" + shape + ",label=\"" + "[" + n.getId() + "] " + "ENTRY" + "\"]\n");
			else
				str.append("\t" + n.getId() + "[" + shape + "]\n");
		}

		final boa.runtime.BoaAbstractTraversal printGraph = new boa.runtime.BoaAbstractTraversal<Object>(false, false) {
			protected Object preTraverse(final boa.graphs.pdg.PDGNode node) throws Exception {
				final java.util.Set<boa.graphs.pdg.PDGEdge> edges = node.getOutEdges();
				for (final boa.graphs.pdg.PDGEdge e : node.getOutEdges()) {
					str.append("\t" + node.getId() + " -> " + e.getDest().getId());
					if (!(e.getLabel() == null || e.getLabel().equals(".") || e.getLabel().equals("")))
						str.append(" [label=\"" + dotEscape(e.getKind() + ":" + e.getLabel()) + "\"]");
					str.append("\n");
				}
				return null;
			}

			@Override
			public void traverse(final boa.graphs.pdg.PDGNode node, boolean flag) throws Exception {
				if (flag) {
					currentResult = preTraverse(node);
					outputMapObj.put(node.getId(), currentResult);
				} else {
					outputMapObj.put(node.getId(), preTraverse(node));
				}
			}
		};

		try {
			printGraph.traverse(pdgslicer, boa.types.Graph.Traversal.TraversalDirection.FORWARD, boa.types.Graph.Traversal.TraversalKind.DFS);
		} catch (final Exception e) {
			// do nothing
		}

		str.append("}");

		return str.toString();
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "PDTree" })
	public static String pdtreeToDot(final PDTree pdtree) {
		return pdtreeToDot(pdtree, "");
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "PDTree", "string" })
	public static String pdtreeToDot(final PDTree pdtree, final String label) {
		if (pdtree == null || pdtree.getNodes().size() == 0) return "";
		final StringBuilder str = new StringBuilder();
		str.append("digraph {\n");
		if (label.length() > 0) {
			str.append("\tlabelloc=\"t\"\n");
			str.append("\tlabel=\"" + dotEscape(label) + "\"\n");
		}

		for (final boa.graphs.trees.TreeNode n : pdtree.getNodes()) {
			final String shape = "shape=ellipse";

			if (n.hasStmt())
				str.append("\t" + n.getId() + "[" + shape + ",label=\"" + "[" + n.getId() + "] " + dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getStmt())) + "\"]\n");
			else if (n.hasExpr())
				str.append("\t" + n.getId() + "[" + shape + ",label=\"" + "[" + n.getId() + "] " + dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getExpr())) + "\"]\n");
			else if (n.getId() == 0)
				str.append("\t" + n.getId() + "[" + shape + ",label=\"" + "[" + n.getId() + "] " + "START" + "\"]\n");
			else if (n.getId() == pdtree.getNodes().size()-1)
				str.append("\t" + n.getId() + "[" + shape + ",label=\"" + "[" + n.getId() + "] " + "STOP" + "\"]\n");
			else if (n.getId() == pdtree.getNodes().size())
				str.append("\t" + n.getId() + "[" + shape + ",label=\"" + "[" + n.getId() + "] " + "ENTRY" + "\"]\n");
			else
				str.append("\t" + n.getId() + "[" + shape + "]\n");
		}

		final boa.runtime.BoaAbstractTraversal printGraph = new boa.runtime.BoaAbstractTraversal<Object>(false, false) {
			protected Object preTraverse(final boa.graphs.trees.TreeNode node) throws Exception {
				for (final boa.graphs.trees.TreeNode n : node.getChildren()) {
					str.append("\t" + node.getId() + " -> " + n.getId());
					str.append("\n");
				}
				return null;
			}

			@Override
			public void traverse(final boa.graphs.trees.TreeNode node, boolean flag) throws Exception {
				if (flag) {
					currentResult = preTraverse(node);
					outputMapObj.put(node.getId(), currentResult);
				} else {
					outputMapObj.put(node.getId(), preTraverse(node));
				}
			}
		};

		try {
			printGraph.traverse(pdtree, boa.types.Graph.Traversal.TraversalDirection.FORWARD, boa.types.Graph.Traversal.TraversalKind.DFS);
		} catch (final Exception e) {
			// do nothing
		}

		str.append("}");

		return str.toString();
	}
}
