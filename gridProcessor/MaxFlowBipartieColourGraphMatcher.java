package uk.ac.aber.cs31920.assignment.gridProcessor;

import java.util.ArrayList;
import java.util.Optional;
import java.util.PriorityQueue;

public class MaxFlowBipartieColourGraphMatcher {

    /**
     * Class which reads the planar graph from GraphColouringComponent as biparty graph for a simple maximum matching
     * algorithm to find all tile choices.
     *
     * We first choose all GraphEdges that are connected to a Connection Node with only one edge as we know they are part of the solution
     * and then we use dfs and the edge colours to make as many choices as possible.
     * For all remaining unmatched 0s we undo a related bad edge choice (Ford-Fulkerson) and then we repeat the modified dfs.
     */
    ConnectionNode sink = new ConnectionNode(-1, -1);
    ConnectionNode source = new ConnectionNode(-1, -1);
    PriorityQueue<ConnectionNode> allRemainingNodes;
    ArrayList<GraphEdge> preProcessed;
    PriorityQueue<ConnectionNode> colour1;
    PriorityQueue<ConnectionNode> colour2;
    ArrayList<GraphEdge> choices;

    public ArrayList<GraphEdge> getChoices(){
        return choices;
    }

    public MaxFlowBipartieColourGraphMatcher(ArrayList<GraphEdge> alreadyChosen, PriorityQueue<ConnectionNode> networkZeroes){
        this.choices = new ArrayList<>(networkZeroes.size()/2);
        //choices.addAll(alreadyChosen);
        this.preProcessed = new ArrayList<>(networkZeroes.size());
        this.allRemainingNodes = new PriorityQueue<>(networkZeroes.size(), new NodeConnectionComparator());
        this.allRemainingNodes.addAll(networkZeroes);

        //remove already coupled zeroes and all edges involving them
        for(GraphEdge g : alreadyChosen){
            removeChoicesAndNodeFromQueues(g);
        }

        //repeat this until only zeroes with at least two edges remain
        while(!allRemainingNodes.isEmpty() && allRemainingNodes.peek().edges.size() == 1){
            //all edges connected to any node that has the edge as singular choice must be part of the solution
            ArrayList<GraphEdge> choosing = chooseByNumOfChoices(allRemainingNodes);
            for(GraphEdge g: choosing){
                removeChoicesAndNodeFromQueues(g);
            }
            this.choices.addAll(choosing);
            if(!allRemainingNodes.isEmpty()){
                PriorityQueue<ConnectionNode> allNodeCopies = new PriorityQueue<>(allRemainingNodes.size(), new NodeConnectionComparator());
                allNodeCopies.addAll(allRemainingNodes);
                allRemainingNodes.clear();
                allRemainingNodes.addAll(allNodeCopies);
            } else {
                break;
            }
        }
        if(!allRemainingNodes.isEmpty()){
            this.colour1 = new PriorityQueue<>((networkZeroes.size()/2), new NodeFlowComparator());
            this.colour2 = new PriorityQueue<>((networkZeroes.size()/2), new NodeFlowComparator());

            //initialise source & sink
            this.source.flow = 0;
            this.source.seen = true;
            this.source.isSourceOrSink = true;
            this.sink.isSourceOrSink = true;

            createIntialNetwork();
            flowNodeToNode();
        }
    }

    private void createIntialNetwork(){
        // because all edge weights should technically be one we do not actually need to build a proper source and sink
        // we still initialise the "source" just to keep track of the flow
        // but we have all the information needed to execute out maximum matching algorithm in the existing data structures already
        for(ConnectionNode n : this.allRemainingNodes){
            if(n.colour == 1){
                this.colour2.add(n);
                //n.addConnection(sink);
            } else {
                this.colour1.add(n);
               // source.addConnection(n);
            }
        }
        source.isSourceOrSink = true;
        //sink.isSourceOrSink = true;

        //sanity check
        if(colour1.size() != colour2.size()){
            System.out.println("Sth went wrong.");
            System.out.println("No Solution");
            System.exit(1);
        }
    }

    /**
     * Used to remove edges that are still reffered to by other zeroes to zeroes we have already partnered up in preprocessing
     * @param choice graph-edge that was chosen in preprocessing due to one of it's nodes only having one edge choice
     */
    private void removeChoicesAndNodeFromQueues(GraphEdge choice){
        ConnectionNode first = choice.first;
        ConnectionNode second = choice.second;
        first.flow = 1;
        second.flow = 1;
        first.priority--;
        second.priority--;
        //remove nodes from biparty matching graph
        this.allRemainingNodes.remove(first);
        this.allRemainingNodes.remove(second);
        GraphEdge[] impliedImpossibleEdges = new GraphEdge[3];
        int index = 0;

        //find all graph edges that are not chosen (at most 3 since one of the nodes will only have 1 which is choice)
        for(GraphEdge g: first.edges){
            if(g!= choice){
                impliedImpossibleEdges[index]= g;
                g.flow = 4;
                index++;
            }
        }
        for(GraphEdge g: second.edges){
            if(g!= choice){
                impliedImpossibleEdges[index]= g;
                g.flow = 4;
                index++;
            }
        }
        //remove these edges from the nodes they imply
        for(GraphEdge impossibleEdge : impliedImpossibleEdges){
            if(impossibleEdge != null){
                impossibleEdge.first.edges.remove(impossibleEdge);
                impossibleEdge.second.edges.remove(impossibleEdge);
                impossibleEdge.first.priority--;
                impossibleEdge.second.priority--;
            }
        }
    }

    /**
     * For all connection nodes that only have 1 edge, we know that edge is part of a valid solution if one exists,
     * so we choose those.
     * This function is called multiple times, by removing the before mentioned edges we create new nodes that have onlu one
     * valid edge in certain grids.
     * @param allNodes - all nodes of edges that have not yet been chosen
     * @return newly chosen edges, we then use to remove further implied nodes and edges
     * Is repeated possibly O(n(log(n)) times and can solve specific types of grids (see input02.txt in testInputs)
     */
    private ArrayList<GraphEdge> chooseByNumOfChoices(PriorityQueue<ConnectionNode> allNodes){
        ArrayList<GraphEdge> choicesInternal = new ArrayList<GraphEdge>(allNodes.size());
        if(allNodes.size() > 2){
            for(ConnectionNode z : allNodes){
                //since we sort nodes by number of edges we can early-out once we find a node with 2 edges
                if(z.edges.size() == 2){
                    return choicesInternal;
                }
                //we know all Connection nodes with just one edge choice must be part of the solution
                if(z.edges.size() == 1){
                    GraphEdge firstChoice = z.edges.peek();
                    choicesInternal.add(firstChoice);
                }
            }
        } else {
            //if there are only two nodes left they have to have a common edge
            if(allNodes.size() == 2){
                ConnectionNode z = allNodes.peek();
                if(checkEdgeSelectable(z.edges.peek())){
                    choicesInternal.add(z.edges.peek());
                } else {
                    System.out.println("Sth went wrong with the preprocessing - breakpoint chooseByNumOfChoices() to debug");
                }
            }
        }
        return choicesInternal;
    }

    private boolean checkEdgeSelectable(GraphEdge g){
        if(g == null) return false;
        if(g.first.flow == 0 && g.second.flow ==0 && g.flow == 0)
            return true;

        return false;
    }

    private GraphEdge findEdgeWithPreviousColour(ConnectionNode node, int colour){
        if(colour == 1){
            if(!node.colour1.isEmpty() && checkEdgeSelectable(node.colour1.get())){ return node.colour1.get(); }
        } else if( colour == 2){
            if(!node.colour2.isEmpty() && checkEdgeSelectable(node.colour2.get())){ return node.colour2.get(); }
        } else if( colour == 10){
            if(!node.colour10.isEmpty() && checkEdgeSelectable(node.colour10.get())){ return node.colour10.get(); }
        } else if( colour == 20){
            if(!node.colour20.isEmpty() && checkEdgeSelectable(node.colour20.get())){ return node.colour20.get(); }
        }
        return null;
    }

    /**
     * We traverse the graph by making a limited number of edge choices through dfs, but only choose same coloured edges or
     * edges which are on a 90 degree angle if the same colour does not exist.
     *
     * Edges are selctable only if their flow is 0
     * ConnectionsNodes with flow 0 are prioritised
     * @param current node we will perform dfs on
     * @return true if we want to break out of the while loop
     */
    private boolean dfsEdgeSelection(ConnectionNode current){
        if(current.flow != 0 && source.flow != allRemainingNodes.size()){
            return true;
        } else {
            boolean hasValidChoice = false;
            for(GraphEdge g : current.edges){
                if(g.flow == 0){
//                    if(g.flow > 2){
//                        //select edge regardless in this part
//                        g.flow = 0;
//                    }
                    // Comment on implementation: As stated in the class description, all edges SHOULD have a flow of 1
                    // However, during development a parallel solution attempted required edges to have a flow of 2 :(
                    // As we are not traversing the graph in a traditional way for Ford-Folkerson, but rather simulating the
                    // steps through our data structures, this in essence does not make a difference.
                    // TLDR: For ConnectionNodes flow has to be one, for edges it has to be 2 to be valid. Practically this makes
                    // no difference in this particular code.
                    boolean firstIsCurrent = g.first == current;
                    if(checkEdgeSelectable(g)){
                        int previousEdgeColour = g.id;
                        g.flow += 2;
                        g.first.flow++;
                        g.second.flow++;
                        source.flow += 2;
                        if(source.flow == allRemainingNodes.size()) return false;
                        if(firstIsCurrent){
                            for(ConnectionNode checkAdjecent : g.second.implyingNodes){
                                dfsEdgeSelection(checkAdjecent, previousEdgeColour);
                            }
                        } else {
                            for(ConnectionNode checkAdjecent : g.first.implyingNodes){
                                dfsEdgeSelection(checkAdjecent, previousEdgeColour);
                            }
                        }
                        hasValidChoice = true;
                        break;
                    }
                }
            }
            return hasValidChoice;
        }
    }

    /**
     * This is the internal version of the function tha is actually making the choice for the edge selection based of the
     * "colour". Colours are determined by GraphEdge.id and can have the value 1 or 2 and 10 or 20, based on if they are horizontal
     * or vertical.
     * @see MatrixArrayConverter createGraphData()
     * @param current node in recursive call being looked at
     * @param previousEdgeColour so we can make a "smart" choice
     */
    private void dfsEdgeSelection(ConnectionNode current, int previousEdgeColour){
        if(current.flow !=0 && source.flow != allRemainingNodes.size()){
            return;
        } else {
            GraphEdge sameColour = findEdgeWithPreviousColour(current, previousEdgeColour);
            if(sameColour != null){
                sameColour.flow+=2;
                sameColour.first.flow++;
                sameColour.second.flow++;
                source.flow+=2;
                if(source.flow == allRemainingNodes.size()) return;
                for(ConnectionNode in : sameColour.getOther(current).implyingNodes){
                    if(in.flow == 0){
                        dfsEdgeSelection(in, previousEdgeColour);
                        return;
                    }
                }
            } else {
                //make 90 turn
                if(previousEdgeColour >=10){
                    //previous edge colour was 10 or 20, so we want 1 or 2
                    for(GraphEdge g : current.edges){
                        if(g.id < 10 && checkEdgeSelectable(g)){
                            g.flow += 2;
                            g.first.flow++;
                            g.second.flow++;
                            source.flow += 2;
                            if(source.flow == allRemainingNodes.size()) return;
                            for(ConnectionNode n : g.getOther(current).implyingNodes){
                                if(n.flow == 0){
                                    dfsEdgeSelection(n, g.id);
                                    return;
                                }
                            }
                        }
                    }
                } else {
                    //previous edge colour was 1 or 2, so we want 10 or 20
                    for(GraphEdge g : current.edges){
                        if(g.id >= 10 && checkEdgeSelectable(g)){
                            g.flow += 2;
                            g.first.flow++;
                            g.second.flow++;
                            source.flow += 2;
                            if(source.flow == allRemainingNodes.size()) return;
                            for(ConnectionNode n : g.getOther(current).implyingNodes){
                                if(n.flow == 0){
                                    dfsEdgeSelection(n, g.id);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This function simulates a flow network through the use of priority queues prioritising flow
     * If flow is 0 for a ConnectionNode we try to find a valid solution first, else we swap edges to
     * get a better attempt.
     *
     * Instead of computing the residual network we determine the residual network is simply the unused edges and
     * all selected edges are "back" edges. We can do this because we know the flow on all edges should be 1.
     * (In essence in this code it is 2, please see comment inside dfsEdgeSelection() or report)
     */
    private void flowNodeToNode() {
        if (!colour1.isEmpty() && !colour2.isEmpty()) {
            while (colour1.peek().flow != 1 && source.flow != allRemainingNodes.size()) {
                /**
                 * If an input seemingly is hung breakpoint on the line below, then around line 350
                 */
                while (colour1.peek().flow != 1) {
                    PriorityQueue<ConnectionNode> colour1Copy = new PriorityQueue<>(new NodeFlowComparator());
                    colour1Copy.addAll(colour1);
                    boolean noValidChoice = false;
                    for (ConnectionNode n : colour1Copy) {
                        if (n.flow == 0 && !dfsEdgeSelection(n)) {
                            noValidChoice = true;
                        } else if (n.flow > 0) {
                            break;
                        }
                    }
                    colour1.clear();
                    colour1.addAll(colour1Copy);
                    if (noValidChoice) break;
                }
                if (colour1.peek().flow != 1) {
                    ConnectionNode n = colour1.poll();
                    if (n.flow > 0) break;
                    /**
                     * Breakpoint on line below to see what's going wrong in the data
                     */
                    if (n.flow == 0) {
                        if (n.priority <= 0) {
                            for (GraphEdge p : n.edges) {
                                p.flow = 0;
                            }
                            n.priority = n.edges.size();
                        }
                        for (GraphEdge p : n.edges) {
                            if (p.flow == 0) {
                                if (p.getOther(n).flow == 1 && p.getOther(n).edges.size() >= 2) {
                                    //swap edges
                                    //1) find other edge
                                    ConnectionNode other = p.getOther(n);
                                    boolean foundEdge = false;
                                    for (GraphEdge q : other.edges) {
                                        if (q.flow == 2) {
                                            q.flow += 2; // make unselectable by increasing to 4
                                            q.first.flow--;
                                            q.second.flow--;
                                            q.first.priority--;
                                            q.second.priority--;
                                            p.flow += 2;
                                            p.first.flow++;
                                            p.second.flow++;
                                            foundEdge = true;
                                            break;
                                        }
                                    }
                                    if (foundEdge) break;
                                } else if (p.getOther(n).flow == 0) {
                                    p.flow += 2;
                                    p.first.flow++;
                                    p.second.flow++;
                                    source.flow += 2;
                                    break;
                                }
                            }
                        }
                    }
                    colour1.add(n);
                }
            }
        }
        for (ConnectionNode n : this.colour1) {
            for (GraphEdge g : n.edges) {
                if (g.flow == 2) {
                    choices.add(g);
                }
            }
        }
        System.out.println();
    }

}
