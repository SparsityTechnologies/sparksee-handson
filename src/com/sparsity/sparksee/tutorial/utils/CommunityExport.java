/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sparsity.sparksee.tutorial.utils;

import com.sparsity.sparksee.gdb.EdgeExport;
import com.sparsity.sparksee.gdb.ExportManager;
import com.sparsity.sparksee.gdb.Graph;
import com.sparsity.sparksee.gdb.GraphExport;
import com.sparsity.sparksee.gdb.NodeExport;
import com.sparsity.sparksee.gdb.NodeShape;
import com.sparsity.sparksee.gdb.Type;
import com.sparsity.sparksee.gdb.Value;
import java.awt.Color;

/**
     * Update this Export implementation to update the resulting
     * visualization.
     *
     * @see ExportManager
     * @see NodeExport
     * @see EdgeExport
     * @see GraphExport
     */
    public class CommunityExport extends ExportManager {

        private Graph g = null;
        private int userType = Type.InvalidType;
        private int nicknameType = Type.InvalidType;
        private int labelType = Type.InvalidType;
        private int knowsType = Type.InvalidType;
        Color colorArray[] = new Color[12];

        @Override
        public void prepare(Graph graph) {
            g = graph;
            userType = g.findType("User");
           
            if( userType != Type.InvalidType ) {
                nicknameType = g.findAttribute(userType, "nickname");
                labelType = g.findAttribute(userType,"label");
            }
                      
            knowsType = g.findType("knows");
            
            colorArray[0] = Color.RED;
            colorArray[1] = Color.BLUE;
            colorArray[2] = Color.CYAN;
            colorArray[3] = Color.GRAY;
            colorArray[4] = Color.ORANGE;
            colorArray[5] = Color.MAGENTA;
            colorArray[6] = Color.PINK;
            colorArray[7] = Color.YELLOW;
            colorArray[8] = Color.decode("#008080");
            colorArray[9] = Color.LIGHT_GRAY;
            colorArray[10] = Color.DARK_GRAY;
            colorArray[11] = Color.GREEN;
            
            
        }

        @Override
        public void release() {
        }

        @Override
        public boolean getGraph(GraphExport ge) {
            ge.setDefaults();
            return true;
        }

        @Override
        public boolean getNodeType(int nodetype, NodeExport ne) {
            return false;
        }

        @Override
        public boolean getEdgeType(int i, EdgeExport ee) {
            return false;
        }

        @Override
        public boolean getNode(long nodeOID, NodeExport ne) {
            ne.setDefaults();
            int nodeType = g.getObjectType(nodeOID);
            if (userType == nodeType ) {
                if(g.getAttribute(nodeOID,labelType).getInteger() == -1) return false;
                ne.setShape(NodeShape.Round);
                Value v = g.getAttribute(nodeOID, labelType);
                ne.setColor(colorArray[v.getInteger() % 12]);
                ne.setLabel(g.getAttribute(nodeOID, nicknameType ).getString()+"["+v.getInteger()+"]");
                return true;
            }
            return false;
        }

        @Override
        public boolean getEdge(long edgeOID, EdgeExport ee) {
            ee.setDefaults();
            Type t = g.getType(g.getObjectType(edgeOID));
            ee.setLabel(" ");
            ee.setWidth(1);
            ee.setAsDirected(false);
            return true;
        }

        @Override
        public boolean enableType(int i) {
            return i == userType || i == knowsType;
        }
          
    }
