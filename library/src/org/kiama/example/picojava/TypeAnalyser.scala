/*
 * This file is part of Kiama.
 *
 * Copyright (C) 2008-2015 Anthony M Sloane, Macquarie University.
 *
 * Kiama is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Kiama is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Kiama.  (See files COPYING and COPYING.LESSER.)  If not, see
 * <http://www.gnu.org/licenses/>.
 */

/*
 * This file is derived from a JastAdd implementation of PicoJava, created
 * in the Department of Computer Science at Lund University.  See the
 * following web site for details:
 *
 * http://jastadd.cs.lth.se/examples/PicoJava/index.shtml
 */

package org.kiama
package example.picojava

import org.kiama.attribution.Attribution

trait TypeAnalyser {

    self : Attribution with NameResolution with NullObjects with PredefinedTypes =>

    import PicoJavaTree._

    /**
     * Is this declaration unknown?
     *
     * syn boolean Decl.isUnknown() = false;
     * eq UnknownDecl.isUnknown() = true;
     */
    val isUnknown : Decl => Boolean =
        attr {
            case UnknownDecl (_) => true
            case _               => false
        }

    /**
     * Return the type of a construct or unknownDecl if none.
     *
     * syn lazy TypeDecl Decl.type();
     * syn lazy TypeDecl Exp.type();
     * eq TypeDecl.type() = this;
     * eq VarDecl.type() = getType().decl().type();
     * eq IdnUse.type() = decl().type();
     * eq Dot.type() = getIdnUse().type();
     * eq BooleanLiteral.type() = booleanType();
     */
    val tipe : PicoJavaNode => TypeDecl =
        attr {
            case t : TypeDecl       => t
            case v : VarDecl        => tipe (decl (v.Type))
            case i : IdnUse         => tipe (decl (i))
            case d : Dot            => tipe (d.IdnUse)
            case b : BooleanLiteral => booleanType (b)
            case t                  => unknownDecl (t)
        }

    /**
     * Is this declaration for a type that is a subtype of the
     * provided type.  The unknown type is a subtype of all types.
     *
     * syn lazy boolean TypeDecl.isSubtypeOf(TypeDecl typeDecl);
     * eq TypeDecl.isSubtypeOf(TypeDecl typeDecl) = typeDecl.isSuperTypeOf(this);
     * eq ClassDecl.isSubtypeOf(TypeDecl typeDecl) = typeDecl.isSuperTypeOfClassDecl(this);
     * eq UnknownDecl.isSubtypeOf(TypeDecl typeDecl) = true;
     */
    val isSubtypeOf : TypeDecl => TypeDecl => Boolean =
        paramAttr {
             typedecl => {
                 case UnknownDecl (_) => true
                 case c : ClassDecl   => isSuperTypeOfClassDecl (c) (typedecl)
                 case t : TypeDecl    => isSuperTypeOf (t) (typedecl)
             }
        }

    /**
     * Is this declaration for a type that is a supertype of the
     * provided type?  The unknown type is a supertype of all types.
     *
     * syn lazy boolean TypeDecl.isSuperTypeOf(TypeDecl typeDecl) = this == typeDecl;
     * eq UnknownDecl.isSuperTypeOf(TypeDecl typeDecl) = true;
     */
    val isSuperTypeOf : TypeDecl => TypeDecl => Boolean =
        paramAttr {
            typedecl => {
                case UnknownDecl (_) => true
                case t               => t eq typedecl
            }
        }

    /**
     * Is this declaration for a type that is a supertype of the
     * provided class type?  The unknown type is a supertype of all
     * class types.
     *
     * syn lazy boolean TypeDecl.isSuperTypeOfClassDecl(ClassDecl typeDecl) =
     *    this == typeDecl || typeDecl.superClass() != null && typeDecl.superClass().isSubtypeOf(this);
     * eq UnknownDecl.isSuperTypeOfClassDecl(ClassDecl typeDecl) = true;
     */
    val isSuperTypeOfClassDecl : ClassDecl => TypeDecl => Boolean =
        paramAttr {
            typedecl => {
                case UnknownDecl (_) => true
                case t : TypeDecl    =>
                    (t eq typedecl) || (superClass (typedecl) != null) && (isSubtypeOf (superClass (typedecl)) (t))
            }
        }

    /**
     * Return the superclass of a class (or null if none).
     *
     * syn lazy ClassDecl ClassDecl.superClass();
     * eq ClassDecl.superClass() {
     *     if (hasSuperclass() && getSuperclass().decl() instanceof ClassDecl && !hasCycleOnSuperclassChain())
     *         return (ClassDecl) getSuperclass().decl();
     *     else
     *         return null;
     * }
     */
    val superClass : ClassDecl => ClassDecl =
        attr (
            c =>
                c.Superclass match {
                    case Some (i) =>
                        decl (i) match {
                            case sc : ClassDecl if !hasCycleOnSuperclassChain (c) => sc
                            case _                                                => null
                        }
                    case None     => null
                }
        )

    /**
     * True if there is a cycle somewhere on the superclass chain, false otherwise.
     *
     * syn lazy boolean ClassDecl.hasCycleOnSuperclassChain() circular [true];
     * eq ClassDecl.hasCycleOnSuperclassChain() {
     *    if (hasSuperclass() && getSuperclass().decl() instanceof ClassDecl) //First, check if there is a superclass
     *        return ((ClassDecl) getSuperclass().decl()).hasCycleOnSuperclassChain();
     *    else
     *        return false;
     */
    val hasCycleOnSuperclassChain : ClassDecl => Boolean =
        circular (true) (
            c =>
                c.Superclass match {
                    case Some (i) =>
                        decl (i) match {
                            case sc : ClassDecl => hasCycleOnSuperclassChain (sc)
                            case _              => false
                        }
                    case None     => false
                }
        )

    /**
     * Is this expression a value or not?
     *
     * syn boolean Exp.isValue();
     * eq Exp.isValue() = true;
     * eq Dot.isValue() = getIdnUse().isValue();
     * eq TypeUse.isValue() = false;
     * Note! If we did not have the rewrites below, the above equation would have to instead be written as:
     * eq IdnUse.isValue() = !(decl() instanceof TypeDecl)
     *
     * FIXME: currently using the "without rewrites" version
     */
    val isValue : Exp => Boolean =
        attr {
            case i : IdnUse => ! decl (i).isInstanceOf[TypeDecl] // replace this one
            // with this one, when the rewrites are in:
            // case t : TypeUse => false
            case d : Dot   => isValue (d.IdnUse)
            case _         => true
        }

    /*
     * FIXME: need to do these at some point
     * Rewrite rules for replacing Use-nodes based on their declaration kind
     *
     * rewrite Use {
     *     when(decl() instanceof VarDecl)
     *     to VariableUse new VariableUse(getName());
     * }
     *
     * rewrite Use {
     *     when(decl() instanceof TypeDecl)
     *     to TypeUse new TypeUse(getName());
     * }
     */
}
