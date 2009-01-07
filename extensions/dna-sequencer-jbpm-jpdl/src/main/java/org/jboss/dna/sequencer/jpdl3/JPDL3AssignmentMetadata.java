/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.dna.sequencer.jpdl3;

/**
 * @author Serge Pagop
 */
public class JPDL3AssignmentMetadata {

    /**
     * The full qualified class name.
     */
    private String fqClassName = "";

    /**
     * The expression.
     */
    private String expression = "";

    /**
     * The config type.
     */
    private String configType = "";

    /**
     * Get the full qualified name of the class delegation.
     * 
     * @return the fqClassName.
     */
    public String getFqClassName() {
        return this.fqClassName;
    }

    /**
     * Set the full qualified name of the class delegation.
     * 
     * @param fqClassName Sets fqClassName to the specified value.
     */
    public void setFqClassName( String fqClassName ) {
        this.fqClassName = fqClassName;
    }

    /**
     * Get the assignment expression for the jpdl identity component.
     * 
     * @return the expression.
     */
    public String getExpression() {
        return this.expression;
    }

    /**
     * Set the expression.
     * 
     * @param expression Sets expression to the specified value.
     */
    public void setExpression( String expression ) {
        this.expression = expression;
    }

    /**
     * Get the configType.
     * 
     * @return configType
     */
    public String getConfigType() {
        return this.configType;
    }

    /**
     * @param configType Sets configType to the specified value.
     */
    public void setConfigType( String configType ) {
        this.configType = configType;
    }

}
