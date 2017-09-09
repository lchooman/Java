import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    A Java Enumeration implementation to return permutations of characters in any string (at least one-character long), without any duplication caused by repeated characters.
    Copyright (C) 2017 Lewis Tat Fong Choo Man

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see http://www.gnu.org/licenses/gpl.html.
 */
class Node {
	private int id;

	private String name;

	private List<Node> childrenNodeList = new ArrayList<>();

	private Node parentNode;

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Node> getChildrenNodeList() {
		return this.childrenNodeList;
	}

	public void setChildrenNodeList(List<Node> childrenNodeList) {
		this.childrenNodeList = childrenNodeList;
	}

	public Node getParentNode() {
		return this.parentNode;
	}

	public void setParentNode(Node parentNode) {
		this.parentNode = parentNode;
	}
}

class ParentNode extends Node {
}

public class TraverseNodesOptimized {
	private static Map<String, Map<String, String []>> HIERARCHICAL_DATA = new HashMap<String, Map<String, String []>>();

	private List<Node> nodeList;

	static {
		Map<String, String[]> dataLevel2And3 = new HashMap<String, String []>();

		dataLevel2And3.put("IT Manager", new String [] { "Front-end developer", "QA assistant", "Back-end developer" });

		dataLevel2And3.put("Finance Manager", new String [] { "Accountant", "Underwriter", "Salesperson" });

		HIERARCHICAL_DATA.put("CEO", dataLevel2And3);

		dataLevel2And3 = new HashMap<String, String []>();

		dataLevel2And3.put("Customer Service Manager", new String [] { "Customer Service Rep", "Litigator" });

		dataLevel2And3.put("IT Operations Manager", new String [] { "System Administrator", "Database Administrator" });

		HIERARCHICAL_DATA.put("Director", dataLevel2And3);
	}

	private List<Node> getNodes() {

		List<Node> nodeList = new ArrayList<>();
		Map<String, String[]> map;
		Node node1, node2, node3;
		int id = 0;

		for (String key1 : HIERARCHICAL_DATA.keySet()) {
			node1 = new Node();
			id++;
			node1.setId(id);
			node1.setName(key1);
			node1.setParentNode(null);
			nodeList.add(node1);

			map = HIERARCHICAL_DATA.get(key1);
			for (String key2 : map.keySet()) {
				node2 = new Node();
				id++;
				node2.setId(id);
				node2.setName(key2);
				node2.setParentNode(node1);
				node1.getChildrenNodeList().add(node2);

				for (String value : map.get(key2)) {
					node3 = new Node();
					id++;
					node3.setId(id);
					node3.setName(value);
					node3.setParentNode(node2);
					node2.getChildrenNodeList().add(node3);
				}
			}
		}
		return nodeList;
	}

	public List<Node> getNodeList() {
		return this.nodeList;
	}

	public TraverseNodesOptimized() {
		this.nodeList = this.getNodes();
	}

	private void printIndentation(int level) {
		if (level > 0) {
			System.out.printf("%1$" + (level * 4) + "s", " ");
		}
	}

	public void searchNodes(List<Node> searchNodeList, List<Node> resultList, Matcher matcher) {
		for (Node node : searchNodeList) {
			matcher.reset(node.getName());
			if (matcher.find()) {
				resultList.add(node);
			}
			searchNodes(node.getChildrenNodeList(), resultList, matcher);
		}
	}

	public List<Node> gotoRoot(List<Node> nodeList) {
		List<Node> rootNodeList = new ArrayList<>();
		Node parentNode, newNode;

		for (Node node : nodeList) {
			parentNode = node;
			while (parentNode.getParentNode() != null) {
				newNode = new ParentNode();
				newNode.setId(parentNode.getParentNode().getId());
				newNode.setName(parentNode.getParentNode().getName());
				newNode.getChildrenNodeList().add(parentNode);
				newNode.setParentNode(parentNode.getParentNode().getParentNode());
				parentNode.setParentNode(newNode);
				parentNode = newNode;
			}
			rootNodeList.add(parentNode);
		}
		return rootNodeList;
	}

	private void printNodes(List<Node> nodeList, int level) {
		for (Node node : nodeList) {
			printIndentation(level);
			System.out.printf("%1$s (%2$d)\n", node.getName(), node.getId());
			printNodes(node.getChildrenNodeList(), level + 1);
		}
	}

	public void printNodes(List<Node> nodeList) {
		printNodes(nodeList, 0);
	}

	public void printNodes() {
		printNodes(this.nodeList, 0);
	}

	private void addToNodeBufferMap(Node node, Map<Integer, Node> nodeBufferMap) {
		nodeBufferMap.put(node.getId(), node);
		for (Node childNode : node.getChildrenNodeList()) {
			addToNodeBufferMap(childNode, nodeBufferMap);
		}
	}

	public List<Node> mergeBranchesFromRoot(List<Node> rootNodeList) {
		List<Node> mergedRootNodeList = new ArrayList<>(), childrenNodeList;
		Map<Integer, Node> nodeBufferMap = new HashMap<>();
		Node node, mergedParentNode;

		for (int i = 0; i < rootNodeList.size(); i++) {
			node = rootNodeList.get(i);
			mergedParentNode = null;
			while (node instanceof ParentNode && nodeBufferMap.containsKey(node.getId())) {
				mergedParentNode = nodeBufferMap.get(node.getId());
				childrenNodeList = node.getChildrenNodeList();
				node = childrenNodeList.size() > 0 ? childrenNodeList.get(0) : null;
			}

			if (node != null && !nodeBufferMap.containsKey(node.getId())) {
				if (mergedParentNode == null) {
					mergedRootNodeList.add(node);
				} else {
					mergedParentNode.getChildrenNodeList().add(node);
				}
				addToNodeBufferMap(node, nodeBufferMap);
			}
		};
		return mergedRootNodeList;
	}

	public static void main (String [] args) {

		if (args.length < 1) {
			System.out.printf("Usage: java %1$s <search pattern>\n", Thread.currentThread().getStackTrace()[1].getClassName());
			System.exit(1);
		}

		TraverseNodesOptimized traverseNodes = new TraverseNodesOptimized();
		System.out.println("Full Tree");
		System.out.println("=========");
		traverseNodes.printNodes(traverseNodes.getNodeList());

		List<Node> nodeList = new ArrayList<>();
		traverseNodes.searchNodes(traverseNodes.getNodeList(), nodeList, Pattern.compile(args[0], Pattern.CASE_INSENSITIVE).matcher(""));

		List<Node> rootNodeList = traverseNodes.gotoRoot(nodeList);
		System.out.println("Found Branches");
		System.out.println("==============");
		traverseNodes.printNodes(rootNodeList);

		List<Node> mergedRootNodeList = traverseNodes.mergeBranchesFromRoot(rootNodeList);
		System.out.println("Merged Found Branches");
		System.out.println("=====================");
		traverseNodes.printNodes(mergedRootNodeList);
	}
}
