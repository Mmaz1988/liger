/*
 * "
 *     Copyright (C) 2021 Mark-Matthias Zymla
 *
 *     This file is part of the abstract syntax annotator  (https://github.com/Mmaz1988/abstract-syntax-annotator-web/blob/master/README.md).
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * "
 */

package de.ukon.liger.webservice.rest.dtos;

import java.util.HashMap;

public class LigerWebNode extends LigerGraphComponent {

    /*
                { // edge ab
                data: {id: '14', source: '1', target: '4', label: "projection",type: "proj"}
            }
     */

    public LigerWebNode(String id, String type) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("node_type", type);

        this.data = data;
    }

    public LigerWebNode(String id, String type, HashMap<String, String> avp)
    {
        HashMap<String,Object> data = new HashMap<>();
        data.put("id",id);
        data.put("node_type",type);
        data.put("avp",avp);

        this.data = data;
    }

    public LigerWebNode(String id, String type, HashMap<String, String> avp, String projection)
    {
        HashMap<String,Object> data = new HashMap<>();
        data.put("id",id);
        data.put("node_type",type);
        data.put("avp",avp);
        data.put("projection",projection);

        this.data = data;
    }

    //Edge
    public LigerWebNode(String id, String source, String target, String label, String type)
    {
        HashMap<String,Object> data = new HashMap<>();
        data.put("id",id);
        data.put("source",source);
        data.put("target",target);
        data.put("label",label);
        data.put("edge_type",type);

        this.data = data;

    }
}
