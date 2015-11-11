/*
 * Copyright (c) 2014, Colorado State University All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * 
 * This software is provided by the copyright holders and contributors "as is"
 * and any express or implied warranties, including, but not limited to, the
 * implied warranties of merchantability and fitness for a particular purpose
 * are disclaimed. In no event shall the copyright holder or contributors be
 * liable for any direct, indirect, incidental, special, exemplary, or
 * consequential damages (including, but not limited to, procurement of
 * substitute goods or services; loss of use, data, or profits; or business
 * interruption) however caused and on any theory of liability, whether in
 * contract, strict liability, or tort (including negligence or otherwise)
 * arising in any way out of the use of this software, even if advised of the
 * possibility of such damage.
 */

package mendel.network;

import java.util.ArrayList;
import java.util.List;

/**
 * Maintains information for all nodes within the Mendel cluster.
 * @author ctolooee
 *
 */
public class NetworkInfo {

    private List<GroupInfo> groups = new ArrayList<>();

    public void addGroup(GroupInfo group) {
        groups.add(group);
    }

    public List<GroupInfo> getGroups() {
        return groups;
    }

    public List<NodeInfo> getAllNodes() {
        List<NodeInfo> nodeList = new ArrayList<>();
        for (GroupInfo group : groups) {
            List<NodeInfo> groupNodes = group.getAllNodes();
            nodeList.addAll(groupNodes);
        }

        return nodeList;
    }

    @Override
    public String toString() {
        String str = "Network Information:" + System.lineSeparator();
        for (GroupInfo group : groups) {
            str += group;
        }

        return str;
    }
}
