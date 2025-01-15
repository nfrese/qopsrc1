/* 
 * Copyright (C) 2018 Norbert Frese
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
*/

package at.qop.ws;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QOPRestApiIndex extends QOPRestApiBase {
       
    public QOPRestApiIndex() {
        super();
    }
    
    @GetMapping("/qop")
    protected String info()
    {
    	StringBuilder html = new StringBuilder();
    	html.append("<h1>QOP Service</h1>");
    	html.append("<p><a href=\"ui\">Demo User Interface</a></p>");
    	html.append("<p><a href=\"ui/admin\">Administration</a></p>");
    	return html.toString();
    }

}
