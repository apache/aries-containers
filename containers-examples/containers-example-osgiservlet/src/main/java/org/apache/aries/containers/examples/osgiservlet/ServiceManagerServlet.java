/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.aries.containers.examples.osgiservlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.aries.containers.ServiceConfig;
import org.apache.aries.containers.ServiceConfig.Builder;
import org.apache.aries.containers.ServiceManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

@Component(service = Servlet.class,
    property = {HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN + "=/manager",
            HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT + "=(" +
                    HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME + "=" + MyServletContext.NAME + ")"
    })
public class ServiceManagerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Reference
    ServiceManager serviceManager;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        PrintWriter pw = resp.getWriter();
        pw.println("<HTML>");
        pw.println(getHeader());
        pw.println("<BODY><H1>Service Deployments</H1>");

        pw.println("<UL>");
        try {
            for (String dep : serviceManager.listServices()) {
                pw.println("<LI>" + dep);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
        pw.println("</UL>");

        pw.println("<FORM METHOD='POST'>New Container: <BR>"
                + "<LABEL CLASS='l1' FOR='name'>Name: </LABEL><INPUT TYPE='text' NAME='name' VALUE='myapache' CLASS='l1'><BR>"
                + "<LABEL CLASS='l1' FOR='image'>Image: </LABEL><INPUT TYPE='text' NAME='image' VALUE='httpd' CLASS='l1'><BR>"
                + "<LABEL CLASS='l1' FOR='cpu'>CPU: </LABEL><INPUT TYPE='text' NAME='cpu' VALUE='0.2' CLASS='l1'>units<BR>"
                + "<LABEL CLASS='l1' FOR='memory'>Memory: </LABEL><INPUT TYPE='text' NAME='memory' VALUE='64' CLASS='l1'>mb<BR>"
                + "<LABEL CLASS='l1' FOR='ports'>Ports: </LABEL><INPUT TYPE='text' NAME='ports' VALUE='80' CLASS='l1'>(space separated)<BR>"
                + "<INPUT TYPE='submit' VALUE='create!'>");
        pw.println("</FORM>");
        pw.println("</BODY></HTML>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        String name = req.getParameter("name");
        String image = req.getParameter("image");
        double cpu = Double.parseDouble(req.getParameter("cpu"));
        int memory = Integer.parseInt(req.getParameter("memory"));
        int[] ports = Arrays.stream(req.getParameter("ports").split(" ")).mapToInt(Integer::parseInt).toArray();

        Builder builder = ServiceConfig.builder(name, image).cpu(cpu).memory(memory);
        for (int p : ports) {
            builder.port(p);
        }

        try {
            serviceManager.getService(builder.build());
        } catch (Exception e) {
            throw new ServletException(e);
        }

        PrintWriter pw = resp.getWriter();
        pw.println("<HTML>");
        pw.println(getHeader());
        pw.println("<BODY><H1>Service Created!</H1>");
        pw.println("<A HREF='manager'>List Services</A>");
        pw.println("</BODY>");
    }

    private String getHeader() {
        return ("<HEAD><TITLE>Service Deployments</TITLE>"
                + "<STYLE>"
                + "label.l1 {"
                + "  width: 100px;"
                + "  float: left;"
                + "}"
                + "</STYLE>"
                + "</HEAD>");
    }
}
