/*
 This file is part of the BlueJ program. 
 Copyright (C) 1999-2009  Michael Kolling and John Rosenberg 
 
 This program is free software; you can redistribute it and/or 
 modify it under the terms of the GNU General Public License 
 as published by the Free Software Foundation; either version 2 
 of the License, or (at your option) any later version. 
 
 This program is distributed in the hope that it will be useful, 
 but WITHOUT ANY WARRANTY; without even the implied warranty of 
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 GNU General Public License for more details. 
 
 You should have received a copy of the GNU General Public License 
 along with this program; if not, write to the Free Software 
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. 
 
 This file is subject to the Classpath exception as provided in the  
 LICENSE.txt file that accompanied this code.
 */
package bluej.parser.entity;

public interface EntityResolver
{
    /**
     * Resolve a package or class. If a class with the given name exists in the resolver's scope,
     * it is returned; otherwise a package is returned.
     * 
     * @param name  The package or class name. This must be an unqualified name.
     */
    public PackageOrClass resolvePackageOrClass(String name);
    
    /**
     * Resolve a class from its fully-qualified name. The supplied name should
     * be the same as would be returned by Class.getName() for the required type.
     */
    public ClassEntity resolveQualifiedClass(String name);
    
    /**
     * Resolve a value. If a local variable or field with the given name exists in the resolver's
     * scope, it is returned; otherwise the effect is as if resolvePackageOrClass was called.
     * 
     * <p>To resolve the final value entity, call resolveAsValue() on the returned entity.
     */
    public JavaEntity resolveValueEntity(String name);
}
